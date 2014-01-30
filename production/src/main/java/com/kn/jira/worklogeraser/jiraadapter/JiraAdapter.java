package com.kn.jira.worklogeraser.jiraadapter;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
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
import org.springframework.web.client.HttpClientErrorException;
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
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.google.common.collect.Lists;

public class JiraAdapter {
   private static final String TRANSITION_COMMENT = "Transition was carried out by 'WorklogEraser'";
   public static final String CLOSE_TRANSITION_NAME = "Close Issue";
   public static final String RESOLVE_TRANSITION_NAME = "Resolve Issue";
   public static final String REOPEN_TRANSITION_NAME = "Reopen Issue";
   public static final String REOPENED_STATUS_NAME = "REOPENED";
   public static final String COMMENT_TEXT = "Worklogs was modified by 'WorklogEraser'.";
   private final Logger logger = LoggerFactory.getLogger( getClass() );
   private RestTemplate httpClient;
   private JiraRestClient jiraClient;
   private final String password;
   private final String restServicesUri;
   private final String serverUriString;
   private final String userName;
   private IssueRestClient issueClient;
   private SearchRestClient searchClient;

   // Constructors
   public JiraAdapter( final String serverUriString, final String userName, final String password ) {
      this.serverUriString = serverUriString;
      this.restServicesUri = this.serverUriString + "/api/2/";
      this.userName = userName;
      this.password = password;
      instantiateJiraRestClient();
      instantiateHttpClient();
   }

   // Public accessors and mutators
   public void closeIssue( Issue subjectIssue ) throws JiraAdapterException {
      Collection<FieldInput> fieldInputs = Arrays.asList( new FieldInput( "Resolution", "Fixed" ));
      performTransition( subjectIssue, CLOSE_TRANSITION_NAME, fieldInputs );
   }

   public void deleteWorklog( Worklog worklog ) throws JiraAdapterException {
      String issueId = StringUtils.substringAfterLast( worklog.getIssueUri().toString(), "/" );
      String worklogId = StringUtils.substringAfterLast( worklog.getSelf().toString(), "/" );
      UriBuilder worklogUri = UriBuilder.fromUri( restServicesUri ).path( "issue" ).path( issueId ).path( "worklog" ).path( worklogId );
      try{
         httpClient.delete( worklogUri.build() );
      }catch( HttpClientErrorException e ){
         if( e.getStatusCode().value() == 403 )
            throw new JiraAdapterDeleteWorklogException( worklogId, issueId, e );
         else
            throw new JiraAdapterException( "deleteWorklog", e );
      }
   }

   public List<BasicProject> findAllProjects() {
      List<BasicProject> foundProjects = Lists.newArrayList();
      final ProjectRestClient projectClient = jiraClient.getProjectClient();

      for( final BasicProject projectSummary : projectClient.getAllProjects().claim() ){
         foundProjects.add( projectSummary );
      }

      return foundProjects;
   }

   public List<Issue> findClosedObsolatedIssues( String project, String status, String date ) throws JiraAdapterException {
      String queryTemplate = "project={0} AND status={1} AND status CHANGED BEFORE {2}";
      String query = MessageFormat.format( queryTemplate, new Object[] { project, status, date } );
      query = StringUtils.replace( query, "/", "\\u002f" );
      return findIssuesByQuery( query );
   }
   
   public Issue findIssueByKey( String issueKey ){
      Issue foundIssue = issueClient.getIssue( issueKey ).claim();
      return foundIssue;
   }

   public List<Issue> findIssuesByQuery( String query ) throws JiraAdapterException {
      List<Issue> foundIssues = Lists.newArrayList();

      SearchResult searchResult = null;
      try{
         Promise<SearchResult> promise = searchClient.searchJql( query ); 
         searchResult = promise.claim();
         for( final BasicIssue issueSummary : searchResult.getIssues() ){
            Issue jiraIssue = issueClient.getIssue( issueSummary.getKey() ).claim();
            foundIssues.add( jiraIssue );
         }
      }catch( Exception e ){
         throw new JiraAdapterException( "findIssuesByQuery", e );
      }

      return foundIssues;
   }

   public User findUserDetails( String userName ) {
      UserRestClient userService = jiraClient.getUserClient();
      User userDetails = userService.getUser( userName ).claim();
      return userDetails;
   }

   public User findUserDetails( BasicUser userSummary ) {
      return findUserDetails( userSummary.getName() );
   }
   
   public void reopenIssue( Issue subjectIssue ) throws JiraAdapterException {
      Collection<FieldInput> fieldInputs = Arrays.asList();
      performTransition( subjectIssue, REOPEN_TRANSITION_NAME, fieldInputs );
   }

   public void resolveIssue( Issue subjectIssue ) throws JiraAdapterException {
      Collection<FieldInput> fieldInputs = Arrays.asList( new FieldInput( "Resolution", "Fixed" ));
      performTransition( subjectIssue, RESOLVE_TRANSITION_NAME, fieldInputs );
   }

   public void setUp() {
      searchClient = jiraClient.getSearchClient();
      issueClient = jiraClient.getIssueClient();
   }

   public void signIssueAsManipulated( URI issueUri ) {
      IssueRestClient issueClient = jiraClient.getIssueClient();
      URI commentUri = UriBuilder.fromUri( issueUri ).path( "comment" ).build();
      try{
         issueClient.addComment( commentUri, Comment.valueOf( COMMENT_TEXT ) );
      }catch( Throwable e ){
         e.printStackTrace();
      }
   }

   public void tearDown() {}

   // Properties
   public String getUser() {
      return userName;
   }

   // Protected, private helper methods
   private void configureHttpClientContext() {
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      UsernamePasswordCredentials credentials = new UsernamePasswordCredentials( userName, password );
      AuthScope authtehnticationScope = new AuthScope( AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM );
      credentialsProvider.setCredentials( authtehnticationScope, credentials );
      HttpClientContext context = HttpClientContext.create();
      context.setCredentialsProvider( credentialsProvider );
   }

   private Transition determineTransitionByName( Iterable<Transition> transitions, String transitionName ) {
      for( Transition transition : transitions ){
         if( transition.getName().equals( transitionName ) ){
            return transition;
         }
      }
      return null;
   }

   private void instantiateHttpClient() {
      HttpComponentsClientHttpRequestFactory httpClientFactory = new HttpComponentsClientHttpRequestFactory();
      httpClient = new RestTemplate( httpClientFactory );
      configureHttpClientContext();
   }

   private void instantiateJiraRestClient() {
      URI serverUri = URI.create( serverUriString );
      try{
         AsynchronousJiraRestClientFactory clientFactory = new AsynchronousJiraRestClientFactory();
         jiraClient = clientFactory.createWithBasicHttpAuthentication( serverUri, userName, password );
      }catch( Exception e ){
         logger.error( "Couldn't instantiate the Jira REST client.", e );
      }
   }
   
   private void performTransition( Issue subjectIssue, String transitionName, Collection<FieldInput> fieldInputs ) throws JiraAdapterException{
      final Iterable<Transition> transitions = issueClient.getTransitions( subjectIssue.getTransitionsUri() ).claim();
      final Transition transition = determineTransitionByName( transitions, transitionName );

      if( transition != null ){
         Promise<Void> promise = null;
         try{
            TransitionInput transitionInput = new TransitionInput( transition.getId(), fieldInputs, Comment.valueOf( TRANSITION_COMMENT ));
            promise = issueClient.transition( subjectIssue.getTransitionsUri(), transitionInput );
            waitForPromisIsDone( promise );
         }catch( Exception e ){
            throw new JiraAdapterException( transitionName, e );
         }
         
      }else throw new JiraAdapterTransitionNotFoundException( transitionName, subjectIssue );
   }

   private void waitForPromisIsDone( Promise<Void> promise ) throws InterruptedException {
      waitForFinish: for( int i = 0; i < 5000; i++ ){
         if( !(promise.isDone() || promise.isCancelled()) ){
           Thread.sleep( 50 );
           continue waitForFinish;
         }
      }
   }
}
