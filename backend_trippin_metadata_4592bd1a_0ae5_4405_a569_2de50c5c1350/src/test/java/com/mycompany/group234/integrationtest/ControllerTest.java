package com.mycompany.group234.integrationtest;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.group234.SpringApp;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.path.xml.XmlPath;
import io.restassured.path.xml.element.Node;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpringApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.config.location=classpath:application-test.yml" })
class ControllerTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private final ObjectMapper mapper = new ObjectMapper();

  @Autowired
  private WebApplicationContext context;
  @LocalServerPort
  private int port;

  @BeforeEach
  void setup() {
    RestAssuredMockMvc.webAppContextSetup(context);
  }

  
  
   private JsonNode getJSONFromFile(String filePath) throws IOException {
    try(InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath)){
      JsonNode jsonNode = mapper.readValue(in, JsonNode.class);
      return jsonNode;
    }
    catch(Exception e){
      throw new RuntimeException(e);
    }
  }
  
  private String getPayload(String filePath) throws IOException {
	  String jsonString = mapper.writeValueAsString( getJSONFromFile(filePath) );
	  return jsonString;
  }

  @Test
  void testRetrieveServiceDocument() {
    final String xml = given()
        .accept(ContentType.XML)
        .when()
        .get("/trippin_metadata/")
        .then()
        .statusCode(HttpStatusCode.OK.getStatusCode())
        .contentType(ContentType.XML)
        .extract()
        .asString();

    final XmlPath path = new XmlPath(xml);
    final Collection<Node> n = ((Node) ((Node) path.get("service")).get("workspace")).get("collection");
    assertNotNull(n);
    assertFalse(n.isEmpty());
  }

  @Test
  void  testRetrieveMetadataDocument() {
    final String xml = given()
        .when()
        .get("/trippin_metadata/$metadata")
        .then()
        .statusCode(HttpStatusCode.OK.getStatusCode())
        .contentType(ContentType.XML)
        .extract()
        .asString();

    final XmlPath path = new XmlPath(xml);
    final Node n = ((Node) ((Node) path.get("edmx:Ed mx")).get("DataServices")).get("Schema");
    assertNotNull(n);
    assertEquals("Trippin", n.getAttribute("Namespace"));
    assertNotNull(n.get("EntityContainer"));
  }

	

	
  @Test
  void  testCreateTripInstance() throws IOException {
    given()
        .contentType("application/json")
        .body(getPayload("TripInstance.json"))
        .when()
        .post("/trippin_metadata/Trips")
        .then()
        .statusCode(HttpStatusCode.CREATED.getStatusCode());
    
  }
	
	
  
   
  
   @Test
  public void testSystemFilterOptionsTrip() throws IOException {
  
   given()
            .when()
            .get("/trippin_metadata/Trips?$top=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).TripId", equalTo("<<replace_with_keyFieldValue>>"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Trips?$skip=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).TripId", equalTo("<<replace_with_keyFieldValue>>"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Trips/$count")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body(is("2"));
            
            
		
	     given()
	            .when()
	            .get("/trippin_metadata/Trips(<<replace_with_keyFieldValue>>)?$expand=Travellers")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Travellers.get(0).UserName",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/Trips(<<replace_with_keyFieldValue>>)?$expand=Travellers($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Travellers.get(0).UserName",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/Trips('lewisblack1')?$expand=Travellers($select=UserName,FirstName)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("Travellers.get(0).UserName",is(1))
	        .body("Travellers.get(0).size()", is(2));
	        
	    
        
  
		
	     given()
	            .when()
	            .get("/trippin_metadata/Trips(<<replace_with_keyFieldValue>>)?$expand=PlanItems")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("PlanItems.get(0).PlanItemId",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/Trips(<<replace_with_keyFieldValue>>)?$expand=PlanItems($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("PlanItems.get(0).PlanItemId",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/Trips('lewisblack1')?$expand=PlanItems($select=PlanItemId,ConfirmationCode)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("PlanItems.get(0).PlanItemId",is(1))
	        .body("PlanItems.get(0).size()", is(2));
	        
	    
        
  
    } 
	
	

	
  @Test
  void  testCreateAirlineInstance() throws IOException {
    given()
        .contentType("application/json")
        .body(getPayload("AirlineInstance.json"))
        .when()
        .post("/trippin_metadata/Airlines")
        .then()
        .statusCode(HttpStatusCode.CREATED.getStatusCode());
    
  }
	
	
  
   
  
   @Test
  public void testSystemFilterOptionsAirline() throws IOException {
  
   given()
            .when()
            .get("/trippin_metadata/Airlines?$top=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).AirlineCode", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Airlines?$skip=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).AirlineCode", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Airlines/$count")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body(is("2"));
            
            
    } 
	
	

	
  @Test
  void  testCreateFlightInstance() throws IOException {
    given()
        .contentType("application/json")
        .body(getPayload("FlightInstance.json"))
        .when()
        .post("/trippin_metadata/Flights")
        .then()
        .statusCode(HttpStatusCode.CREATED.getStatusCode());
    
  }
	
	
  
   
  
   @Test
  public void testSystemFilterOptionsFlight() throws IOException {
  
   given()
            .when()
            .get("/trippin_metadata/Flights?$top=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).PlanItemId", equalTo("<<replace_with_keyFieldValue>>"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Flights?$skip=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).PlanItemId", equalTo("<<replace_with_keyFieldValue>>"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Flights/$count")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body(is("2"));
            
            
		
	     given()
	            .when()
	            .get("/trippin_metadata/Flights(<<replace_with_keyFieldValue>>)?$expand=Airline")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Airline.get(0).AirlineCode",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/Flights(<<replace_with_keyFieldValue>>)?$expand=Airline($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Airline.get(0).AirlineCode",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/Flights('lewisblack1')?$expand=Airline($select=AirlineCode,Name)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("Airline.get(0).AirlineCode",is(1))
	        .body("Airline.get(0).size()", is(2));
	        
	    
        
  
		
	     given()
	            .when()
	            .get("/trippin_metadata/Flights(<<replace_with_keyFieldValue>>)?$expand=From")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("From.get(0).IcaoCode",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/Flights(<<replace_with_keyFieldValue>>)?$expand=From($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("From.get(0).IcaoCode",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/Flights('lewisblack1')?$expand=From($select=IcaoCode,Name)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("From.get(0).IcaoCode",is(1))
	        .body("From.get(0).size()", is(2));
	        
	    
        
  
		
	     given()
	            .when()
	            .get("/trippin_metadata/Flights(<<replace_with_keyFieldValue>>)?$expand=To")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("To.get(0).IcaoCode",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/Flights(<<replace_with_keyFieldValue>>)?$expand=To($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("To.get(0).IcaoCode",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/Flights('lewisblack1')?$expand=To($select=IcaoCode,Name)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("To.get(0).IcaoCode",is(1))
	        .body("To.get(0).size()", is(2));
	        
	    
        
  
    } 
	
	

	
  @Test
  void  testCreatePlanItemInstance() throws IOException {
    given()
        .contentType("application/json")
        .body(getPayload("PlanItemInstance.json"))
        .when()
        .post("/trippin_metadata/PlanItems")
        .then()
        .statusCode(HttpStatusCode.CREATED.getStatusCode());
    
  }
	
	
  
   
  
   @Test
  public void testSystemFilterOptionsPlanItem() throws IOException {
  
   given()
            .when()
            .get("/trippin_metadata/PlanItems?$top=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).PlanItemId", equalTo("<<replace_with_keyFieldValue>>"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/PlanItems?$skip=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).PlanItemId", equalTo("<<replace_with_keyFieldValue>>"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/PlanItems/$count")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body(is("2"));
            
            
		
	     given()
	            .when()
	            .get("/trippin_metadata/PlanItems(<<replace_with_keyFieldValue>>)?$expand=Ticket")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Ticket.get(0).DocId",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/PlanItems(<<replace_with_keyFieldValue>>)?$expand=Ticket($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Ticket.get(0).DocId",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/PlanItems('lewisblack1')?$expand=Ticket($select=DocId,DocName)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("Ticket.get(0).DocId",is(1))
	        .body("Ticket.get(0).size()", is(2));
	        
	    
        
  
    } 
	
	

	
  @Test
  void  testCreateDocumentInstance() throws IOException {
    given()
        .contentType("application/json")
        .body(getPayload("DocumentInstance.json"))
        .when()
        .post("/trippin_metadata/Documents")
        .then()
        .statusCode(HttpStatusCode.CREATED.getStatusCode());
    
  }
	
	
  
   
  
   @Test
  public void testSystemFilterOptionsDocument() throws IOException {
  
   given()
            .when()
            .get("/trippin_metadata/Documents?$top=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).DocId", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Documents?$skip=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).DocId", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Documents/$count")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body(is("2"));
            
            
		
	     given()
	            .when()
	            .get("/trippin_metadata/Documents('<<replace_with_keyFieldValue>>')?$expand=PlanItem")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("PlanItem.get(0).PlanItemId",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/Documents('<<replace_with_keyFieldValue>>')?$expand=PlanItem($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("PlanItem.get(0).PlanItemId",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/Documents('lewisblack1')?$expand=PlanItem($select=PlanItemId,ConfirmationCode)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("PlanItem.get(0).PlanItemId",is(1))
	        .body("PlanItem.get(0).size()", is(2));
	        
	    
        
  
		
	     given()
	            .when()
	            .get("/trippin_metadata/Documents('<<replace_with_keyFieldValue>>')?$expand=Person")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Person.get(0).UserName",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/Documents('<<replace_with_keyFieldValue>>')?$expand=Person($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Person.get(0).UserName",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/Documents('lewisblack1')?$expand=Person($select=UserName,FirstName)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("Person.get(0).UserName",is(1))
	        .body("Person.get(0).size()", is(2));
	        
	    
        
  
    } 
	
	

	
  @Test
  void  testCreateManagerInstance() throws IOException {
    given()
        .contentType("application/json")
        .body(getPayload("ManagerInstance.json"))
        .when()
        .post("/trippin_metadata/Managers")
        .then()
        .statusCode(HttpStatusCode.CREATED.getStatusCode());
    
  }
	
	
  
   
  
   @Test
  public void testSystemFilterOptionsManager() throws IOException {
  
   given()
            .when()
            .get("/trippin_metadata/Managers?$top=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).UserName", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Managers?$skip=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).UserName", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Managers/$count")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body(is("2"));
            
            
		
	     given()
	            .when()
	            .get("/trippin_metadata/Managers('<<replace_with_keyFieldValue>>')?$expand=DirectReports")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("DirectReports.get(0).UserName",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/Managers('<<replace_with_keyFieldValue>>')?$expand=DirectReports($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("DirectReports.get(0).UserName",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/Managers('lewisblack1')?$expand=DirectReports($select=UserName,FirstName)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("DirectReports.get(0).UserName",is(1))
	        .body("DirectReports.get(0).size()", is(2));
	        
	    
        
  
    } 
	
		
	

	
  @Test
  void  testCreateEmployeeInstance() throws IOException {
    given()
        .contentType("application/json")
        .body(getPayload("EmployeeInstance.json"))
        .when()
        .post("/trippin_metadata/Employees")
        .then()
        .statusCode(HttpStatusCode.CREATED.getStatusCode());
    
  }
	
	
  
   
  
   @Test
  public void testSystemFilterOptionsEmployee() throws IOException {
  
   given()
            .when()
            .get("/trippin_metadata/Employees?$top=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).UserName", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Employees?$skip=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).UserName", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Employees/$count")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body(is("2"));
            
            
		
	     given()
	            .when()
	            .get("/trippin_metadata/Employees('<<replace_with_keyFieldValue>>')?$expand=Peers")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Peers.get(0).UserName",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/Employees('<<replace_with_keyFieldValue>>')?$expand=Peers($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Peers.get(0).UserName",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/Employees('lewisblack1')?$expand=Peers($select=UserName,FirstName)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("Peers.get(0).UserName",is(1))
	        .body("Peers.get(0).size()", is(2));
	        
	    
        
  
    } 
	
		
	

	
  @Test
  void  testCreateAirportInstance() throws IOException {
    given()
        .contentType("application/json")
        .body(getPayload("AirportInstance.json"))
        .when()
        .post("/trippin_metadata/Airports")
        .then()
        .statusCode(HttpStatusCode.CREATED.getStatusCode());
    
  }
	
	
  
   
  
   @Test
  public void testSystemFilterOptionsAirport() throws IOException {
  
   given()
            .when()
            .get("/trippin_metadata/Airports?$top=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).IcaoCode", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Airports?$skip=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).IcaoCode", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Airports/$count")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body(is("2"));
            
            
    } 
	
	

	
  @Test
  void  testCreateEventInstance() throws IOException {
    given()
        .contentType("application/json")
        .body(getPayload("EventInstance.json"))
        .when()
        .post("/trippin_metadata/Events")
        .then()
        .statusCode(HttpStatusCode.CREATED.getStatusCode());
    
  }
	
	
  
   
  
   @Test
  public void testSystemFilterOptionsEvent() throws IOException {
  
   given()
            .when()
            .get("/trippin_metadata/Events?$top=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).PlanItemId", equalTo("<<replace_with_keyFieldValue>>"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Events?$skip=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).PlanItemId", equalTo("<<replace_with_keyFieldValue>>"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/Events/$count")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body(is("2"));
            
            
    } 
	
	

	
  @Test
  void  testCreatePublicTransportationInstance() throws IOException {
    given()
        .contentType("application/json")
        .body(getPayload("PublicTransportationInstance.json"))
        .when()
        .post("/trippin_metadata/PublicTransportations")
        .then()
        .statusCode(HttpStatusCode.CREATED.getStatusCode());
    
  }
	
	
  
   
  
   @Test
  public void testSystemFilterOptionsPublicTransportation() throws IOException {
  
   given()
            .when()
            .get("/trippin_metadata/PublicTransportations?$top=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).PlanItemId", equalTo("<<replace_with_keyFieldValue>>"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/PublicTransportations?$skip=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).PlanItemId", equalTo("<<replace_with_keyFieldValue>>"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/PublicTransportations/$count")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body(is("2"));
            
            
    } 
	
	

	
  @Test
  void  testCreatePersonInstance() throws IOException {
    given()
        .contentType("application/json")
        .body(getPayload("PersonInstance.json"))
        .when()
        .post("/trippin_metadata/People")
        .then()
        .statusCode(HttpStatusCode.CREATED.getStatusCode());
    
  }
	
	
  
   
  
   @Test
  public void testSystemFilterOptionsPerson() throws IOException {
  
   given()
            .when()
            .get("/trippin_metadata/People?$top=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).UserName", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/People?$skip=1")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body("value.get(0).UserName", equalTo("'<<replace_with_keyFieldValue>>'"))
            .body("value.size()", is(1));
    given()
            .when()
            .get("/TripPinRESTierService/People/$count")
            .then()
            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
            .body(is("2"));
            
            
		
	     given()
	            .when()
	            .get("/trippin_metadata/People('<<replace_with_keyFieldValue>>')?$expand=Friends")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Friends.get(0).UserName",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/People('<<replace_with_keyFieldValue>>')?$expand=Friends($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Friends.get(0).UserName",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/People('lewisblack1')?$expand=Friends($select=UserName,FirstName)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("Friends.get(0).UserName",is(1))
	        .body("Friends.get(0).size()", is(2));
	        
	    
        
  
		
	     given()
	            .when()
	            .get("/trippin_metadata/People('<<replace_with_keyFieldValue>>')?$expand=BestFriend")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("BestFriend.get(0).UserName",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/People('<<replace_with_keyFieldValue>>')?$expand=BestFriend($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("BestFriend.get(0).UserName",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/People('lewisblack1')?$expand=BestFriend($select=UserName,FirstName)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("BestFriend.get(0).UserName",is(1))
	        .body("BestFriend.get(0).size()", is(2));
	        
	    
        
  
		
	     given()
	            .when()
	            .get("/trippin_metadata/People('<<replace_with_keyFieldValue>>')?$expand=Trips")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Trips.get(0).TripId",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/People('<<replace_with_keyFieldValue>>')?$expand=Trips($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Trips.get(0).TripId",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/People('lewisblack1')?$expand=Trips($select=TripId,Name)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("Trips.get(0).TripId",is(1))
	        .body("Trips.get(0).size()", is(2));
	        
	    
        
  
		
	     given()
	            .when()
	            .get("/trippin_metadata/People('<<replace_with_keyFieldValue>>')?$expand=Attachments")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Attachments.get(0).DocId",is(1));
	    given()
	            .when()
	            .get("/trippin_metadata/People('<<replace_with_keyFieldValue>>')?$expand=Attachments($top=1)")
	            .then()
	            .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	            .body("Attachments.get(0).DocId",is(1));
	    given()
	        .when()
	        .get("/trippin_metadata/People('lewisblack1')?$expand=Attachments($select=DocId,DocName)")
	        .then()
	        .statusCode(HttpStatusCode.fromStatusCode(200).getStatusCode())
	        .body("Attachments.get(0).DocId",is(1))
	        .body("Attachments.get(0).size()", is(2));
	        
	    
        
  
    } 
	
           
       
  
  
  
  
 
  @AfterEach
  void  teardown() {
    jdbcTemplate.execute("DELETE FROM trippin_metadata.Trip");
    jdbcTemplate.execute("DELETE FROM trippin_metadata.Airline");
    jdbcTemplate.execute("DELETE FROM trippin_metadata.AbstractPlanItem");
    jdbcTemplate.execute("DELETE FROM trippin_metadata.Airport");
    jdbcTemplate.execute("DELETE FROM trippin_metadata.Document");
    jdbcTemplate.execute("DELETE FROM trippin_metadata.AbstractPerson");
     jdbcTemplate.execute("DELETE FROM trippin_metadata.PersonEmails");
     jdbcTemplate.execute("DELETE FROM trippin_metadata.TripTravellers");
     jdbcTemplate.execute("DELETE FROM trippin_metadata.TripPlanItems");
     jdbcTemplate.execute("DELETE FROM trippin_metadata.ManagerDirectReports");
     jdbcTemplate.execute("DELETE FROM trippin_metadata.PersonAddressInfo");
     jdbcTemplate.execute("DELETE FROM trippin_metadata.TripTags");
     jdbcTemplate.execute("DELETE FROM trippin_metadata.EmployeePeers");
     jdbcTemplate.execute("DELETE FROM trippin_metadata.PersonFeatures");

    RestAssuredMockMvc.reset();
  }
}
