package com.kn.jira.worklogeraser.domain;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.kn.jira.worklogeraser.sharedresources.JiraAdapterFixture;
import com.kn.jira.worklogeraser.sharedresources.TestConfigurationWithMockAdaptersFixture;

public class WorklogEraserTest {
   private Properties configurationProperties;
   private JiraAdapterFixture jiraAdapterFixture;
   private TestConfigurationWithMockAdaptersFixture testConfiguration;
   private WorklogEraser worklogEraser;
   private Integer obsolescenceTimePeriod;

   @Before public void beforeEachTest() {
      testConfiguration = new TestConfigurationWithMockAdaptersFixture();
      testConfiguration.setUp();
      jiraAdapterFixture = testConfiguration.getJiraAdapterFixture();
      
      worklogEraser = testConfiguration.getWorklogEraser();
      configurationProperties = testConfiguration.getConfigurationProperties();
      
      obsolescenceTimePeriod = Integer.parseInt( configurationProperties.getProperty( "worklogeraser.obsolescenceTimePeriod" ));
   }

   @After public void afterEachTest(){
      testConfiguration.tearDown();
   }
   
   @Test public void perform_calculatesObsolatedWorklogsDate() throws Exception{
      worklogEraser.perform();
      
      DateTime currentDate = new DateTime();
      
      assertThat( worklogEraser.getObsolatedWorklogDate(), equalTo( currentDate.minusDays( obsolescenceTimePeriod ).withTimeAtStartOfDay().toDate() ));
   }
   
   @Test public void perform_instantiatesActionLogger(){
      worklogEraser.perform();
      
      EraseActionLogger actionLogger = Whitebox.getInternalState( worklogEraser, "actionLogger" );
      
      assertThat( actionLogger, notNullValue() );
   }
   
   @Test public void perform_collectsAllJiraProjects(){
      worklogEraser.perform();
      
      List<BasicProject> foundProjects = Whitebox.getInternalState( worklogEraser, "allJiraProjects", WorklogEraser.class );
      
      assertThat( foundProjects, equalTo( jiraAdapterFixture.getProjects() ));
   }

   @Test public void perform_collectsJiraIsuesWithCondition(){
      worklogEraser.perform();
      
      Map<URI, Issue> foundIssues = Whitebox.getInternalState( worklogEraser, "subjectIssues", WorklogEraser.class );
      
      assertThat( foundIssues, equalTo( jiraAdapterFixture.getExpectedSubjectIssues() ));
   }

   @Test public void perform_collectsJiraWorklogsWithCondition(){
      worklogEraser.perform();
      
      List<Worklog> foundWorklogs = Whitebox.getInternalState( worklogEraser, "subjectWorklogs", WorklogEraser.class );
      
      assertThat( foundWorklogs, equalTo( jiraAdapterFixture.getExpectedSubjectWorklogs() ));
   }

   @Test public void perform_instantiatesMatchingStrategy() throws Exception {
      worklogEraser.perform();
      
      EmployeeMatchingStrategy currentMatchingStrategy = Whitebox.getInternalState( worklogEraser, "employeeMatchingStrategy", WorklogEraser.class ); 
      assertThat( currentMatchingStrategy, equalTo( testConfiguration.getEmployeeMatchingStrategy() ));
   }

   @Test public void perform_delegatesToMatchingStrategy() throws Exception {
      EmployeeMatchingStrategy spyStrategy = spy( testConfiguration.getEmployeeMatchingStrategy() );
      Whitebox.setInternalState( worklogEraser, "employeeMatchingStrategy", spyStrategy );
      
      worklogEraser.perform();

      verify( spyStrategy, times( 1 )).perforErase( jiraAdapterFixture.getExpectedSubjectIssues(), jiraAdapterFixture.getExpectedSubjectWorklogs() );
   }
   
   //Protected, private helper methods
}