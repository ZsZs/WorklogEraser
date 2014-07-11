package com.kn.jira.worklogeraser.domain;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Worklog;
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
   protected final AnonymizationActionLogger actionLogger;
   protected ApplicationContext applicationContext;
   protected final JiraAdapter jiraAdapter;
   protected final PdmAdapter pdmAdapter;
   protected Map<URI, Issue> subjectIssues;
   protected List<Worklog> subjectWorklogs;
   
   public EmployeeMatchingStrategy( final AnonymizationActionLogger actionLogger, final JiraAdapter jiraAdapter, final PdmAdapter pdmAdapter ){
      this.actionLogger = actionLogger;      
      this.jiraAdapter = jiraAdapter;
      this.pdmAdapter = pdmAdapter;
   }
   
   public abstract Boolean isWorklogEffected( Worklog worklog );
   
   //Properties
   @Override
   public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
      this.applicationContext = applicationContext;
   }
   
   //Protected, private helper methods
   protected abstract PersonInPdm findPersonInPdm( User jiraUser );
   
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
