package com.kn.jira.worklogeraser.jiraadapter;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.kn.jira.worklogeraser.sharedresources.TestConfigurationFixture;
import com.kn.jira.worklogeraser.sharedresources.TestConfigurationWithMockAdaptersFixture;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AsynchronousJiraRestClientFactory.class })
public class JiraAdapterTest {
   private TestConfigurationFixture testConfiguration;
   private JiraAdapter jiraAdapter;

   @Before public void beforeEachTest(){
      testConfiguration = new TestConfigurationWithMockAdaptersFixture();
      testConfiguration.setUp();
      jiraAdapter = testConfiguration.getJiraAdapter();
   }
   
   @After public void afterEachTest(){
      testConfiguration.tearDown();
   }
   
   @Ignore @Test public void findAllProjects_returnsListOfJiraProjects(){
      List<BasicProject> projects = jiraAdapter.findAllProjects();
      
      assertThat( projects.size(), greaterThan( 0 ));
   }
   
    @Ignore @Test public void findClosedObsolatedIssues(){
      List<Issue> issues = jiraAdapter.findClosedObsolatedIssues( "WORKLOG", "CLOSED", "2013/12/13" );
      
      assertThat( issues.size(), greaterThan( 0 ));
   }

   @Ignore @Test public void findIssuesByQuery_returnsListOfJiraIssues(){
      List<Issue> issues = jiraAdapter.findIssuesByQuery( "project=\"ADMIN\"" );
      
      assertThat( issues.size(), greaterThan( 0 ));
   }
   
   //Private helper methods
}
