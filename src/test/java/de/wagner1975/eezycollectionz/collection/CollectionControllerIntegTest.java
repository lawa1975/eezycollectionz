package de.wagner1975.eezycollectionz.collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CollectionControllerIntegTest {

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
      createCollection("00000000-0000-aabb-0000-000000000000", "Liste Z"),
      createCollection("00000001-1111-bbcc-0000-000000000001", "Liste Y"),
      createCollection("00000002-2222-ccdd-0000-000000000002", "Liste X"),
      createCollection("00000003-3333-ddee-0000-000000000003", "Liste W"),
      createCollection("00000004-4444-eeff-0000-000000000004", "Liste V"),
      createCollection("00000005-5555-ffee-0000-000000000005", "Liste U"),
      createCollection("00000006-6666-eedd-0000-000000000006", "Liste T"),
      createCollection("00000007-7777-ddcc-0000-000000000007", "Liste S"),
      createCollection("00000008-8888-ccbb-0000-000000000008", "Liste R"),
      createCollection("00000009-9999-bbaa-0000-000000000009", "Liste Q"),
      createCollection("10000000-0000-aabb-0000-000000000000", "Liste P"),
      createCollection("10000001-1111-bbcc-0000-000000000001", "Liste O"),
      createCollection("10000002-2222-ccdd-0000-000000000002", "Liste N"),
      createCollection("10000003-3333-ddee-0000-000000000003", "Liste M"),
      createCollection("10000004-4444-eeff-0000-000000000004", "Liste L"),
      createCollection("10000005-5555-ffee-0000-000000000005", "Liste K"),
      createCollection("10000006-6666-eedd-0000-000000000006", "Liste J"),
      createCollection("10000007-7777-ddcc-0000-000000000007", "Liste I"),
      createCollection("10000008-8888-ccbb-0000-000000000008", "Liste H"),
      createCollection("10000009-9999-bbaa-0000-000000000009", "Liste G"));      

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
      get("/api/collections").
    then().
      statusCode(200).
      body("content", hasSize(3)).
      body("content[0].id", equalTo("10000003-3333-ddee-0000-000000000003")).
      body("content[0].name", equalTo("Liste M")).
      body("content[1].id", equalTo("10000002-2222-ccdd-0000-000000000002")).
      body("content[1].name", equalTo("Liste N")).
      body("content[2].id", equalTo("10000001-1111-bbcc-0000-000000000001")).
      body("content[2].name", equalTo("Liste O"));      
  }

  @Test
  void getById_Success_Ok() {
    given().
      contentType(ContentType.JSON).
      pathParam("id", "00000006-6666-eedd-0000-000000000006").
    when().
      get("/api/collections/{id}").
    then().
      statusCode(200).
      body("id", equalTo("00000006-6666-eedd-0000-000000000006")).
      body("name", equalTo("Liste T"));     
  }  

  @Test
  @Transactional
  @Rollback
  void post_Success_Created() {
    long countBefore = repository.count();
    long countAfter = countBefore + 1;

    given().
      contentType(ContentType.JSON).
      body(CollectionInput.builder().name("Neue Liste").build()).
    when().
      post("/api/collections").
    then().
      statusCode(201);

    assertEquals(countAfter, repository.count());      
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
