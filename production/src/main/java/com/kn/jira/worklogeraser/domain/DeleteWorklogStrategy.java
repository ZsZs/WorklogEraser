package com.kn.jira.worklogeraser.domain;

import java.util.Properties;

import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapterDeleteWorklogException;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapterException;

public class DeleteWorklogStrategy extends AnonymizationStrategy {
   
   //Constructors
   public DeleteWorklogStrategy( Properties configurationProperties, JiraAdapter jiraAdapter ) {
      super( configurationProperties, jiraAdapter );
   }

   //Public accessors and mutators
   @Override
   public void perform( Worklog worklog ) {
      deleteWorklog( worklog );
   }
   
   //Protected, private helper methods
   protected void deleteWorklog( Worklog worklog ){
      User jiraUser = jiraAdapter.findUserDetails( worklog.getAuthor() );
      try{
         jiraAdapter.deleteWorklog( worklog );
         //actionLogger.worklogDeleted( worklog.getIssueUri().toString(), person.getEmailAddress(), worklog.getComment() );
         programLogger.info( jiraUser.getEmailAddress() + "'s worklog: " + worklog.getComment() + " of issue: " + worklog.getIssueUri().toString() + " is deleted.");
      }catch( JiraAdapterDeleteWorklogException e ){
         programLogger.error( jiraUser.getEmailAddress() + "'s worklog: " + worklog.getComment() + " of issue: " + worklog.getIssueUri().toString() + " could not be deleted.", e );
      }catch( JiraAdapterException e ){
      }
   }

}
