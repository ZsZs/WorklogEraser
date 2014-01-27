package com.kn.jira.worklogeraser.pdmadapter;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( namespace = "http://persondata.ws.pdmplusplus.kn.com/", name = "searchEmployeeByStringResponse" )
@XmlAccessorType( XmlAccessType.FIELD )
public class SearchEmployeeByStringResponse {
   @XmlElementWrapper( name="return", required = true ) @XmlElement( name = "foundedPersons" ) List<PersonInPdm> foundedPersons;

   //Properties
   public List<PersonInPdm> getFoundedPersons() { return foundedPersons; } 
}
