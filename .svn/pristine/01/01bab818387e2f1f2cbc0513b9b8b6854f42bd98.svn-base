package com.kn.jira.worklogeraser.sharedresources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kn.jira.worklogeraser.domain.EmployeeMatchingStrategy;
import com.kn.jira.worklogeraser.pdmadapter.PdmAdapter;
import com.kn.jira.worklogeraser.pdmadapter.PersonInPdm;

public class PdmAdapterFixture {
   private static final String NONE_GERMAN_EMPLOYEE_EMAIL = "none.german.employee@kh.com";
   private static final String GERMAN_EMPLOYEE_EMAIL = "german.employee@kh.com";
   private PdmAdapter mockPdmAdapter;
   private PersonInPdm germanEmployee;
   private PersonInPdm noneGermanEmployee;
   
   //Public accessors and mutators
   public void setUp(){
      mockPdmAdapter = mock( PdmAdapter.class );
      createPersons();
   }
   
   public void tearDown(){
   }

   //Properties
   public PdmAdapter getPdmAdapter() { return mockPdmAdapter; }
   
   //Protected, private helper methods
   private void createPersons(){
      germanEmployee = mock( PersonInPdm.class );
      when( germanEmployee.getEmailAddress() ).thenReturn( GERMAN_EMPLOYEE_EMAIL );
      when( germanEmployee.getCountryCode() ).thenReturn( EmployeeMatchingStrategy.EXPECTED_COUNTRY_CODE );
      when( germanEmployee.getCompanyName() ).thenReturn( EmployeeMatchingStrategy.EXPECTED_COMPANY_NAME );
      when( germanEmployee.getIsStaffMember() ).thenReturn( EmployeeMatchingStrategy.EXPECTED_STUFF_MEMBER_VALUE );
      when( mockPdmAdapter.findPersonByEmail( GERMAN_EMPLOYEE_EMAIL )).thenReturn( germanEmployee );
      
      noneGermanEmployee = mock( PersonInPdm.class );
      when( noneGermanEmployee.getEmailAddress() ).thenReturn( NONE_GERMAN_EMPLOYEE_EMAIL );
      when( noneGermanEmployee.getCountryCode() ).thenReturn( "HU" );
      when( noneGermanEmployee.getCompanyName() ).thenReturn( "Agile Renovation Llc." );
      when( noneGermanEmployee.getIsStaffMember() ).thenReturn( "nope" );
      when( mockPdmAdapter.findPersonByEmail( NONE_GERMAN_EMPLOYEE_EMAIL )).thenReturn( noneGermanEmployee );
   }
}
