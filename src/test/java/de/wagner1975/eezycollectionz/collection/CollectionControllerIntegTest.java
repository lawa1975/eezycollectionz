package de.wagner1975.eezycollectionz.collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static de.wagner1975.eezycollectionz.TestConstants.UUID_V4_REGEX;
import static de.wagner1975.eezycollectionz.TestConstants.ISO_8601_DATE_REGEX;
import static de.wagner1975.eezycollectionz.TestConstants.POSTGRESQL_DOCKER_IMAGE_NAME;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.wagner1975.eezycollectionz.support.TimeFactory;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/database/collection_controller_integ_test/before.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "/database/collection_controller_integ_test/after.sql")
@ActiveProfiles("test")
class CollectionControllerIntegTest {

  private static final String REQUEST_PATH = "/api/collections";

  @Container
  private static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>(POSTGRESQL_DOCKER_IMAGE_NAME);  

  @DynamicPropertySource
  private static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @LocalServerPort
  private Integer port;

  @Autowired
  private CollectionRepository repository;

  @Autowired
  private TimeFactory timeFactory;

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost:" + port; 
  }

  @Test
  void get_Success_Ok() {
    given().
      contentType(ContentType.JSON).
      param("page", 2).
      param("size", 3).
      param("sort", "name,asc").
    when().
      get(REQUEST_PATH).
    then().
      statusCode(200).
      body(
        "content", hasSize(3),
        "content[0].id", equalTo("10000003-3333-4000-8000-ddee00000003"),
        "content[0].name", equalTo("Collection M"),
        "content[1].id", equalTo("10000002-2222-4000-8000-ccdd00000002"),
        "content[1].name", equalTo("Collection N"),
        "content[2].id", equalTo("10000001-1111-4000-8000-bbcc00000001"),
        "content[2].name", equalTo("Collection O"));      
  }

  @Test
  void getById_Success_Ok() {
    given().
      contentType(ContentType.JSON).
      pathParam("id", "00000006-6666-4000-8000-eedd00000006").
    when().
      get(REQUEST_PATH + "/{id}").
    then().
      statusCode(200).
      body(
        "id", equalTo("00000006-6666-4000-8000-eedd00000006"),
        "name", equalTo("Collection T"));
  }  

  @Test
  void post_Success_Created() {
    var newName = "A brand new collection";

    var countBefore = repository.count();

    var timeBefore = timeFactory.now();

    var response = 
    given().
      contentType(ContentType.JSON).
      body(CollectionInput.builder().name(newName).build()).
    when().
      post(REQUEST_PATH).
    then().
      statusCode(201).
      body(
        "id", matchesPattern(UUID_V4_REGEX),
        "createdAt", matchesPattern(ISO_8601_DATE_REGEX),
        "lastModifiedAt", matchesPattern(ISO_8601_DATE_REGEX),
        "name", equalTo(newName)).
    extract().response();

    var timeAfter = timeFactory.now(); 
    
    var countAfter = countBefore + 1;
    assertEquals(countAfter, repository.count());

    String createdAtAsString = response.path("createdAt");
    String lastModifiedAtAsString = response.path("lastModifiedAt");
    assertEquals(createdAtAsString, lastModifiedAtAsString);

    var createdAt = Instant.parse(createdAtAsString);
    assertTrue(createdAt.compareTo(timeBefore) >= 0);
    assertTrue(createdAt.compareTo(timeAfter) <= 0);
    
    String newIdAsString = response.path("id");

    given().
      contentType(ContentType.JSON).
      pathParam("id", newIdAsString).
    when().
      get(REQUEST_PATH + "/{id}").
    then().
      statusCode(200).
      body(
        "id", equalTo(newIdAsString),
        "createdAt", equalTo(createdAtAsString),
        "lastModifiedAt", equalTo(lastModifiedAtAsString),
        "name", equalTo(newName));    
  }

  @Test
  void put_Success_Ok() {
    var newName = "Another freaky collection name";

    var existingIdAsString = "00000004-4444-4000-8000-eeff00000004";
    
    var responseBefore =
    given().
      contentType(ContentType.JSON).
      pathParam("id", existingIdAsString).
    when().
      get(REQUEST_PATH + "/{id}").
    then().
      statusCode(200).
      body(
        "id", equalTo(existingIdAsString),
        "name", not(equalTo(newName))).
    extract().response();

    String createdAtBeforeAsString = responseBefore.path("createdAt");
    String lastModifiedAtBeforeAsString = responseBefore.path("lastModifiedAt");    
    
    var countBefore = repository.count();
    var timeBefore = timeFactory.now();

    var response = 
    given().
      contentType(ContentType.JSON).
      pathParam("id", existingIdAsString).
      body(CollectionInput.builder().name(newName).build()).
    when().
      put(REQUEST_PATH + "/{id}").
    then().
      statusCode(200).
      body(
        "id", equalTo(existingIdAsString),
        "createdAt", equalTo(createdAtBeforeAsString),
        "lastModifiedAt", matchesPattern(ISO_8601_DATE_REGEX),
        "name", equalTo(newName)).
    extract().response();

    var timeAfter = timeFactory.now(); 

    assertEquals(countBefore, repository.count());

    String lastModifiedAtAsString = response.path("lastModifiedAt");
    assertNotEquals(lastModifiedAtBeforeAsString, lastModifiedAtAsString);

    var lastModifiedAt = Instant.parse(lastModifiedAtAsString);
    assertTrue(lastModifiedAt.compareTo(timeBefore) >= 0);
    assertTrue(lastModifiedAt.compareTo(timeAfter) <= 0);

    given().
      contentType(ContentType.JSON).
      pathParam("id", existingIdAsString).
    when().
      get(REQUEST_PATH + "/{id}").
    then().
      statusCode(200).
      body(
        "id", equalTo(existingIdAsString),
        "createdAt", equalTo(createdAtBeforeAsString),
        "lastModifiedAt", equalTo(lastModifiedAtAsString),
        "name", equalTo(newName));      
  }

  @Test
  void delete_Success_NoContent()  {
    var existingIdAsString = "00000002-2222-4000-8000-ccdd00000002";
    
    given().
      contentType(ContentType.JSON).
      pathParam("id", existingIdAsString).
    when().
      get(REQUEST_PATH + "/{id}").
    then().
      statusCode(200).
      body("id", equalTo(existingIdAsString));

    var countBefore = repository.count();

    given().
      contentType(ContentType.JSON).
      pathParam("id", existingIdAsString).
    when().
      delete(REQUEST_PATH + "/{id}").
    then().
      statusCode(204);

    var countAfter = countBefore - 1;
    assertEquals(countAfter, repository.count());    
      
    given().
      contentType(ContentType.JSON).
      pathParam("id", existingIdAsString).
    when().
      get(REQUEST_PATH + "/{id}").
    then().
      statusCode(404);
  } 
}
