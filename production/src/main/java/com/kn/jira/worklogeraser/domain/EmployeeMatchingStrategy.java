package com.kn.jira.worklogeraser.domain;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapterDeleteWorklogException;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapterException;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapterTransitionNotFoundException;
import com.kn.jira.worklogeraser.pdmadapter.PdmAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PersonInPdm;
   
public abstract class EmployeeMatchingStrategy implements ApplicationContextAware{
   public static final String EXPECTED_COUNTRY_CODE = "DE";
   public static final String EXPECTED_COMPANY_NAME = "K\u00fchne + Nagel (AG & Co.) KG";
   public static final String EXPECTED_STUFF_MEMBER_VALUE = "Y";
   protected final Logger programLogger = LoggerFactory.getLogger( this.getClass() );
   protected AnonymizationActionLogger actionLogger;
   protected ApplicationContext applicationContext;
   protected JiraAdapter jiraAdapter;
   protected PdmAdapter pdmAdapter;
   protected Map<URI, Issue> subjectIssues;
   protected List<Worklog> subjectWorklogs;
   
   public EmployeeMatchingStrategy( AnonymizationActionLogger actionLogger ){
      this.actionLogger = actionLogger;      
   }
   
   public void perforErase( Map<URI, Issue> subjectIssues, List<Worklog> subjectWorklogs ) throws JiraAdapterException{
      instantiateAdapters();
      this.subjectIssues = subjectIssues;
      this.subjectWorklogs = subjectWorklogs;
      checkAllWorklogs();
   }
   
   //Properties
   @Override
   public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
      this.applicationContext = applicationContext;
   }
   
   //Protected, private helper methods
   protected void deleteWorklog( Worklog worklog, PersonInPdm person ){
      try{
         jiraAdapter.deleteWorklog( worklog );
         actionLogger.worklogDeleted( worklog.getIssueUri().toString(), person.getEmailAddress(), worklog.getComment() );
         programLogger.info( person.getEmailAddress() + "'s worklog: " + worklog.getComment() + " of issue: " + worklog.getIssueUri().toString() + " is deleted.");
      }catch( JiraAdapterDeleteWorklogException e ){
         try{
            reopenDeleteCloseIssue( worklog, person );
         }catch( JiraAdapterException e1 ){
            programLogger.error( person.getEmailAddress() + "'s worklog: " + worklog.getComment() + " of issue: " + worklog.getIssueUri().toString() + " could not be deleted.", e );
         }
      }catch( JiraAdapterException e ){
         programLogger.error( person.getEmailAddress() + "'s worklog: " + worklog.getComment() + " of issue: " + worklog.getIssueUri().toString() + " could not be deleted.", e );
      }
   }

   protected void checkAllWorklogs() throws JiraAdapterException{
      URI manipulatedIssueUri = null;
      for( Worklog jiraWorklog : subjectWorklogs ){
         BasicUser jiraUserSummary = jiraWorklog.getAuthor();
         User jiraUser = jiraAdapter.findUserDetails( jiraUserSummary );
         programLogger.debug( "Found worklog: '" + jiraWorklog.getComment() + "' of person: " + jiraUser.getEmailAddress() );
         PersonInPdm person = findPersonInPdm( jiraUser );
         if( verifyPerformer( person ) ){
            deleteWorklog( jiraWorklog, person );
            manipulatedIssueUri = jiraWorklog.getIssueUri();
         }
      }
      
      if( manipulatedIssueUri != null ) signIssueAsManipulated( manipulatedIssueUri );
   }
   
   protected abstract PersonInPdm findPersonInPdm( User jiraUser );
   
   protected void instantiateAdapters(){
      pdmAdapter = applicationContext.getBean( "pdmAdapter", PdmAdapter.class );
      jiraAdapter = applicationContext.getBean( "jiraAdapter", JiraAdapter.class );
   }
   
   protected void reopenDeleteCloseIssue( Worklog worklog, PersonInPdm person ) throws JiraAdapterException {
      Issue subjectIssue = subjectIssues.get( worklog.getIssueUri() );
      try{
         jiraAdapter.reopenIssue( subjectIssue );
         subjectIssue = reloadIssue( subjectIssue );
         if( subjectIssue.getStatus().getName().toUpperCase().equals( JiraAdapter.REOPENED_STATUS_NAME )){
            deleteWorklog( worklog, person );
            jiraAdapter.closeIssue( subjectIssue );
         }else{
            programLogger.info( "Issue: '" + subjectIssue.getKey() + "' can't be reopened as the status remained: '" + subjectIssue.getStatus().getName() + "'." );
         }
      }catch( JiraAdapterTransitionNotFoundException exception ){
         programLogger.error( "Issue: " + worklog.getIssueUri().toString() + " doesn't have 'Reopen' transtion in the current state.", exception );
         throw exception;
      }catch( JiraAdapterException exception ){
         programLogger.error( person.getEmailAddress() + "'s worklog: " + worklog.getComment() + " to issue: " + worklog.getIssueUri().toString() + " could not be deleted.", exception );
         throw exception;
      }
   }
   
   protected Issue reloadIssue( Issue subjectIssue ) {
      return jiraAdapter.findIssueByKey( subjectIssue.getKey() );
   }
   
   protected void signIssueAsManipulated( URI issueUri ) {
      jiraAdapter.signIssueAsManipulated( issueUri );
      actionLogger.worklogWasManipulated();
   }

   protected Boolean verifyPerformer( PersonInPdm person ) {
      if( person != null ){
         programLogger.debug( "Verififying properties of person: " + person.getEmailAddress() + " in country: " + person.getCountryCode() );
         if( person.getCountryCode().equals( EXPECTED_COUNTRY_CODE ) &&
             person.getCompanyName().equals( EXPECTED_COMPANY_NAME ) &&
             person.getIsStaffMember().equals( EXPECTED_STUFF_MEMBER_VALUE )){ 
            return true; 
         }
         else{ return false; }
      }else return false;
   }
}
