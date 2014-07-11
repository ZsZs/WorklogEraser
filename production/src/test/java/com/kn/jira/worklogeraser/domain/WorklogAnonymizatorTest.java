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

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.kn.jira.worklogeraser.sharedresources.JiraAdapterFixture;
import com.kn.jira.worklogeraser.sharedresources.TestConfigurationWithMockAdaptersFixture;

public class WorklogAnonymizatorTest {
   private Properties configurationProperties;
   private JiraAdapterFixture jiraAdapterFixture;
   private TestConfigurationWithMockAdaptersFixture testConfiguration;
   private WorklogAnonymizator worklogAnonymizator;
   private Integer obsolescenceTimePeriod;

   @Before public void beforeEachTest() {
      testConfiguration = new TestConfigurationWithMockAdaptersFixture();
      testConfiguration.setUp();
      jiraAdapterFixture = testConfiguration.getJiraAdapterFixture();
      
      worklogAnonymizator = testConfiguration.getWorklogEraser();
      configurationProperties = testConfiguration.getConfigurationProperties();
      
      obsolescenceTimePeriod = Integer.parseInt( configurationProperties.getProperty( WorklogAnonymizator.CONF_OBSOLESCENCE_TIME_PERIOD ));
   }

   @After public void afterEachTest(){
      testConfiguration.tearDown();
   }
   
   @Test public void perform_calculatesObsolatedWorklogsDate() throws Exception{
      worklogAnonymizator.perform();
      
      DateTime currentDate = new DateTime();
      
      assertThat( worklogAnonymizator.getObsolatedWorklogDate(), equalTo( currentDate.minusDays( obsolescenceTimePeriod ).withTimeAtStartOfDay().toDate() ));
   }
   
   @Test public void perform_instantiatesActionLogger(){
      worklogAnonymizator.perform();
      
      AnonymizationActionLogger actionLogger = Whitebox.getInternalState( worklogAnonymizator, "actionLogger" );
      
      assertThat( actionLogger, notNullValue() );
   }
   
   @Test public void perform_collectsAllJiraProjects(){
      worklogAnonymizator.perform();
      
      List<BasicProject> foundProjects = Whitebox.getInternalState( worklogAnonymizator, "allJiraProjects", WorklogAnonymizator.class );
      
      assertThat( foundProjects, equalTo( jiraAdapterFixture.getProjects() ));
   }

   @Test public void perform_collectsJiraIsuesWithCondition(){
      worklogAnonymizator.perform();
      
      Map<URI, Issue> foundIssues = Whitebox.getInternalState( worklogAnonymizator, "subjectIssues", WorklogAnonymizator.class );
      
      assertThat( foundIssues, equalTo( jiraAdapterFixture.getExpectedSubjectIssues() ));
   }

   @Test public void perform_collectsJiraWorklogsWithCondition(){
      worklogAnonymizator.perform();
      
      List<Worklog> foundWorklogs = Whitebox.getInternalState( worklogAnonymizator, "subjectWorklogs", WorklogAnonymizator.class );
      
      assertThat( foundWorklogs, equalTo( jiraAdapterFixture.getExpectedSubjectWorklogs() ));
   }

   @Test public void perform_instantiatesMatchingStrategy() throws Exception {
      worklogAnonymizator.perform();
      
      EmployeeMatchingStrategy currentMatchingStrategy = Whitebox.getInternalState( worklogAnonymizator, "employeeMatchingStrategy", WorklogAnonymizator.class ); 
      assertThat( currentMatchingStrategy, equalTo( testConfiguration.getEmployeeMatchingStrategy() ));
   }

   @Test public void perform_delegatesToMatchingStrategy() throws Exception {
      EmployeeMatchingStrategy spyStrategy = spy( testConfiguration.getEmployeeMatchingStrategy() );
      Whitebox.setInternalState( worklogAnonymizator, "employeeMatchingStrategy", spyStrategy );
      
      worklogAnonymizator.perform();
      
      List<Worklog> subjectWorklogs = Whitebox.getInternalState( worklogAnonymizator, "subjectWorklogs" );
      for( Worklog worklog : subjectWorklogs ){
         verify( spyStrategy, times( 1 )).isWorklogEffected( worklog );
      }
   }

   @Test public void perform_delegatesToAnonymizatioStrategy() throws Exception {
      AnonymizationStrategy spyStrategy = spy( testConfiguration.getAnonymizationStrategy() );
      Whitebox.setInternalState( worklogAnonymizator, "anonymizationStrategy", spyStrategy );
      
      worklogAnonymizator.perform();
      
      List<Worklog> subjectWorklogs = Whitebox.getInternalState( worklogAnonymizator, "subjectWorklogs" );
      for( Worklog worklog : subjectWorklogs ){
         verify( spyStrategy, times( 1 )).perform( worklog );
      }
   }
   
   //Protected, private helper methods
}