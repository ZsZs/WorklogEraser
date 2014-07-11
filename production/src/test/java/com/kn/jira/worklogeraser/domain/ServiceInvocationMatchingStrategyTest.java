package com.kn.jira.worklogeraser.domain;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.atlassian.jira.rest.client.api.domain.Worklog;

public class ServiceInvocationMatchingStrategyTest extends EmployeeMatchingStrategyTest{
   
   //Test methods
   @Test public void isWorklogEffected_invokesJiraAdapter(){
      Worklog subjectWorklog = subjectWorklogs.get( 0 );
      employeeMatchingStrategy.isWorklogEffected( subjectWorklog );
      
      verify( jiraAdapter, times( 1 ) ).findUserDetails( subjectWorklog.getAuthor() );
   }
   
   @Test public void isWorklogEffected_invokesPdmAdapter(){
      Worklog subjectWorklog = subjectWorklogs.get( 0 );
      employeeMatchingStrategy.isWorklogEffected( subjectWorklog );
      
      verify( pdmAdapter, times( 1 ) ).findPersonByEmail( (String) anyObject() );
   }
   
}
