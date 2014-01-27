package com.kn.jira.worklogeraser.domain;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class PerformEraseAndAnalyzeActionLog {
   protected static final String BEAN_CONTAINER_DEFINITION_XML = "classpath:BeanContainerDefinition.xml";
   private ApplicationContext applicationContext;
   private WorklogEraser worklogEraser;
   private EraseActionLogger actionLogger;
   private XPath xPath;
   
   @Before public void beforeEachTest(){
      applicationContext = new ClassPathXmlApplicationContext( BEAN_CONTAINER_DEFINITION_XML );
      worklogEraser = applicationContext.getBean( "worklogEraser", WorklogEraser.class );
      actionLogger = applicationContext.getBean( "eraseActionLog", EraseActionLogger.class );
      
      xPath = XPathFactory.newInstance().newXPath();
   }
   
   @Test public void performEraseAndAnalyzeActionLog() throws XPathExpressionException{
      //EXECUTE:
      worklogEraser.perform();

      //VERIFY:
      Document actionLog = actionLogger.getLog();
      NodeList deletitionNodes = (NodeList) xPath.evaluate( "/worklogEraser/execution/subjectProject/subjectIssue/worklogDeleted", actionLog.getDocumentElement(), XPathConstants.NODESET );

      assertThat( deletitionNodes.getLength(), greaterThan( 0 ));
   }
}
