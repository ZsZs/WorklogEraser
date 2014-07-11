package com.kn.jira.worklogeraser.domain;

import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PdmAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PersonInPdm;

public class ServiceInvocationMatchingStrategy extends EmployeeMatchingStrategy {

   public ServiceInvocationMatchingStrategy( final AnonymizationActionLogger actionLogger, final JiraAdapter jiraAdapter, final PdmAdapter pdmAdapter ) {
      super( actionLogger, jiraAdapter, pdmAdapter );
   }

   @Override
   public Boolean isWorklogEffected( Worklog worklog ) {
      BasicUser jiraUserSummary = worklog.getAuthor();
      User jiraUser = jiraAdapter.findUserDetails( jiraUserSummary );
      programLogger.debug( "Found worklog: '" + worklog.getComment() + "' of person: " + jiraUser.getEmailAddress() );
      PersonInPdm person = findPersonInPdm( jiraUser );
      return verifyPerformer( person );
   }

   protected PersonInPdm findPersonInPdm( User jiraUser ) {
      PersonInPdm person = pdmAdapter.findPersonByEmail( jiraUser.getEmailAddress() );
      return person;
   }
}
