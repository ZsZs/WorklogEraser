package com.kn.jira.worklogeraser.domain;

import static org.mockito.Mockito.verify;

import java.util.Properties;

import org.junit.Test;

import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapterException;

public class DeleteWorklogStrategyTest extends AnonymizationStrategyTest {

   public DeleteWorklogStrategyTest() {
   }

   @Test public void performAnonymization_invokesJiraAdapter() throws JiraAdapterException{
      anonymizationStrategy.perform( worklog );
      
      verify( jiraAdapter ).deleteWorklog( worklog );
   }
   
   //Protected, private helper methods.
   @Override
   protected AnonymizationStrategy instantiateStrategy( Properties configurationProperties, JiraAdapter jiraAdapter ) {
      return new DeleteWorklogStrategy( configurationProperties, jiraAdapter );
   }

}
