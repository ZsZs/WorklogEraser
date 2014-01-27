package com.kn.jira.worklogeraser.sharedresources;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.net.URI;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.springframework.context.ApplicationContext;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PdmAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PersonInPdm;

@PrepareForTest({ AsynchronousJiraRestClientFactory.class })
public class TestConfigurationWithMockAdaptersFixture extends TestConfigurationFixture {
   private JiraAdapterFixture jiraAdapterFixture;
   private ApplicationContext mockApplicationContext;
   private JiraAdapter mockJiraAdapter;
   private JiraRestClient mockJiraRestClient;
   private PdmAdapter mockPdmAdapter;
   private PdmAdapterFixture pdmAdapterFixture;

   // Public accessors and mutators
   public User determineJiraUser( Worklog worklog ) {
      BasicUser jiraUserSummary = worklog.getAuthor();
      User jiraUser = mockJiraAdapter.findUserDetails( jiraUserSummary );
      return jiraUser;
   }

   public PersonInPdm determinePersonInPdm( Worklog worklog ) {
      User jiraUser = determineJiraUser( worklog );
      PersonInPdm person = mockPdmAdapter.findPersonByEmail( jiraUser.getEmailAddress() );
      return person;
   }

   @Override
   public void setUp() {
      mockJiraRestClient();
      super.setUp();
   }

   @Override
   public void tearDown() {
      jiraAdapterFixture.tearDown();
      pdmAdapterFixture.tearDown();
      super.tearDown();
   }

   // Properties
   public JiraAdapterFixture getJiraAdapterFixture() {
      return jiraAdapterFixture;
   }

   public PdmAdapterFixture getPdmAdapterFixture() {
      return pdmAdapterFixture;
   }

   // Protected, private helper methods
   protected void mockAdapters() {
      jiraAdapterFixture = new JiraAdapterFixture();
      jiraAdapterFixture.setUp( applicationContext );
      mockJiraAdapter = jiraAdapterFixture.getJiraAdapter();

      pdmAdapterFixture = new PdmAdapterFixture();
      pdmAdapterFixture.setUp();
      mockPdmAdapter = pdmAdapterFixture.getPdmAdapter();
   }

   @Override
   protected void retrieveBeans() {
      mockAdapters();
      stubApplicationContextToReturnMockAdapters();
      super.retrieveBeans();
      Whitebox.setInternalState( worklogEraser, "applicationContext", mockApplicationContext );
      Whitebox.setInternalState( employeeMatchingStrategy, "applicationContext", mockApplicationContext );
   }

   private void mockJiraRestClient() {
      mockJiraRestClient = mock( JiraRestClient.class );
      mockStatic( AsynchronousJiraRestClientFactory.class );
      AsynchronousJiraRestClientFactory mockJiraClientFactory = mock( AsynchronousJiraRestClientFactory.class );
      try{
         whenNew( AsynchronousJiraRestClientFactory.class ).withAnyArguments().thenReturn( mockJiraClientFactory );
      }catch( Exception e ){
         e.printStackTrace();
      }
      
      when( mockJiraClientFactory.createWithBasicHttpAuthentication( (URI) anyObject(), anyString(), anyString( ))).thenReturn( mockJiraRestClient );
      
      SearchResult mockSearchResult = mock( SearchResult.class );
      @SuppressWarnings( "unchecked" )
      Promise<SearchResult> mockPromise = mock( Promise.class );
      when( mockPromise.claim() ).thenReturn( mockSearchResult );
      SearchRestClient mockSearchClient = mock( SearchRestClient.class );
      when( mockSearchClient.searchJql( "project=\"WORKLOG\" AND status=\"CLOSED\" AND status CHANGED BEFORE \"2013/12/13\"" )).thenReturn( mockPromise );
      IssueRestClient mockIssueClient = mock( IssueRestClient.class );
      when( mockJiraRestClient.getSearchClient() ).thenReturn( mockSearchClient );
      when( mockJiraRestClient.getIssueClient() ).thenReturn( mockIssueClient );
   }

   private void stubApplicationContextToReturnMockAdapters() {
      mockApplicationContext = spy( applicationContext );
      when( mockApplicationContext.getBean( "jiraAdapter", JiraAdapter.class ) ).thenReturn( mockJiraAdapter );
      when( mockApplicationContext.getBean( "pdmAdapter", PdmAdapter.class ) ).thenReturn( mockPdmAdapter );
   }
}
