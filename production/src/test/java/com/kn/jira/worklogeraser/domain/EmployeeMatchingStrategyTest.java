package com.kn.jira.worklogeraser.domain;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PdmAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PersonInPdm;
import com.kn.jira.worklogeraser.sharedresources.JiraAdapterFixture;
import com.kn.jira.worklogeraser.sharedresources.PdmAdapterFixture;
import com.kn.jira.worklogeraser.sharedresources.TestConfigurationWithMockAdaptersFixture;

public abstract class EmployeeMatchingStrategyTest {
   protected EmployeeMatchingStrategy employeeMatchingStrategy;
   public JiraAdapter jiraAdapter;
   protected JiraAdapterFixture jiraAdapterFixture;
   protected EraseActionLogger mockActionLogger;
   protected PdmAdapter pdmAdapter;
   protected PdmAdapterFixture pdmAdapterFixture;
   protected List<Worklog> subjectWorklogs;
   protected TestConfigurationWithMockAdaptersFixture testConfiguration;

   @Before public void beforeEachTest() {
      testConfiguration = new TestConfigurationWithMockAdaptersFixture();
      testConfiguration.setUp();
      jiraAdapterFixture = testConfiguration.getJiraAdapterFixture();
      jiraAdapter = jiraAdapterFixture.getJiraAdapter();
      pdmAdapterFixture = testConfiguration.getPdmAdapterFixture();
      pdmAdapter = pdmAdapterFixture.getPdmAdapter();
      
      employeeMatchingStrategy = testConfiguration.getEmployeeMatchingStrategy();
      subjectWorklogs = jiraAdapterFixture.getExpectedSubjectWorklogs();
      
      mockActionLogger = mock( EraseActionLogger.class );
      Whitebox.setInternalState( employeeMatchingStrategy, "actionLogger", mockActionLogger );
   }

   @After
   public void afterEachTest() {
      testConfiguration.tearDown();
   }

   //Test methods
   @Test
   public void performErase_forEachWorklog_retrievesPersonFromPdm() {
      employeeMatchingStrategy.perforErase( subjectWorklogs );
   
      for( Worklog worklog : subjectWorklogs ){
         User jiraUser = testConfiguration.determineJiraUser( worklog );
         
         verify( pdmAdapter, atLeastOnce() ).findPersonByEmail( jiraUser.getEmailAddress() );
      }
   }

   @Test
   public void performErase_whenWorklogPerformerIsGermanEmployee_deteletesWorklog() {
      employeeMatchingStrategy.perforErase( subjectWorklogs );
   
      for( Worklog worklog : subjectWorklogs ){
         PersonInPdm person = testConfiguration.determinePersonInPdm( worklog );
         if( employeeMatchingStrategy.verifyPerformer( person )){
            verify( jiraAdapter, atLeastOnce() ).deleteWorklog( worklog );
         }
      }
   }

   @Test
   public void performErase_whenWorklogPerformerIsGermanEmployee_logsDeleteAction() {
      employeeMatchingStrategy.perforErase( subjectWorklogs );
   
      for( Worklog worklog : subjectWorklogs ){
         PersonInPdm person = testConfiguration.determinePersonInPdm( worklog );
         
         if( employeeMatchingStrategy.verifyPerformer( person )){
            verify( jiraAdapter, atLeastOnce() ).deleteWorklog( worklog );
            verify( mockActionLogger, atLeastOnce() ).worklogDeleted( worklog.getIssueUri().toString(), person.getEmailAddress(), worklog.getComment() );
         }
      }
   }

   @Test
   public void performErase_whenWorklogWasDeleted_addsCommentToTheIssue() {
      employeeMatchingStrategy.perforErase( subjectWorklogs );
   
      for( Worklog worklog : subjectWorklogs ){
         PersonInPdm person = testConfiguration.determinePersonInPdm( worklog );
         
         if( employeeMatchingStrategy.verifyPerformer( person )){
            verify( jiraAdapter, atLeastOnce() ).signIssueAsManipulated( worklog.getIssueUri() );
            verify( mockActionLogger, atLeastOnce() ).worklogWasManipulated();
         }
      }
      
      verify( mockActionLogger, atLeastOnce() ).worklogDeleted( anyString(), anyString(), anyString() );
   }

}
