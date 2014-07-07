package com.kn.jira.worklogeraser.domain;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.atlassian.jira.rest.client.api.domain.BasicProject;

public class WorklogAnonymizatorTest {
   public static final String DEFAULT_APPLICATION_CONFIGURATION = "file:src/test/resources/BeanContainerDefinition.xml";
   private WorklogAnonymizator worklogEraser;
   
   @Before public void beforeEachTest(){
      PropertyConfigurator.configure( "src/test/resources/log4j.properties" );
      
      @SuppressWarnings( "resource" )
      ApplicationContext applicationContext = new ClassPathXmlApplicationContext( DEFAULT_APPLICATION_CONFIGURATION );
      worklogEraser = applicationContext.getBean( WorklogAnonymizator.BEAN_NAME_WORKLOG_ANONYMIZATOR, WorklogAnonymizator.class );
   }
   
   @Test public void perform_InvestigatesAllProjects(){
      worklogEraser.perform();
      
      @SuppressWarnings( "unchecked" )
      List<BasicProject> allJiraProjects = (List<BasicProject>) Whitebox.getInternalState( worklogEraser, "allJiraProjects" );
      assertThat( allJiraProjects.size(), greaterThan( 0 ));
   }
}
