package com.kn.jira.worklogeraser.jiraadapter;

public class JiraAdapterDeleteWorklogException extends JiraAdapterException {
   private static final long serialVersionUID = 91255102774297365L;
   private static String formatPattern = "Deleting worklog: ''{0}'' of issue: ''{1}'' resulted in the following exeption:\n {2}";

   public JiraAdapterDeleteWorklogException( String worklogId, String issueId, Throwable cause ) {
      super( formatPattern, new Object[]{worklogId, issueId, cause.getMessage()}, cause );
   }
}
