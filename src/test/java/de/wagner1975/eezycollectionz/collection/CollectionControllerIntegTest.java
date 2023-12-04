package de.wagner1975.eezycollectionz.collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CollectionControllerIntegTest {

  private static final String REQUEST_PATH = "/api/collections";  

  @Container
  private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1-alpine3.18");  

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

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost:" + port;
  
    repository.deleteAll();

    var collections = List.of(
      createCollection("00000000-0000-aabb-0000-000000000000", "Collection Z"),
      createCollection("00000001-1111-bbcc-0000-000000000001", "Collection Y"),
      createCollection("00000002-2222-ccdd-0000-000000000002", "Collection X"),
      createCollection("00000003-3333-ddee-0000-000000000003", "Collection W"),
      createCollection("00000004-4444-eeff-0000-000000000004", "Collection V"),
      createCollection("00000005-5555-ffee-0000-000000000005", "Collection U"),
      createCollection("00000006-6666-eedd-0000-000000000006", "Collection T"),
      createCollection("00000007-7777-ddcc-0000-000000000007", "Collection S"),
      createCollection("00000008-8888-ccbb-0000-000000000008", "Collection R"),
      createCollection("00000009-9999-bbaa-0000-000000000009", "Collection Q"),
      createCollection("10000000-0000-aabb-0000-000000000000", "Collection P"),
      createCollection("10000001-1111-bbcc-0000-000000000001", "Collection O"),
      createCollection("10000002-2222-ccdd-0000-000000000002", "Collection N"),
      createCollection("10000003-3333-ddee-0000-000000000003", "Collection M"),
      createCollection("10000004-4444-eeff-0000-000000000004", "Collection L"),
      createCollection("10000005-5555-ffee-0000-000000000005", "Collection K"),
      createCollection("10000006-6666-eedd-0000-000000000006", "Collection J"),
      createCollection("10000007-7777-ddcc-0000-000000000007", "Collection I"),
      createCollection("10000008-8888-ccbb-0000-000000000008", "Collection H"),
      createCollection("10000009-9999-bbaa-0000-000000000009", "Collection G"));      

    repository.saveAll(collections);
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
        "content[0].id", equalTo("10000003-3333-ddee-0000-000000000003"),
        "content[0].name", equalTo("Collection M"),
        "content[1].id", equalTo("10000002-2222-ccdd-0000-000000000002"),
        "content[1].name", equalTo("Collection N"),
        "content[2].id", equalTo("10000001-1111-bbcc-0000-000000000001"),
        "content[2].name", equalTo("Collection O"));      
  }

  @Test
  void getById_Success_Ok() {
    given().
      contentType(ContentType.JSON).
      pathParam("id", "00000006-6666-eedd-0000-000000000006").
    when().
      get(REQUEST_PATH + "/{id}").
    then().
      statusCode(200).
      body(
        "id", equalTo("00000006-6666-eedd-0000-000000000006"),
        "name", equalTo("Collection T"));
  }  

  @Test
  @Transactional
  @Rollback
  void post_Success_Created() {
    long countBefore = repository.count();
    String newName = "A brand new collection";

    Response response = 
    given().
      contentType(ContentType.JSON).
      body(CollectionInput.builder().name(newName).build()).
    when().
      post(REQUEST_PATH).
    then().
      statusCode(201).
      body(
        "id", not(equalTo(null)),
        "createdAt", not(equalTo(null)),
        "lastModifiedAt", not(equalTo(null)),
        "name", equalTo(newName)).
    extract().response();

    long countAfter = countBefore + 1;
    String newIdAsString = response.path("id");
    String createdAtAsString = response.path("createdAt");
    String lastModifiedAtAsString = response.path("lastModifiedAt");

    assertEquals(countAfter, repository.count());
    assertEquals(createdAtAsString, lastModifiedAtAsString);
    
    given().
      contentType(ContentType.JSON).
      pathParam("id", newIdAsString).
    when().
      get("/api/collections/{id}").
    then().
      statusCode(200).
      body(
        "id", equalTo(newIdAsString),
        "createdAt", equalTo(createdAtAsString),
        "lastModifiedAt", equalTo(lastModifiedAtAsString),
        "name", equalTo(newName));    
  }

  private Collection createCollection(String id, String name) {
    var now = Instant.now();
    return Collection.builder()
      .id(UUID.fromString(id))
      .createdAt(now)
      .lastModifiedAt(now)
      .name(name)
      .build();    
  }
}
