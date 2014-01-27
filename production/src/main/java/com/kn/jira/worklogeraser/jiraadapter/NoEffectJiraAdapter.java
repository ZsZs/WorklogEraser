package com.kn.jira.worklogeraser.jiraadapter;

import java.net.URI;

import com.atlassian.jira.rest.client.domain.Worklog;

public class NoEffectJiraAdapter extends JiraAdapter{

   public NoEffectJiraAdapter( final String serverUriString, final String userName, final String password ) {
      super( serverUriString, userName, password );
   }

   @Override
   public void deleteWorklog( Worklog worklog ) {
      // No operation
   }
   
   @Override
   public void signIssueAsManipulated( URI issueUri ){
      // No operation
   }
}
