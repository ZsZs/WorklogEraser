package com.kn.jira.worklogeraser.domain;

import java.text.MessageFormat;

public abstract class WorklogEraserException extends Exception {
   private static final long serialVersionUID = 5413577276552401366L;
   protected Throwable cause = null;

   public WorklogEraserException( String formatPattern, Object[] arguments, Throwable cause ) {
      super( MessageFormat.format( formatPattern, arguments ), cause );
      this.cause = cause;
   }
}
