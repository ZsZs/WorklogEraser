package com.kn.jira.worklogeraser.domain;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;

public abstract class AnonymizationStrategy {
   protected Properties configurationProperties;
   protected final JiraAdapter jiraAdapter;
   protected Logger programLogger = LoggerFactory.getLogger( AnonymizationStrategy.class );
   
   public AnonymizationStrategy( final Properties configurationProperties, final JiraAdapter jiraAdapter ){
      this.configurationProperties = configurationProperties;
      this.jiraAdapter = jiraAdapter;
   }
   
   //Public accessors and mutators
   public abstract void perform( Worklog worklog );
   
   //Protected, private helper methods

}
