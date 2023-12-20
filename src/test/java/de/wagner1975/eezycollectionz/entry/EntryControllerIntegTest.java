package de.wagner1975.eezycollectionz.entry;

import static de.wagner1975.eezycollectionz.TestConstants.POSTGRESQL_DOCKER_IMAGE_NAME;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/database/entry_controller_integ_test/before.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "/database/entry_controller_integ_test/after.sql")
@ActiveProfiles("test")
class EntryControllerIntegTest {

  private static final String REQUEST_PATH = "/api/entries";

  @Container
  private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRESQL_DOCKER_IMAGE_NAME);  

  @DynamicPropertySource
  private static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @LocalServerPort
  private Integer port;

  @Autowired
  private EntryRepository repository;  
  
  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost:" + port;
  }

  @Test
  void getByCollectionId_Success_Ok() {
    given().
      contentType(ContentType.JSON).
      param("collectionId", "10000000-a000-4000-8000-10000000a000").
      param("page", 1).
      param("size", 3).
      param("sort", "name,asc").
    when().
      get(REQUEST_PATH).
    then().
      statusCode(200).
      body(
        "content", hasSize(3),
        "content[0].id", equalTo("20000000-b400-4000-8000-20000000b400"),
        "content[0].name", equalTo("Entry V (B)"),
        "content[1].id", equalTo("20000000-b300-4000-8000-20000000b300"),
        "content[1].name", equalTo("Entry W (B)"),
        "content[2].id", equalTo("20000000-b200-4000-8000-20000000b200"),
        "content[2].name", equalTo("Entry X (B)"));            
  }
  
  @Test
  void getById_Success_Ok() {
    given().
      contentType(ContentType.JSON).
      pathParam("id", "20000000-ba00-4000-8000-20000000ba00").
    when().
      get(REQUEST_PATH + "/{id}").
    then().
      statusCode(200).
      body(
        "id", equalTo("20000000-ba00-4000-8000-20000000ba00"),
        "name", equalTo("Entry P (A)"));
  }
  
  @Test
  void delete_Success_NoContent()  {
    var existingIdAsString = "20000000-b600-4000-8000-20000000b600";
    
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
