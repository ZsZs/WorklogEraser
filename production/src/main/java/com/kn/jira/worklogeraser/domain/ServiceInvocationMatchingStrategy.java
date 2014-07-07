package com.kn.jira.worklogeraser.domain;

import com.atlassian.jira.rest.client.api.domain.User;
import com.kn.jira.worklogeraser.pdmadapter.PersonInPdm;

public class ServiceInvocationMatchingStrategy extends EmployeeMatchingStrategy {

   public ServiceInvocationMatchingStrategy( AnonymizationActionLogger actionLogger ) {
      super( actionLogger );
   }

   protected PersonInPdm findPersonInPdm( User jiraUser ) {
      PersonInPdm person = pdmAdapter.findPersonByEmail( jiraUser.getEmailAddress() );
      return person;
   }
}
