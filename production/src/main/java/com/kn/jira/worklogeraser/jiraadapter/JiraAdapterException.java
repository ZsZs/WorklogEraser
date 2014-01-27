package com.kn.jira.worklogeraser.jiraadapter;

import com.kn.jira.worklogeraser.domain.WorklogEraserException;

public class JiraAdapterException extends WorklogEraserException {
   private static final long serialVersionUID = -2938394220279170557L;
   private static String formatPattern = "By invoking JiraAdapter.{0}() method, the following exeption was captured:\n {1}";
   
   public JiraAdapterException( String invokedMethodName, Throwable cause ){
      super( formatPattern, new Object[]{ invokedMethodName, cause.getMessage() }, cause );
   }
   
   public JiraAdapterException( String formatPattern, Object[] arguments, Throwable cause ) {
      super( formatPattern, arguments, cause );
   }
}
