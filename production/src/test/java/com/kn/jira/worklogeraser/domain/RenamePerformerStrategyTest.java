package com.kn.jira.worklogeraser.domain;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Test;

import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;

public class RenamePerformerStrategyTest extends AnonymizationStrategyTest {
   private static final String ANONYMOUS_NAME = "knd.employee";

   @Override
   public void beforeEachTests() {
      super.beforeEachTests();
   }

   @Override
   protected AnonymizationStrategy instantiateStrategy( final Properties configurationProperties, final JiraAdapter jiraAdapter ) {
      when( configurationProperties.getProperty( WorklogAnonymizator.CONF_ANONYM_USER )).thenReturn( ANONYMOUS_NAME );
      return new RenamePerformerStrategy( configurationProperties, jiraAdapter );
   }

   @Test public void performAnonymization_invokesJiraAdapter(){
      anonymizationStrategy.perform( worklog );
      verify( jiraAdapter ).replaceWorklogPerformer( worklog, ANONYMOUS_NAME );
   }
}
