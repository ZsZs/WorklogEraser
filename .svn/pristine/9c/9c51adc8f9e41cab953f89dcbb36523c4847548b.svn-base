package com.kn.jira.worklogeraser.pdmadapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kn.jira.worklogeraser.sharedresources.TestConfigurationFixture;

public class PdmPersonServiceClientTest {
   private static final String SEARCH_STRING = "Zsolt";
   private TestConfigurationFixture testConfiguration;
   private PdmPersonServiceClient webServiceClient;

   @Before public void beforeEachTest(){
      testConfiguration = new TestConfigurationFixture();
      testConfiguration.setUp();
      webServiceClient = testConfiguration.getPdmPersonServiceClient();
   }
   
   @After public void afterEachTest(){
      testConfiguration.tearDown();
   }
   
   @Test public void findEmployeeByEmail(){
      FindEmployeeByEmailResponse response = webServiceClient.findEmployeeByEmail( "external.zsolt.zsuffa@kuehne-nagel.com" );
      PersonInPdm foundedPerson = response.getFoundedPerson();
      
      assertThat( foundedPerson.getEmailAddress(), equalTo( "external.zsolt.zsuffa@kuehne-nagel.com" ));
   }
   
   @Test public void findEmployeeByString(){
      SearchEmployeeByStringResponse response = webServiceClient.findEmployeeByString( SEARCH_STRING );
      List<PersonInPdm> foundedPersons = response.getFoundedPersons();
      
      assertThat( foundedPersons, everyItem( Matchers.<PersonInPdm>hasProperty( "firstname", containsString( SEARCH_STRING ))));
   }
}
