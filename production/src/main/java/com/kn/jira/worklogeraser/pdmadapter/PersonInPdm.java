package com.kn.jira.worklogeraser.pdmadapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType( XmlAccessType.FIELD )
public class PersonInPdm {
   @XmlAttribute private String acCompany;
   @XmlAttribute private String adressAddendum;
   @XmlAttribute private String branchOfficeNumber;
   @XmlAttribute private String businessFieldName;
   @XmlAttribute private String businessSubfieldName;
   @XmlAttribute private String businessUnitName;
   @XmlAttribute private String city;
   @XmlAttribute private String companyName;
   @XmlAttribute private String corporateCompany;
   @XmlAttribute private String costCenter;
   @XmlAttribute private String countryCode;
   @XmlAttribute private String departmentName;
   @XmlAttribute private String departmentTypeName;
   @XmlAttribute private String emailAddress;
   @XmlAttribute private String fax;
   @XmlAttribute private String firstname;
   @XmlAttribute private String firstnameInternational;
   @XmlAttribute private String isActive;
   @XmlAttribute private String isStaffMember;
   @XmlAttribute private String knCode;
   @XmlAttribute private String knRegionCode;
   @XmlAttribute private String knRegionName;
   @XmlAttribute private String lastname;
   @XmlAttribute private String lastnameInternational;
   @XmlAttribute private String locationCode;
   @XmlAttribute private String mobile;
   @XmlAttribute private String pdmUid;
   @XmlAttribute private String phone;
   @XmlAttribute private String postalCode;
   @XmlAttribute private String solidLinePdmUid;
   @XmlAttribute private String street;
   
   //Properties
   public String getAcCompany() { return acCompany; }
   public String getAdressAddendum() { return adressAddendum; }
   public String getBranchOfficeNumber() { return branchOfficeNumber; }
   public String getBusinessFieldName() { return businessFieldName; }
   public String getBusinessSubfieldName() { return businessSubfieldName; }
   public String getBusinessUnitName() { return businessUnitName; }
   public String getCity() { return city; }
   public String getCompanyName() { return companyName; }
   public String getCorporateCompany() { return corporateCompany; }
   public String getCostCenter() { return costCenter; }
   public String getCountryCode() { return countryCode; }
   public String getDepartmentName() { return departmentName; }
   public String getDepartmentTypeName() { return departmentTypeName; }
   public String getEmailAddress() { return emailAddress; }
   public String getFax() { return fax; }
   public String getFirstname() { return firstname; }
   public String getFirstnameInternational() { return firstnameInternational; }
   public String getIsActive() { return isActive; }
   public String getIsStaffMember() { return isStaffMember; }
   public String getKnCode() { return knCode; }
   public String getKnRegionCode() { return knRegionCode; }
   public String getKnRegionName() { return knRegionName; }
   public String getLastname() { return lastname; }
   public String getLastnameInternational() { return lastnameInternational; }
   public String getLocationCode() { return locationCode; }
   public String getMobile() { return mobile; }
   public String getPdmUid() { return pdmUid; }
   public String getPhone() { return phone; }
   public String getPostalCode() { return postalCode; }
   public String getSolidLinePdmUid() { return solidLinePdmUid; }
   public String getStreet() { return street; }
}
