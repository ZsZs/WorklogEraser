package com.kn.jira.worklogeraser.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PdmAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PersonInPdm;

public class TestEmployeeMatchingStrategy extends EmployeeMatchingStrategy {
   private static final String GERMAN_EMPLOYEE_EMAIL = "german.employee@kh.com";

   public TestEmployeeMatchingStrategy( AnonymizationActionLogger actionLogger, final JiraAdapter jiraAdapter, final PdmAdapter pdmAdapter ) {
      super( actionLogger, jiraAdapter, pdmAdapter );
   }

   @Override
   public Boolean isWorklogEffected( Worklog worklog ) {
      BasicUser jiraUserSummary = worklog.getAuthor();
      User jiraUser = jiraAdapter.findUserDetails( jiraUserSummary );
      
      return jiraUser.getEmailAddress().equals( GERMAN_EMPLOYEE_EMAIL );
   }

   @Override
   protected PersonInPdm findPersonInPdm( User jiraUser ) {
      if( jiraUser.getEmailAddress().equals( GERMAN_EMPLOYEE_EMAIL )){
         PersonInPdm germanPerson = mock( PersonInPdm.class );
         when( germanPerson.getCountryCode() ).thenReturn( EXPECTED_COUNTRY_CODE );
         when( germanPerson.getCompanyName() ).thenReturn( EXPECTED_COMPANY_NAME );
         when( germanPerson.getIsStaffMember() ).thenReturn( EXPECTED_STUFF_MEMBER_VALUE );
         return germanPerson;
      }else return null;
   }

}
