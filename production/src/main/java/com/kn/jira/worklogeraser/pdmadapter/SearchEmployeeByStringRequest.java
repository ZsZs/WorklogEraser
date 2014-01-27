package com.kn.jira.worklogeraser.pdmadapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace = "http://persondata.ws.pdmplusplus.kn.com/", name = "searchEmployeeByString")
@XmlType(propOrder = { "searchString" })
public class SearchEmployeeByStringRequest {
   @XmlElement private String searchString = "Zsolt Zsuffa";
   
   public SearchEmployeeByStringRequest(){}
   
   public SearchEmployeeByStringRequest( final String searchString ){
      this.searchString = searchString;
   }
   
   public String getSearchString(){ return searchString; }
}
