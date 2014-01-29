package com.kn.jira.worklogeraser.sharedresources;

import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.kn.jira.worklogeraser.domain.EmployeeMatchingStrategy;
import com.kn.jira.worklogeraser.domain.EraseActionLogger;
import com.kn.jira.worklogeraser.domain.WorklogEraser;
import com.kn.jira.worklogeraser.jiraadapter.JiraAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PdmPersonServiceClient;

public class TestConfigurationFixture {
   protected static final String BEAN_CONTAINER_DEFINITION_XML = "classpath:BeanContainerDefinition.xml";
   protected EraseActionLogger actionLog;
   protected ApplicationContext applicationContext;
   protected Properties configurationProperties;
   protected EmployeeMatchingStrategy employeeMatchingStrategy;
   protected JiraAdapter jiraAdapter;
   protected PdmPersonServiceClient pdmServiceClient;
   protected WorklogEraser worklogEraser;

   public void setUp() {
      applicationContext = new ClassPathXmlApplicationContext( BEAN_CONTAINER_DEFINITION_XML );
      
      retrieveBeans();
   }

   public void tearDown() {
   }

   public EraseActionLogger getActionLog() { return actionLog; }
   public ApplicationContext getApplicationContext() { return applicationContext; }
   public Properties getConfigurationProperties() { return configurationProperties; }
   public EmployeeMatchingStrategy getEmployeeMatchingStrategy() { return employeeMatchingStrategy; }
   public JiraAdapter getJiraAdapter() { return jiraAdapter; }
   public PdmPersonServiceClient getPdmPersonServiceClient() { return pdmServiceClient; }
   public WorklogEraser getWorklogEraser() { return worklogEraser; }

   //Protected, private helper methods
   protected void retrieveBeans() {
      actionLog = applicationContext.getBean( "eraseActionLog", EraseActionLogger.class );
      worklogEraser = applicationContext.getBean( "worklogEraser", WorklogEraser.class );
      configurationProperties = applicationContext.getBean( "configProperties", Properties.class );
      jiraAdapter = applicationContext.getBean( "jiraAdapter", JiraAdapter.class );
      jiraAdapter.setUp();
      pdmServiceClient = applicationContext.getBean( "pdmServiceClient", PdmPersonServiceClient.class );
      employeeMatchingStrategy = applicationContext.getBean( "employeeMatchingStrategy", EmployeeMatchingStrategy.class );
   }
}
