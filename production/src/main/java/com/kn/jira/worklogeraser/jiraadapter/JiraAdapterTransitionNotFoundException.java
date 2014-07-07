package com.kn.jira.worklogeraser.jiraadapter;

import com.atlassian.jira.rest.client.api.domain.Issue;

public class JiraAdapterTransitionNotFoundException extends JiraAdapterException {
   private static final long serialVersionUID = 91255102774297365L;
   private static String formatPattern = "Transtion: ''{0}'' of issue: ''{1}'' is not defined in the current status: ''{2}''";

   public JiraAdapterTransitionNotFoundException( String requieredTranstion, Issue issue ) {
      super( formatPattern, new Object[]{requieredTranstion, issue.getKey(), issue.getStatus().getName()}, null );
   }
}
