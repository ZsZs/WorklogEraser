package com.kn.jira.worklogeraser.domain;

import com.atlassian.jira.rest.client.api.domain.User;
import com.kn.jira.worklogeraser.pdmadapter.PersonInPdm;

public class InMemoryEmployeeMatchingStrategy extends EmployeeMatchingStrategy{

   public InMemoryEmployeeMatchingStrategy( AnonymizationActionLogger actionLogger ) {
      super( actionLogger );
   }

   @Override
   protected PersonInPdm findPersonInPdm( User jiraUser ) {
      return null;
   }

}
