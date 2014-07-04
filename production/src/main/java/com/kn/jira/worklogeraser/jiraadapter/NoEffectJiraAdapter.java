package com.kn.jira.worklogeraser.jiraadapter;

import java.net.URI;
import java.util.Properties;

import com.atlassian.jira.rest.client.domain.Worklog;

public class NoEffectJiraAdapter extends JiraAdapter{

   public NoEffectJiraAdapter( final Properties configurationProperties, final String serverUriString ) {
      super( configurationProperties, serverUriString );
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
