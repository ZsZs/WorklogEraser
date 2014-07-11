package com.kn.jira.worklogeraser.domain;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class AnonymizationActionLogger {
   public static final String DATE_FORMAT = "dd/MM/yyyy, HH:mm:ss";
   public static final String DELETITION_ELEMENT_NAME = "worklogAnonymated";
   public static final String EXECUTION_ELEMENT_NAME = "execution";
   public static final String EXECUTION_END_ATTRIBUTE_NAME = "finishedOn";
   public static final String EXECUTION_START_ATTRIBUTE_NAME = "startedOn";
   public static final String ISSUE_ELEMENT_NAME = "subjectIssue";
   public static final String OBSOLATION_ATTRIBUTE_NAME = "worklogsObsolatedFrom";
   public static final String PROJECT_ACCESS_RESTRICTION_ELEMENT_NAME = "projectAccessRestriction";
   public static final String PROJECT_ELEMENT_NAME = "subjectProject";
   public static final String ROOT_ELEMENT_NAME = "worklogEraser";
   public static final String WORK_MODIFIED_ATTRIBUTE_NAME = "isWorkModified";
   private Element currentExecutionElement;
   private Logger programLogger = LoggerFactory.getLogger( AnonymizationActionLogger.class );
   private Document logDocument;
   private Resource logResource;
   private Element rootElement;
   private Element subjectIssueElement;
   private Element subjectProjectElement;
   private SimpleDateFormat dateFormat;

   // Constructors
   public AnonymizationActionLogger( final Resource logResouce ) {
      this.logResource = logResouce;
   }

   // Public mutator and accessor methods
   public void considerIssue( String issueName, String status ) {
      subjectIssueElement = logDocument.createElement( ISSUE_ELEMENT_NAME );
      subjectIssueElement.setAttribute( "name", issueName );
      subjectIssueElement.setAttribute( "status", status );
      subjectProjectElement.appendChild( subjectIssueElement );
   }

   public void considerProject( String projectName ) {
      subjectProjectElement = logDocument.createElement( PROJECT_ELEMENT_NAME );
      subjectProjectElement.setAttribute( "name", projectName );
      currentExecutionElement.appendChild( subjectProjectElement );
   }

   public void executionEnd() {
      String formattedTimeStamp = dateFormat.format( new Date());
      currentExecutionElement.setAttribute( EXECUTION_END_ATTRIBUTE_NAME, formattedTimeStamp );
   }
   
   public void executionStart( Date obsolationDate ) {
      currentExecutionElement = logDocument.createElement( EXECUTION_ELEMENT_NAME );
      dateFormat = new SimpleDateFormat( DATE_FORMAT );
      currentExecutionElement.setAttribute( EXECUTION_START_ATTRIBUTE_NAME, dateFormat.format( new Date()));
      currentExecutionElement.setAttribute( OBSOLATION_ATTRIBUTE_NAME, new SimpleDateFormat( WorklogAnonymizator.DATE_FORMAT ).format( obsolationDate ));
      rootElement.appendChild( currentExecutionElement );
   }

   public void projectAccessRestriction( String jiraUser ) {
      Element projectAccessRestrictionElement = logDocument.createElement( PROJECT_ACCESS_RESTRICTION_ELEMENT_NAME );
      projectAccessRestrictionElement.setAttribute( "name", jiraUser );
      subjectProjectElement.appendChild( projectAccessRestrictionElement );
   }

   public void setUp() throws SAXException, IOException, ParserConfigurationException, TransformerException {
      openLogFile();
      rootElement = logDocument.getDocumentElement();
   }

   public void tearDown() throws TransformerException, IOException {
      saveLogFile();
   }
   
   public void worklogAnonymated( String issueUri, String email, String comment ) {
      Element deletitionElement = logDocument.createElement( DELETITION_ELEMENT_NAME );
      deletitionElement.setAttribute( "issueUri", issueUri );
      deletitionElement.setAttribute( "email", email );
      deletitionElement.setTextContent( comment );
      subjectIssueElement.appendChild( deletitionElement );
   }
   
   public void worklogWasManipulated(){
      if( subjectIssueElement.getAttribute( WORK_MODIFIED_ATTRIBUTE_NAME ).isEmpty() ){
         subjectIssueElement.setAttribute( WORK_MODIFIED_ATTRIBUTE_NAME, "true" );
      }
   }

   //Properties
   public Document getLog() { return logDocument; }
   public String getLogPath() { return logResource.getFilename(); }

   // Protected, private helper methods
   protected void createLogFile() throws ParserConfigurationException, TransformerException, IOException {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware( true );
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

      logDocument = documentBuilder.newDocument();
      Element rootElement = logDocument.createElement( ROOT_ELEMENT_NAME );
      logDocument.appendChild( rootElement );

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
      DOMSource source = new DOMSource( logDocument );

      //Resource resource = resourceLoader.getResource( logFilePath );
      try{
         StreamResult result = new StreamResult( logResource.getFile() );
         transformer.transform( source, result );
      }catch( Throwable e ){
         e.printStackTrace();
      }
   }

   protected void openLogFile() throws SAXException, IOException, ParserConfigurationException, TransformerException {
      if( !logResource.exists() ) createLogFile();
      File logFile = logResource.getFile();
      
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware( true );
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      logDocument = documentBuilder.parse( logFile );
      programLogger.debug( "Output file opened: " + logFile.getAbsolutePath() );
   }

   protected void saveLogFile() throws TransformerException, IOException {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
      
      DOMSource source = new DOMSource( logDocument );

      StreamResult result = new StreamResult( logResource.getFile() );
      try{
         transformer.transform( source, result );
      }catch( Throwable e ){
         e.printStackTrace();
      }
   }
}
