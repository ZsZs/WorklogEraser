package com.kn.jira.worklogeraser.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Properties;

import org.junit.Before;

import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;

public abstract class AnonymizationStrategyTest {
   protected AnonymizationStrategy anonymizationStrategy;
   protected Properties configurationProperties;
   protected JiraAdapter jiraAdapter;
   protected Worklog worklog;
   
   @Before public void beforeEachTests(){
      User worklogAuthor = mock( User.class );
      when( worklogAuthor.getEmailAddress() ).thenReturn( "worklog.author@huehne-nagel.com" );
      BasicUser worklogAuthorSummary = mock( BasicUser.class );
      
      worklog = mock( Worklog.class );
      when( worklog.getAuthor() ).thenReturn( worklogAuthorSummary );
      when( worklog.getComment() ).thenReturn( "Was working hard...." );
      when( worklog.getIssueUri() ).thenReturn( URI.create( "http://localhost:2990/jira" ));
      
      configurationProperties = mock( Properties.class );
      jiraAdapter = mock( JiraAdapter.class );
      when( jiraAdapter.findUserDetails( worklogAuthorSummary )).thenReturn( worklogAuthor );
      anonymizationStrategy = instantiateStrategy( configurationProperties, jiraAdapter );
   }
   
   //Protected, private test methods
   protected abstract AnonymizationStrategy instantiateStrategy( final Properties configurationProperties, final JiraAdapter jiraAdapter );
}
