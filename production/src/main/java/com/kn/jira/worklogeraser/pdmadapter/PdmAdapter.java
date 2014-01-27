package com.kn.jira.worklogeraser.pdmadapter;

public class PdmAdapter {
   private PdmPersonServiceClient pdmClient;
   
   public PdmAdapter(){}
   
   public PersonInPdm findPersonByEmail( String email ){
      FindEmployeeByEmailResponse response = pdmClient.findEmployeeByEmail( email.toLowerCase() ); 
      if( response != null ) return response.getFoundedPerson();
      else return null;
   }
   
   //Properties
   public void setPersonServiceClient( PdmPersonServiceClient pdmClient ){
      this.pdmClient = pdmClient;
   }
}
