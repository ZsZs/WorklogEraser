package com.kn.jira.worklogeraser.sharedresources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;

import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicStatus;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kn.jira.worklogeraser.domain.WorklogEraser;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapterException;

public class JiraAdapterFixture {
   private static final String JIRA_BASE_URI = "http://localhost:2990/jira/browse";
   private Worklog anotherWorklogToDelete;
   private ApplicationContext applicationContext;
   private Map<URI,Issue> expectedSubjectIssues = Maps.newHashMap();
   private List<Worklog> expectedSubjectWorklogs = Lists.newArrayList();
   private Worklog worklogToDelete;
   private List<Issue> issues = Lists.newArrayList();
   private JiraAdapter mockJiraAdapter;
   private Worklog noneGermanEmployeeWorklog;
   private String obsolatedWorklogDate;
   private List<BasicProject> projects = Lists.newArrayList();
   private BasicProject projectA;
   private List<Issue> projectAissues = Lists.newArrayList();
   private List<Issue> projectAexpectedIssues = Lists.newArrayList();
   private List<Worklog> projectAexpectedWorklogs = Lists.newArrayList();
   private BasicProject projectB;
   private List<Issue> projectBissues = Lists.newArrayList();
   private List<Issue> projectBexpectedIssues = Lists.newArrayList();
   private List<Worklog> projectBexpectedWorklogs = Lists.newArrayList();
   private BasicUser germanEmployeeSummary;
   private BasicUser noneGermanEmployeeSummary;
   private User noneGermanEmployee;
   private User germanEmployee;
   
   //Public accessors and mutators
   public void setUp( ApplicationContext applicationContext ){
      this.applicationContext = applicationContext;
      mockJiraAdapter = mock( JiraAdapter.class );
      try{
         calculateObsolatedWorklogDate();
         createJiraUsers();
         createJiraWorklogs();
         createJiraProjects();
         createJiraIssues();
      }catch( URISyntaxException e ){
         e.printStackTrace();
      }catch( JiraAdapterException e ){
         e.printStackTrace();
      }
   }
   
   public void tearDown(){
   }

   //Properties
   public Map<URI,Issue> getExpectedSubjectIssues() { return expectedSubjectIssues; }
   public List<Worklog> getExpectedSubjectWorklogs() { return expectedSubjectWorklogs; }
   public List<Worklog> getProjectAexpectedWorklogs() { return projectAexpectedWorklogs; }
   public List<Worklog> getProjectBexpectedWorklogs() { return projectBexpectedWorklogs; }
   public List<Issue> getIssues() { return issues; }
   public JiraAdapter getJiraAdapter() { return mockJiraAdapter; }
   public List<BasicProject> getProjects() { return projects; }
   
   //Protected, private helper methods
   private void calculateObsolatedWorklogDate(){
      Properties configurationProperties = applicationContext.getBean( "configProperties", Properties.class );
      Integer obsolescenceTimePeriod = Integer.parseInt( configurationProperties.getProperty( WorklogEraser.OBSOLESCENCE_TIME_PERIOD ));
      DateTime currentDate = new DateTime();
      Date calculatedDate = currentDate.minusDays( obsolescenceTimePeriod ).withTimeAtStartOfDay().toDate();
      
      SimpleDateFormat dateFormat = new SimpleDateFormat( WorklogEraser.DATE_FORMAT ); 
      obsolatedWorklogDate = dateFormat.format( calculatedDate ); 
   }
   
   private void createJiraIssues() throws JiraAdapterException, URISyntaxException{
      BasicStatus mockOpenStatus = mock( BasicStatus.class );
      when( mockOpenStatus.getName() ).thenReturn( "Open" );
      BasicStatus mockClosedStatus = mock( BasicStatus.class );
      when( mockClosedStatus.getName() ).thenReturn( "Closed" );
      
      Issue openIssueWithoutWorklog = mock( Issue.class );
      when( openIssueWithoutWorklog.getKey() ).thenReturn(  "1000" );
      when( openIssueWithoutWorklog.getSelf() ).thenReturn(  new URI( JIRA_BASE_URI + "1000" ));
      when( openIssueWithoutWorklog.getStatus() ).thenReturn( mockOpenStatus );
      projectAissues.add( openIssueWithoutWorklog );
      projectBissues.add( openIssueWithoutWorklog );
      
      Issue openIssueWithWorklog = mock( Issue.class );
      when( openIssueWithWorklog.getKey() ).thenReturn(  "1001" );
      when( openIssueWithWorklog.getSelf() ).thenReturn(  new URI( JIRA_BASE_URI + "1001" ));
      when( openIssueWithWorklog.getStatus() ).thenReturn( mockOpenStatus );
      projectAissues.add( openIssueWithWorklog );
      projectBissues.add( openIssueWithWorklog );
      
      Issue resolvedIssueWithoutWorklog = mock( Issue.class );
      when( resolvedIssueWithoutWorklog.getKey() ).thenReturn(  "1002" );
      when( resolvedIssueWithoutWorklog.getSelf() ).thenReturn(  new URI( JIRA_BASE_URI + "1002" ));
      when( resolvedIssueWithoutWorklog.getStatus() ).thenReturn( mockClosedStatus );
      when( resolvedIssueWithoutWorklog.getWorklogs() ).thenReturn( ImmutableList.of( noneGermanEmployeeWorklog ));
      projectAissues.add( resolvedIssueWithoutWorklog );
      projectBissues.add( resolvedIssueWithoutWorklog );
      
      Issue resolvedIssueWithWorklog = mock( Issue.class );
      when( resolvedIssueWithWorklog.getKey() ).thenReturn(  "1003" );
      when( resolvedIssueWithWorklog.getSelf() ).thenReturn(  new URI( JIRA_BASE_URI + "1003" ));
      when( resolvedIssueWithWorklog.getStatus() ).thenReturn( mockClosedStatus );
      when( resolvedIssueWithWorklog.getWorklogs() ).thenReturn( ImmutableList.of( worklogToDelete, anotherWorklogToDelete ));
      projectAissues.add( resolvedIssueWithWorklog );
      projectAexpectedIssues.add( resolvedIssueWithWorklog );
      projectBissues.add( resolvedIssueWithWorklog );
      projectBexpectedIssues.add( resolvedIssueWithWorklog );
      
      String queryTemplate = "project=\"{0}\" AND status=\"{1}\" AND status CHANGED BEFORE \"{2}\"";
      String query = MessageFormat.format( queryTemplate, new Object[]{ "A", "Closed", obsolatedWorklogDate });
      when( mockJiraAdapter.findIssuesByQuery( query )).thenReturn( projectAexpectedIssues );
      when( mockJiraAdapter.findClosedObsolatedIssues( "A", "Closed", obsolatedWorklogDate )).thenReturn( projectAexpectedIssues );
      
      query = MessageFormat.format( queryTemplate, new Object[]{ "B", "Closed", obsolatedWorklogDate });
      when( mockJiraAdapter.findIssuesByQuery( query )).thenReturn( projectBexpectedIssues );
      when( mockJiraAdapter.findClosedObsolatedIssues( "B", "Closed", obsolatedWorklogDate )).thenReturn( projectBexpectedIssues );
      
      for( Issue issue : projectAexpectedIssues ){
         expectedSubjectIssues.put( issue.getSelf(), issue );
      }
      for( Issue issue : projectBexpectedIssues ){
         expectedSubjectIssues.put( issue.getSelf(), issue );
      }
   }
   
   private void createJiraProjects(){
      projectA = mock( BasicProject.class );
      when( projectA.getKey() ).thenReturn( "A" );
      projects.add( projectA );
      
      projectB = mock( BasicProject.class );
      when( projectB.getKey() ).thenReturn( "B" );
      projects.add( projectB );
      
      when( mockJiraAdapter.findAllProjects() ).thenReturn( projects );
   }
   
   private void createJiraUsers(){
      germanEmployeeSummary = mock( BasicUser.class );
      germanEmployee = mock( User.class );
      when( germanEmployee.getEmailAddress() ).thenReturn( "german.employee@kh.com" );
      when( mockJiraAdapter.findUserDetails( germanEmployeeSummary )).thenReturn( germanEmployee );
      
      noneGermanEmployeeSummary = mock( BasicUser.class );
      noneGermanEmployee = mock( User.class );
      when( noneGermanEmployee.getEmailAddress() ).thenReturn( "none.german.employee@kh.com" );
      when( mockJiraAdapter.findUserDetails( noneGermanEmployeeSummary )).thenReturn( noneGermanEmployee );
   }

   private void createJiraWorklogs() throws URISyntaxException{
      worklogToDelete = mock( Worklog.class );
      anotherWorklogToDelete = mock( Worklog.class );
      when( worklogToDelete.getAuthor() ).thenReturn( germanEmployeeSummary );
      when( worklogToDelete.getIssueUri() ).thenReturn( new URI( JIRA_BASE_URI ));
      
      when( anotherWorklogToDelete.getAuthor() ).thenReturn( germanEmployeeSummary );
      when( anotherWorklogToDelete.getIssueUri() ).thenReturn( new URI( JIRA_BASE_URI ));
      
      noneGermanEmployeeWorklog = mock( Worklog.class );
      when( noneGermanEmployeeWorklog.getAuthor() ).thenReturn( noneGermanEmployeeSummary );
      
      expectedSubjectWorklogs.add( worklogToDelete );
      projectAexpectedWorklogs.add( worklogToDelete );
      
      expectedSubjectWorklogs.add( anotherWorklogToDelete );
      projectBexpectedWorklogs.add( anotherWorklogToDelete );
   }
}
