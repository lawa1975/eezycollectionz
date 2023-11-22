package de.wagner1975.eezycollectionz.collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
      createCollection("00000009-9999-bbaa-0000-000000000009", "Liste Q"));

    repository.saveAll(collections);
  }

  @Test
  void get_Success_Ok() {
    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/api/collections")
      .then()
      .statusCode(200)
      .body("content", hasSize(10));
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
