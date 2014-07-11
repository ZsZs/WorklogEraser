package com.kn.jira.worklogeraser.domain;

import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PdmAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PersonInPdm;

public class InMemoryEmployeeMatchingStrategy extends EmployeeMatchingStrategy{

   public InMemoryEmployeeMatchingStrategy( final AnonymizationActionLogger actionLogger, final JiraAdapter jiraAdapter, final PdmAdapter pdmAdapter ) {
      super( actionLogger, jiraAdapter, pdmAdapter );
   }

   @Override
   public Boolean isWorklogEffected( Worklog worklog ) {
      return null;
   }

   @Override
   protected PersonInPdm findPersonInPdm( User jiraUser ) {
      return null;
   }

}
