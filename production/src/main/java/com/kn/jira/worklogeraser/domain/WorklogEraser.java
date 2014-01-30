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

import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapterException;

public class WorklogEraser implements ApplicationContextAware{
   public static final String DEFAULT_APPLICATION_CONFIGURATION = "file:Configuration/BeanContainerDefinition.xml";
   public static final String DATE_FORMAT = "yyyy/MM/dd";
   public static final String ISSUE_STATUS_DEFAULT = "Closed";
   public static final String OBSOLESCENCE_TIME_PERIOD = "worklogeraser.obsolescenceTimePeriod";
   public static final String CLOSED_STATE_PROPERTY_KEY = "worklogeraser.closed.issue.status.name";
   private EraseActionLogger actionLogger;
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
      WorklogEraser worklogEraser = applicationContext.getBean( "worklogEraser", WorklogEraser.class );
      worklogEraser.perform();
      System.exit( 0 );
   }   
   
   // Constructors
   public WorklogEraser( Properties configurationProperties ) {
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
         collectAllSubjectWorklogsAndPerformErase();
      }catch( WorklogEraserException | SAXException | IOException | ParserConfigurationException | TransformerException e ){
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
      employeeMatchingStrategy = applicationContext.getBean( "employeeMatchingStrategy", EmployeeMatchingStrategy.class );
   }

   // Protected, private helper methods
   protected void defineDateFormat(){
      dateFormat = new SimpleDateFormat( DATE_FORMAT );
   }
   
   protected void determineClosedIssueStatusName(){
      String definedInConfigurationProperties = configurationProperties.getProperty( "" ); 
      if( definedInConfigurationProperties != null ) closedIssueStatusName = definedInConfigurationProperties;
   }
   
   protected void calculateObsolatedWorklogDate(){
      Integer obsolescenceTimePeriod = Integer.parseInt( configurationProperties.getProperty( OBSOLESCENCE_TIME_PERIOD ));
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
   
   protected void collectAllSubjectWorklogsAndPerformErase() throws JiraAdapterException {
      programLogger.info( "Collecting all worklogs of all subject issues." );
      for( Entry<URI, Issue> jiraIssueEntry : subjectIssues.entrySet() ){
         Issue jiraIssue = jiraIssueEntry.getValue();
         actionLogger.considerIssue( jiraIssue.getKey(), jiraIssue.getStatus().getName() );
         List<Worklog> worklogsAssociatedToIssue = Lists.newArrayList( jiraIssue.getWorklogs() );
         performErase( worklogsAssociatedToIssue );
         subjectWorklogs.addAll( worklogsAssociatedToIssue );
      }
      programLogger.info( "Number of subject worklogs found is: " + subjectWorklogs.size() );
   }

   protected void performErase( List<Worklog> subjectWorklogs ) throws JiraAdapterException{
      employeeMatchingStrategy.perforErase( subjectIssues, subjectWorklogs );
   }
   
   protected void setUpActionsLog() throws SAXException, IOException, ParserConfigurationException, TransformerException {
      actionLogger = applicationContext.getBean( "eraseActionLog", EraseActionLogger.class );
      actionLogger.setUp();
      programLogger.info( "Action logger is configured to log into: " + actionLogger.getLogPath() );
   }   
   
   protected void setUpConnectedSystemsAdapters(){
      jiraAdapter = applicationContext.getBean( "jiraAdapter", JiraAdapter.class );
      jiraAdapter.setUp();
      programLogger.info( "Jira and PDM++ web service adapters are configured." );
   }
}
