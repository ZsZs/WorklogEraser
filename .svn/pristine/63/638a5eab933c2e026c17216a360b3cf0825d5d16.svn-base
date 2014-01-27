package com.kn.jira.worklogeraser.jiraadapter;

import java.net.URI;
import java.text.MessageFormat;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.ProjectRestClient;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.UserRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.collect.Lists;

public class JiraAdapter {
   private static final String COMMENT_TEXT = "Worklogs was modified by 'WorklogEraser'.";
   final Logger logger = LoggerFactory.getLogger( getClass() );
   private RestTemplate httpClient;
   private JiraRestClient jiraClient;
   private final String password;
   private final String serverUriString;
   private final String userName;

   //Constructors
   public JiraAdapter( final String serverUriString, final String userName, final String password ) {
      this.serverUriString = serverUriString + "/api/2/";
      this.userName = userName;
      this.password = password;
      instantiateJiraRestClient();
      instantiateHttpClient();
   }
   
   //Public accessors and mutators
   public void deleteWorklog( Worklog worklog ) {
      String issueId = StringUtils.substringAfterLast( worklog.getIssueUri().toString(), "/" );
      String worklogId = StringUtils.substringAfterLast( worklog.getSelf().toString(), "/" );
      UriBuilder worklogUri = UriBuilder.fromUri( serverUriString ).path( "issue" ).path( issueId ).path( "worklog" ).path( worklogId );
      httpClient.delete( worklogUri.build() );
   }

   public List<BasicProject> findAllProjects() {
      List<BasicProject> foundProjects = Lists.newArrayList();
      final ProjectRestClient projectClient = jiraClient.getProjectClient();

      for( final BasicProject projectSummary : projectClient.getAllProjects().claim() ){
         foundProjects.add( projectSummary );
      }
      
      return foundProjects;
   }
   
   public List<Issue> findClosedObsolatedIssues( String project, String status, String date ) {
      String queryTemplate = "project=\"{0}\" AND status=\"{1}\" AND status CHANGED BEFORE \"{2}\"";
      String query = MessageFormat.format( queryTemplate, new Object[]{ project, status, date });
      
      return findIssuesByQuery( query );
   }
   
   public List<Issue> findIssuesByQuery( String query ) {
      List<Issue> foundIssues = Lists.newArrayList();
      
      SearchRestClient searchClient = jiraClient.getSearchClient();
      IssueRestClient issueClient = jiraClient.getIssueClient();
      SearchResult searchResult = searchClient.searchJql( query ).claim();
      for( final BasicIssue issueSummary : searchResult.getIssues() ){
         Issue jiraIssue = issueClient.getIssue( issueSummary.getKey() ).claim();
         foundIssues.add( jiraIssue );
      }
      
      return foundIssues;
   }
   
   public User findUserDetails( String userName ){
      UserRestClient userService = jiraClient.getUserClient();
      User userDetails = userService.getUser( userName ).claim();
      return userDetails;
   }
   
   public User findUserDetails( BasicUser userSummary ){
      return findUserDetails( userSummary.getName() );
   }
   
   public void setUp(){
   }
   
   public void signIssueAsManipulated( URI issueUri ){
      IssueRestClient issueClient = jiraClient.getIssueClient();
      URI commentUri = UriBuilder.fromUri( issueUri ).path( "comment" ).build();
      try{
         issueClient.addComment( commentUri, Comment.valueOf( COMMENT_TEXT ));
      }catch( Throwable e ){
         e.printStackTrace( );
      }
   }
   
   public void tearDown(){
   }

   //Properties
   public String getUser() { return userName; }
   
   //Protected, private helper methods
   private void configureHttpClientContext(){
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      UsernamePasswordCredentials credentials = new UsernamePasswordCredentials( userName, password );
      AuthScope authtehnticationScope = new AuthScope( AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM );
      credentialsProvider.setCredentials( authtehnticationScope, credentials );
      HttpClientContext context = HttpClientContext.create();
      context.setCredentialsProvider( credentialsProvider );
   }
   
   private void instantiateHttpClient(){
      HttpComponentsClientHttpRequestFactory httpClientFactory = new HttpComponentsClientHttpRequestFactory();
      httpClient = new RestTemplate( httpClientFactory );
      configureHttpClientContext();
   }
   
   private void instantiateJiraRestClient(){
      URI serverUri = URI.create( serverUriString );
      try{
         AsynchronousJiraRestClientFactory clientFactory = new AsynchronousJiraRestClientFactory();
         jiraClient = clientFactory.createWithBasicHttpAuthentication( serverUri, userName, password );
      }catch( Exception e ){
         logger.error( "Couldn't instantiate the Jira REST client.", e );;
      }
   }
   
}
