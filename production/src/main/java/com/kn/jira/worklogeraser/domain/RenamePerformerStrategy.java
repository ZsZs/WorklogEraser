package com.kn.jira.worklogeraser.domain;

import java.util.Properties;

import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;

public class RenamePerformerStrategy extends AnonymizationStrategy {
   private String anonymousName; 
   
   //Constructors
   public RenamePerformerStrategy( final Properties configurationProperties, JiraAdapter jiraAdapter ) {
      super( configurationProperties, jiraAdapter );
      determineAnonymousName();
   }

   //Public accessors and mutators
   @Override
   public void perform( Worklog worklog ) {
      jiraAdapter.replaceWorklogPerformer( worklog, anonymousName );
   }
   
   //Proptected, private helper methods
   private void determineAnonymousName() {
      this.anonymousName = configurationProperties.getProperty( WorklogAnonymizator.CONF_ANONYM_USER );      
   }

}
