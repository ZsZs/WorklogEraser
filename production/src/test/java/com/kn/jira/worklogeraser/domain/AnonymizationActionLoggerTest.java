package com.kn.jira.worklogeraser.domain;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.kn.jira.worklogeraser.sharedresources.JiraAdapterFixture;
import com.kn.jira.worklogeraser.sharedresources.PdmAdapterFixture;
import com.kn.jira.worklogeraser.sharedresources.TestConfigurationWithMockAdaptersFixture;

public class AnonymizationActionLoggerTest {
   public static final String DELETITION_ELEMENTS_SELECTOR = "worklogDeleted";
   public static final String EXECUTION_ELEMENTS_SELECTOR = "/worklogEraser/execution";
   public static final String ISSUE_ELEMENTS_SELECTOR = "subjectIssue";
   public static final String PROJECT_ACCESS_RESTRICTION_ELEMENTS_SELECTOR = "projectAccessRestriction";
   public static final String PROJECT_ELEMENTS_SELECTOR = "subjectProject";
   private AnonymizationActionLogger actionLogger;
   protected JiraAdapterFixture jiraAdapterFixture;
   private Document log;
   protected Date obsolatedWorklogDate;
   protected PdmAdapterFixture pdmAdapterFixture;
   private TestConfigurationWithMockAdaptersFixture testConfiguration;
   private XPath xPath;

   @Before
   public void beforeEachTest() throws SAXException, IOException, ParserConfigurationException, TransformerException {
      testConfiguration = new TestConfigurationWithMockAdaptersFixture();
      testConfiguration.setUp();
      jiraAdapterFixture = testConfiguration.getJiraAdapterFixture();
      pdmAdapterFixture = testConfiguration.getPdmAdapterFixture();
      
      actionLogger = testConfiguration.getActionLog();
      actionLogger.setUp();

      log = actionLogger.getLog();
      xPath = XPathFactory.newInstance().newXPath();
      
      determineWorklogObsolationDate();
   }

   @After
   public void afterEachTest() throws TransformerException, XPathExpressionException, IOException {
      //printLastContentOfLog();
      emptyLogDocument();
      actionLogger.tearDown();
      testConfiguration.tearDown();
   }

   //Test methods
   @Test
   public void executionStart_addsContainerElementForAllOtherElements() throws XPathExpressionException {
      NodeList nodes = (NodeList) xPath.evaluate( EXECUTION_ELEMENTS_SELECTOR, log.getDocumentElement(), XPathConstants.NODESET );
      Integer numberOfExecutionElementsBefore = nodes.getLength();
      
      actionLogger.executionStart( new Date() );

      nodes = (NodeList) xPath.evaluate( EXECUTION_ELEMENTS_SELECTOR, log.getDocumentElement(), XPathConstants.NODESET );
      assertThat( nodes.getLength(), equalTo( numberOfExecutionElementsBefore + 1 ) );
   }

   @Test public void executionStart_logsStartTime() {
      actionLogger.executionStart( new Date() );

      Element currentExecutionElement = Whitebox.getInternalState( actionLogger, "currentExecutionElement" );
      String executionStart = currentExecutionElement.getAttribute( AnonymizationActionLogger.EXECUTION_START_ATTRIBUTE_NAME );
      assertThat( executionStart, not( isEmptyOrNullString() ));
   }

   @Test public void executionStart_logsObsolationTime() {
      actionLogger.executionStart( obsolatedWorklogDate );
      SimpleDateFormat dateFormat = new SimpleDateFormat( WorklogAnonymizator.DATE_FORMAT );
      
      String obsolationData = determineExecutionElementAttribute( AnonymizationActionLogger.OBSOLATION_ATTRIBUTE_NAME );
      assertThat( obsolationData, equalTo( dateFormat.format( obsolatedWorklogDate )));
   }

   @Test public void executionEnd_logsFinishTime() {
      actionLogger.executionStart( obsolatedWorklogDate );
      actionLogger.executionEnd();

      Element currentExecutionElement = Whitebox.getInternalState( actionLogger, "currentExecutionElement" );
      String executionEnd = currentExecutionElement.getAttribute( AnonymizationActionLogger.EXECUTION_END_ATTRIBUTE_NAME );
      assertThat( executionEnd, not( isEmptyOrNullString() ));
   }
   
   @Test public void considerProject_addsSubjectProjectElement() throws XPathExpressionException{
      actionLogger.executionStart( obsolatedWorklogDate );
      actionLogger.considerProject( "ProjectName" );
      
      Node subjectProjectElement = determineSubjectProjectElement();
      
      //VERIFY:
      assertThat( subjectProjectElement.getAttributes().getNamedItem( "name" ).getNodeValue(), equalTo( "ProjectName" ));
   }

   @Test public void projectAccessRestriction_addsProjectAccessRestrictionElement() throws XPathExpressionException{
      actionLogger.executionStart( obsolatedWorklogDate );
      actionLogger.considerProject( "ProjectName" );
      actionLogger.projectAccessRestriction( "JiraUser" );
      
      Node projectAccessRestrictionElement = determineProjectAccessRestrictionElement();
      
      //VERIFY:
      assertThat( projectAccessRestrictionElement.getAttributes().getNamedItem( "name" ).getNodeValue(), equalTo( "JiraUser" ));
   }

   @Test public void considerIssue_addsSubjectIssueElement() throws XPathExpressionException{
      //SETUP:
      actionLogger.executionStart( obsolatedWorklogDate );
      actionLogger.considerProject( "ProjectName" );
      
      //EXECUTION:
      actionLogger.considerIssue( "IssueName", "AnyStatus" );
      
      //VERIFY:
      Node subjectIssueElement = determineSubjectIssueElement();
      assertThat( subjectIssueElement.getAttributes().getNamedItem( "name" ).getNodeValue(), equalTo( "IssueName" ));
   }

   @Test public void considerIssue_logsIssueStatus() throws XPathExpressionException{
      //SETUP:
      actionLogger.executionStart( obsolatedWorklogDate );
      actionLogger.considerProject( "ProjectName" );
      
      //EXECUTION:
      actionLogger.considerIssue( "IssueName", "AnyStatus" );
      
      //VERIFY:
      Node subjectIssueElement = determineSubjectIssueElement();
      assertThat( subjectIssueElement.getAttributes().getNamedItem( "name" ).getNodeValue(), equalTo( "IssueName" ));
      assertThat( subjectIssueElement.getAttributes().getNamedItem( "status" ).getNodeValue(), equalTo( "AnyStatus" ));
   }

   @Test public void deleteWorklog_addsDeletitionElementAndSignsIssue() throws XPathExpressionException{
      //SETUP:
      actionLogger.executionStart( obsolatedWorklogDate );
      actionLogger.considerProject( "ProjectName" );
      actionLogger.considerIssue( "IssueName", "WhateverStatus" );
      
      //EXECUTION:
      actionLogger.worklogAnonymated( "http://an.issue.uri", "john.smith@kn.com", "Working hardly in this issue" );
      
      //VERIFY:
      Node deletitionElement = determineDeleteWorklogElement();
      
      assertThat( deletitionElement.getAttributes().getNamedItem( "issueUri" ).getNodeValue(), equalTo( "http://an.issue.uri" ));
      assertThat( deletitionElement.getAttributes().getNamedItem( "email" ).getNodeValue(), equalTo( "john.smith@kn.com" ));
      
      //TEAR DOWN:
      actionLogger.executionEnd();
   }

   @Test public void worklogWasManipulated_signsIssue() throws XPathExpressionException{
      //SETUP:
      actionLogger.executionStart( obsolatedWorklogDate );
      actionLogger.considerProject( "ProjectName" );
      actionLogger.considerIssue( "IssueName", "WhateverStatus" );
      
      //EXECUTION:
      actionLogger.worklogWasManipulated();
      
      //VERIFY:
      Node issueElement = determineSubjectIssueElement();
      assertThat( issueElement.getAttributes().getNamedItem( "isWorkModified" ).getNodeValue(), equalTo( "true" ));
      
      //TEAR DOWN:
      actionLogger.executionEnd();
   }

   //Protected, private helper methods
   private Node determineDeleteWorklogElement() throws XPathExpressionException {
      Node subjectIssueElement = determineSubjectIssueElement();
      NodeList nodes = (NodeList) xPath.evaluate( DELETITION_ELEMENTS_SELECTOR, subjectIssueElement, XPathConstants.NODESET );
      Node deletitionElement = nodes.item( 0 );
      return deletitionElement;
   }

   private String determineExecutionElementAttribute( String attributeName ) {
      Element currentExecutionElement = Whitebox.getInternalState( actionLogger, "currentExecutionElement" );
      String executionStart = currentExecutionElement.getAttribute( attributeName );
      return executionStart;
   }

   private Node determineProjectAccessRestrictionElement() throws XPathExpressionException {
      Node subjectProjectElement = determineSubjectProjectElement();
      NodeList nodes = (NodeList) xPath.evaluate( PROJECT_ACCESS_RESTRICTION_ELEMENTS_SELECTOR, subjectProjectElement, XPathConstants.NODESET );
      Node projectAccessRestrictionElement = nodes.item( 0 );
      return projectAccessRestrictionElement;
   }

   private Node determineSubjectIssueElement() throws XPathExpressionException {
      Node subjectProjectElement = determineSubjectProjectElement();
      NodeList nodes = (NodeList) xPath.evaluate( ISSUE_ELEMENTS_SELECTOR, subjectProjectElement, XPathConstants.NODESET );
      Node subjectIssueElement = nodes.item( 0 );
      return subjectIssueElement;
   }
   private Node determineSubjectProjectElement() throws XPathExpressionException {
      Element currentExecutionElement = Whitebox.getInternalState( actionLogger, "currentExecutionElement" );
      NodeList nodes = (NodeList) xPath.evaluate( PROJECT_ELEMENTS_SELECTOR, currentExecutionElement, XPathConstants.NODESET );
      Node subjectProjectElement = nodes.item( 0 );
      return subjectProjectElement;
   }
   
   private void determineWorklogObsolationDate(){
      obsolatedWorklogDate = new Date();
   }
   
   private void emptyLogDocument() throws XPathExpressionException {
      Element rootElement = log.getDocumentElement();
      NodeList executionNodes = (NodeList) xPath.evaluate( EXECUTION_ELEMENTS_SELECTOR, log.getDocumentElement(), XPathConstants.NODESET );
      for( int i = 0; i < executionNodes.getLength(); ++i ){
         Element executionNode = (Element) executionNodes.item( i );
         rootElement.removeChild( executionNode );
      }
   }

/*
   private void printLastContentOfLog() throws TransformerException{
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
      DOMSource source = new DOMSource( log );
      StringWriter writer = new StringWriter();
      transformer.transform( source, new StreamResult( writer ));
      
      System.out.println( writer.getBuffer().toString() );
   }
*/
}
