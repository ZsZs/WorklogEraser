package com.kn.jira.worklogeraser.domain;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapterException;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapterTransitionNotFoundException;

public class WorklogAnonymizator implements ApplicationContextAware{
   public static final String BEAN_NAME_ACTION_LOGGER = "anonymizationActionLog";
   public static final String BEAN_NAME_ANONYMIZATION_STRATEGY = "anonymizationStrategy";
   public static final String BEAN_NAME_CONFIG_PROPERTIES = "configProperties";
   public static final String BEAN_NAME_EMPLOYEE_MATCHING_STRATEGY = "employeeMatchingStrategy";
   public static final String BEAN_NAME_JIRA_ADAPTER = "jiraAdapter";
   public static final String BEAN_NAME_PDM_ADAPTER = "pdmServiceClient";
   public static final String BEAN_NAME_WORKLOG_ANONYMIZATOR = "worklogAnonymizator";
   public static final String CONF_ANONYM_USER = "WorklogAnonymizator.jira.anonym.user.name";
   public static final String CONF_CHANGED_ISSUE_NOTE = "WorklogAnonymizator.jira.changed.issue.note";
   public static final String CONF_OBSOLESCENCE_TIME_PERIOD = "WorklogAnonymizator.jira.obsolescenceTimePeriod";
   public static final String CONF_CLOSED_STATE = "WorklogAnonymizator.jira.closed.issue.status.name";
   public static final String CONF_JIRA_USER_PASSWORD = "WorklogAnonymizator.jira.user.password";
   public static final String CONF_JIRA_USER_NAME = "WorklogAnonymizator.jira.user.name";
   public static final String DEFAULT_APPLICATION_CONFIGURATION = "file:Configuration/BeanContainerDefinition.xml";
   public static final String DATE_FORMAT = "yyyy/MM/dd";
   public static final String ISSUE_STATUS_DEFAULT = "Closed";
   private AnonymizationActionLogger actionLogger;
   private AnonymizationStrategy anonymizationStrategy;
   private List<BasicProject> allJiraProjects = Lists.newArrayList();
   private ApplicationContext applicationContext;
   private String closedIssueStatusName = ISSUE_STATUS_DEFAULT;
   private Properties configurationProperties;
   private SimpleDateFormat dateFormat;
   private EmployeeMatchingStrategy employeeMatchingStrategy;
   private JiraAdapter jiraAdapter;
   private Logger programLogger = LoggerFactory.getLogger( this.getClass() );
   private Date obsolatedWorklogDate;
   private Map<URI, Issue> subjectIssues = Maps.newHashMap();
   private List<Worklog> subjectWorklogs = Lists.newArrayList();
   private JiraQuery worklogQueryStatement;

   // Main
   public static void main(String [ ] args){
      PropertyConfigurator.configure( "Configuration/log4j.properties" );
      
      @SuppressWarnings( "resource" )
      ApplicationContext applicationContext = new ClassPathXmlApplicationContext( DEFAULT_APPLICATION_CONFIGURATION );
      WorklogAnonymizator worklogEraser = applicationContext.getBean( BEAN_NAME_WORKLOG_ANONYMIZATOR, WorklogAnonymizator.class );
      worklogEraser.perform();
      System.exit( 0 );
   }   
   
   // Constructors
   public WorklogAnonymizator( Properties configurationProperties ) {
      this.configurationProperties = configurationProperties;
   }

   // Public accessor and mutator methods
   public void perform() {
      programLogger.info( "Performing jira worklog erasure process started" );
      try{
         defineDateFormat();
         determineClosedIssueStatusName();
         calculateObsolatedWorklogDate();
         setUpActionsLog();
         setUpConnectedSystemsAdapters();
         actionLogger.executionStart( obsolatedWorklogDate );
         collectAllProjects();
         collectAllSubjectIssues();
         analyseWorklogs();
      }catch( WorklogEraserException | SAXException | IOException | ParserConfigurationException | TransformerException | RuntimeException e ){
//      }catch( Exception e ){
         programLogger.error( "Error occured while performing jira worklog erasure.", e );
      }finally{
         try{
            actionLogger.executionEnd();
            actionLogger.tearDown();
         }catch( TransformerException | IOException e ){
            programLogger.error( "Actions Logger coudn't tear down.", e );
         }finally{
            programLogger.info( "Performing jira worklog erasure process finished." );
         }
      }
   }

   // Properties
   public Date getObsolatedWorklogDate() { return obsolatedWorklogDate; }
   public JiraQuery getWorklogQueryStatement() { return worklogQueryStatement; }
   @Override public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException { 
      this.applicationContext = applicationContext; 
      employeeMatchingStrategy = applicationContext.getBean( BEAN_NAME_EMPLOYEE_MATCHING_STRATEGY, EmployeeMatchingStrategy.class );
      anonymizationStrategy = applicationContext.getBean( BEAN_NAME_ANONYMIZATION_STRATEGY, AnonymizationStrategy.class );
   }

   // Protected, private helper methods
   protected void analyseWorklogs() throws JiraAdapterException {
      programLogger.info( "Collecting all worklogs of all subject issues." );
      for( Entry<URI, Issue> jiraIssueEntry : subjectIssues.entrySet() ){
         Issue jiraIssue = jiraIssueEntry.getValue();
         actionLogger.considerIssue( jiraIssue.getKey(), jiraIssue.getStatus().getName() );
         List<Worklog> worklogsAssociatedToIssue = Lists.newArrayList( jiraIssue.getWorklogs() );
         List<Worklog> subjectWorklogsOfIssue = Lists.newArrayList();
         for( Worklog worklog : worklogsAssociatedToIssue ){
            if( employeeMatchingStrategy.isWorklogEffected( worklog )){
               subjectWorklogsOfIssue.add( worklog );
               subjectWorklogs.add( worklog );
            }
         }
         
         if( subjectWorklogsOfIssue.size() > 0 ){
            performAnonymizationOnIssue( jiraIssue, subjectWorklogsOfIssue );
         }
         
      }
      programLogger.info( "Number of subject worklogs found is: " + subjectWorklogs.size() );
   }
   
   protected void defineDateFormat(){
      dateFormat = new SimpleDateFormat( DATE_FORMAT );
   }
   
   protected void determineClosedIssueStatusName(){
      String definedInConfigurationProperties = configurationProperties.getProperty( "" ); 
      if( definedInConfigurationProperties != null ) closedIssueStatusName = definedInConfigurationProperties;
   }
   
   protected void calculateObsolatedWorklogDate(){
      Integer obsolescenceTimePeriod = Integer.parseInt( configurationProperties.getProperty( CONF_OBSOLESCENCE_TIME_PERIOD ));
      DateTime currentDate = new DateTime();
      obsolatedWorklogDate = currentDate.minusDays( obsolescenceTimePeriod ).withTimeAtStartOfDay().toDate();
      
      programLogger.info( "Obsolated worklog date determined as: " + obsolatedWorklogDate.toString() );
   }
   
   protected void collectAllProjects(){
      programLogger.info( "Collecting all projects from Jira." );
      allJiraProjects = jiraAdapter.findAllProjects();
      programLogger.info( "Number of Jira Projects found is: " + allJiraProjects.size() );
   }
   
   protected void collectAllSubjectIssues() throws JiraAdapterException {
      programLogger.info( "Collecting all subject issues from Jira." );
      for( BasicProject jiraProject : allJiraProjects ){
         actionLogger.considerProject( jiraProject.getName() );
         programLogger.info( "Considering jira project: " + jiraProject.getName());
         try{
            String formattedDate = dateFormat.format( obsolatedWorklogDate );
            List<Issue> foundIssuesInProject = jiraAdapter.findClosedObsolatedIssues( jiraProject.getKey(), closedIssueStatusName, formattedDate );
            for( Issue issue : foundIssuesInProject ){
               subjectIssues.put( issue.getSelf(), issue );
            }
         }catch( RestClientException e ){
            actionLogger.projectAccessRestriction( jiraAdapter.getUser() );;
         }
      }
      programLogger.info( "Number of subject issues found is: " + subjectIssues.size() );
   }
   
   protected void performAnonymizationOnIssue( final Issue issue, final List<Worklog> worklogs ) throws JiraAdapterException {
      Issue subjectIssue = issue;
      try{
         jiraAdapter.reopenIssue( subjectIssue );
         subjectIssue = reloadIssue( subjectIssue );
         if( subjectIssue.getStatus().getName().toUpperCase().equals( JiraAdapter.REOPENED_STATUS_NAME )){
            for( Worklog worklog : worklogs ){
               performAnonymizationOnWorklog( worklog );
            }
            jiraAdapter.closeIssue( subjectIssue );
            signIssueAsManipulated( subjectIssue.getSelf() );
         }else{
            programLogger.info( "Issue: '" + subjectIssue.getKey() + "' can't be reopened as the status remained: '" + subjectIssue.getStatus().getName() + "'." );
         }
      }catch( JiraAdapterTransitionNotFoundException exception ){
         programLogger.error( "Issue: " + subjectIssue.getKey() + " doesn't have 'Reopen' transtion in the current state.", exception );
         throw exception;
      }catch( JiraAdapterException exception ){
         //programLogger.error( person.getEmailAddress() + "'s worklog: " + worklog.getComment() + " to issue: " + subjectIssue.getKey() + " could not be deleted.", exception );
         throw exception;
      }
   }

   protected void performAnonymizationOnWorklog( final Worklog worklog ) throws JiraAdapterException{
      anonymizationStrategy.perform( worklog );
      User jiraUser = jiraAdapter.findUserDetails( worklog.getAuthor() );
      actionLogger.worklogAnonymated( worklog.getIssueUri().toString(), jiraUser.getEmailAddress(), worklog.getComment() );
   }

   protected Issue reloadIssue( Issue subjectIssue ) {
      return jiraAdapter.findIssueByKey( subjectIssue.getKey() );
   }

   protected void setUpActionsLog() throws SAXException, IOException, ParserConfigurationException, TransformerException {
      actionLogger = applicationContext.getBean( BEAN_NAME_ACTION_LOGGER, AnonymizationActionLogger.class );
      actionLogger.setUp();
      programLogger.info( "Action logger is configured to log into: " + actionLogger.getLogPath() );
   }   
   
   protected void setUpConnectedSystemsAdapters(){
      jiraAdapter = applicationContext.getBean( BEAN_NAME_JIRA_ADAPTER, JiraAdapter.class );
      jiraAdapter.setUp();
      programLogger.info( "Jira and PDM++ web service adapters are configured." );
   }
   
   protected void signIssueAsManipulated( URI issueUri ) {
      jiraAdapter.signIssueAsManipulated( issueUri );
      actionLogger.worklogWasManipulated();
   }   
}
