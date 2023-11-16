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
  }

  @Test
  void get_Success_Ok() {
    var now = Instant.now();
    var id1 = "00000001-1111-aaaa-0000-000000000001";
    var id2 = "00000002-2222-bbbb-0000-000000000002";    

    var collections = List.of(
      Collection.builder()
        .id(UUID.fromString(id1))
        .createdAt(now)
        .lastModifiedAt(now)
        .name("Liste A")
        .build(),
      Collection.builder()
        .id(UUID.fromString(id2))
        .createdAt(now)
        .lastModifiedAt(now)
        .name("Liste B")
        .build());        

    repository.saveAll(collections);

    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/api/collections")
      .then()
      .statusCode(200)
      .body("content", hasSize(2));
  }  
}
