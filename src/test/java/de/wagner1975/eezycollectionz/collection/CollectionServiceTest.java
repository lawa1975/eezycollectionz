package de.wagner1975.eezycollectionz.collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class CollectionServiceTest {

  private CollectionService objectUnderTest;

  @Mock
  private CollectionRepository mockRepository;
  
  @Mock
  private CollectionIdProvider mockIdProvider;
  
  @BeforeEach
  void beforeEach() {
    objectUnderTest = new CollectionService(mockRepository, mockIdProvider);
  }

  @Test
  void findAll_Success_ReturnsList() {
    UUID id1 = UUID.fromString("992e4141-add3-49ba-875b-d92da4ea9a18");
    UUID id2 = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");

    when(mockRepository.findAll())
      .thenReturn(List.of(
        Collection.builder().id(id1).build(),
        Collection.builder().id(id2).build()));
      
      var results = objectUnderTest.findAll();

      assertNotNull(results);
      assertEquals(results.size(), 2);
      assertEquals(results.get(0).getId(), id1);
      assertEquals(results.get(1).getId(), id2);
  }

  @Test
  void findById_IsFound_ReturnsCollection() {
    UUID id = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");

    when(mockRepository.findById(eq(id))).thenReturn(Optional.of(Collection.builder().id(id).build()));

    var result = objectUnderTest.findById(id);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(id, result.get().getId());   
  }

  @Test
  void findById_IsNotFound_ReturnsEmpty() {
    when(mockRepository.findById(any())).thenReturn(Optional.empty());

    var result = objectUnderTest.findById(UUID.fromString("c725efeb-de77-46df-916a-2fc195376386"));

    assertNotNull(result);
    assertTrue(result.isEmpty());  
  }  
}
