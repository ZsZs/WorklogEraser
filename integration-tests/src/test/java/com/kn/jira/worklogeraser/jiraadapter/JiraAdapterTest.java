package com.kn.jira.worklogeraser.jiraadapter;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.text.SimpleDateFormat;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.google.common.collect.Lists;
import com.kn.jira.worklogeraser.domain.WorklogEraser;
import com.kn.jira.worklogeraser.sharedresources.TestConfigurationFixture;

public class JiraAdapterTest {
   private static final String PROJECT_NAME = "WORKLOG";
   private static final String QUERY_CLOSED_AND_OBSOLATE_ISSUES = "project=\"WORKLOG\" AND status=\"CLOSED\" AND status CHANGED BEFORE \"2013/12/18\"";
   private static final String QUERY_CLOSED_ISSUES = "project=\"WORKLOG\" AND status=\"CLOSED\"";
   private static final String QUERY_ALL_ISSUES = "project=\"WORKLOG\"";
   private static final String STATUS_NAME = "CLOSED";
   private SimpleDateFormat dateFormat = new SimpleDateFormat( WorklogEraser.DATE_FORMAT );
   private JiraAdapter jiraAdapter;
   private TestConfigurationFixture testConfiguration;

   @Before public void beforeEachTest(){
      testConfiguration = new TestConfigurationFixture();
      testConfiguration.setUp();
      jiraAdapter = testConfiguration.getJiraAdapter();
   }
   
   @After public void afterEachTest(){
      testConfiguration.tearDown();
   }
   
   @Test( expected = JiraAdapterDeleteWorklogException.class ) 
   public void deleteWorklog_whenIssueChangeIsForbidden_throwsException() throws JiraAdapterException{
      Issue subjectIssue = findTheFirstSubjectIssue();
      jiraAdapter.reopenIssue( subjectIssue );
      List<Worklog> worklogs = Lists.newArrayList( subjectIssue.getWorklogs() );
      Worklog subjectWorklog = worklogs.get( 0 );
      
      jiraAdapter.deleteWorklog( subjectWorklog );
   }
   
   @Test public void findAllProjects_returnsListOfJiraProjects(){
      List<BasicProject> projects = jiraAdapter.findAllProjects();
      
      assertThat( projects.size(), greaterThan( 0 ));
   }

   @Test public void findIssuesByQuery_returnsListOfJiraIssues() throws JiraAdapterException{
      List<Issue> allIssues = jiraAdapter.findIssuesByQuery( QUERY_ALL_ISSUES );
      
      assertThat( allIssues.size(), greaterThan( 0 ));
   }
   
   @Test public void findIssuesByQuery_considersIssueStatus() throws JiraAdapterException{
      List<Issue> allIssues = jiraAdapter.findIssuesByQuery( QUERY_ALL_ISSUES );
      List<Issue> closedIssues = jiraAdapter.findIssuesByQuery( QUERY_CLOSED_ISSUES );
      
      assertThat( closedIssues.size(), lessThan( allIssues.size() ));
   }
   
   @Test public void findIssuesByQuery_considersIssueStatusAndObsolation() throws JiraAdapterException{
      List<Issue> closedIssues = jiraAdapter.findIssuesByQuery( QUERY_CLOSED_ISSUES );
      List<Issue> obsolatedIssues = jiraAdapter.findIssuesByQuery( QUERY_CLOSED_AND_OBSOLATE_ISSUES );
      
      assertThat( obsolatedIssues.size(), lessThan( closedIssues.size() ));
   }
   
   @Test public void findUserDetails_determinesFullUserFromSummary(){
      User userDetails = jiraAdapter.findUserDetails( "admin" );
      
      assertThat( userDetails.getName(), equalTo( "admin" ));
   }
   
   @Test public void reopenIssue_whenTransitionExists_setsStatusToOpen() throws JiraAdapterException{
      //SETUP:
      Issue subjectIssue = findTheFirstSubjectIssue();

      //EXECUTE:
      jiraAdapter.reopenIssue( subjectIssue );
      
      //VERIFY:
      assertThat( reloadIssue( subjectIssue ).getStatus().getName(), equalTo( "Reopened" ));
      
      //TEAR DOWN:
      jiraAdapter.closeIssue( subjectIssue );
   }

   @Test public void signIssueAsManipulated_addsCommentToIssue() throws JiraAdapterException{
      //SETUP:
      Issue subjectIssue = findTheFirstSubjectIssue();
      List<Worklog> worklogs = Lists.newArrayList( subjectIssue.getWorklogs() );
      Worklog subjectWorklog = worklogs.get( 0 );
      Integer numberOfCommentsBeforeDelete = Lists.newArrayList( subjectIssue.getComments() ).size();
      
      //EXECUTE:
      jiraAdapter.signIssueAsManipulated( subjectWorklog.getIssueUri() );
      
      //VERIFY:
      subjectIssue = findTheFirstSubjectIssue();
      assertThat( Lists.newArrayList( subjectIssue.getComments() ).size(), greaterThan( numberOfCommentsBeforeDelete ));
   }

   //Private helper methods
   private String calculateTomorrow() {
      DateTime currentDate = new DateTime();
      String tomorrow = dateFormat.format( currentDate.plusDays( 1 ).withTimeAtStartOfDay().toDate() );
      return tomorrow;
   }
   
   private Issue findTheFirstSubjectIssue() throws JiraAdapterException {
      List<Issue> obsolatedIssues = jiraAdapter.findClosedObsolatedIssues( PROJECT_NAME, STATUS_NAME, calculateTomorrow() );
      Issue subjectIssue = obsolatedIssues.get( 0 );
      return subjectIssue;
   }
   
   private Issue reloadIssue( Issue subjectIssue ) {
      return jiraAdapter.findIssueByKey( subjectIssue.getKey() );
   }

}
