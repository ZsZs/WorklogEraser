package com.kn.jira.worklogeraser.domain;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.kn.jira.worklogeraser.pdmadapter.PersonInPdm;

public class ServiceInvocationMatchingStrategyTest extends EmployeeMatchingStrategyTest{
   //Test methods
   @Test
   public void performErase_whenWorklogPerformerIsInPdm_veryfiesPerson() {
      employeeMatchingStrategy.perforErase( subjectWorklogs );
   
      for( Worklog worklog : subjectWorklogs ){
         User jiraUser = testConfiguration.determineJiraUser( worklog );
         PersonInPdm person = pdmAdapter.findPersonByEmail( jiraUser.getEmailAddress() );
         if( person != null ){
            verify( person, atLeastOnce() ).getCountryCode();
            
         }
      }
   }
   
}
