package com.kn.jira.worklogeraser.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlassian.jira.rest.client.api.domain.User;
import com.kn.jira.worklogeraser.pdmadapter.PersonInPdm;

public class TestEmployeeMatchingStrategy extends EmployeeMatchingStrategy {

   public TestEmployeeMatchingStrategy( AnonymizationActionLogger actionLogger ) {
      super( actionLogger );
   }

   @Override
   protected PersonInPdm findPersonInPdm( User jiraUser ) {
      if( jiraUser.getEmailAddress().equals( "german.employee@kh.com" )){
         PersonInPdm germanPerson = mock( PersonInPdm.class );
         when( germanPerson.getCountryCode() ).thenReturn( EXPECTED_COUNTRY_CODE );
         when( germanPerson.getCompanyName() ).thenReturn( EXPECTED_COMPANY_NAME );
         when( germanPerson.getIsStaffMember() ).thenReturn( EXPECTED_STUFF_MEMBER_VALUE );
         return germanPerson;
      }else return null;
   }

}
