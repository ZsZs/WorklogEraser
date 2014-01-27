package com.kn.jira.worklogeraser.pdmadapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace = "http://persondata.ws.pdmplusplus.kn.com/", name = "findEmployeeByEmail")
@XmlType(propOrder = { "mail" })
public class FindEmployeeByEmailRequest {
   @XmlElement private String mail;
   
   public FindEmployeeByEmailRequest(){}
   
   public FindEmployeeByEmailRequest( final String email ){
      this.mail = email;
   }
   
   public String getEmail(){ return mail; }
}
