package de.wagner1975.eezycollectionz.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class EntryServiceTest {

  @Mock
  private EntryRepository mockRepository;
  
  @Mock
  private EntryIdProvider mockIdProvider;

  @InjectMocks
  private EntryService objectUnderTest;
  
  @Test
  void findByCollectionId_IsFound_ReturnsList() {
    var collectionId = UUID.fromString("f3381a9d-ee1a-5fdc-aa1a-1ffab2acaf01");
    var id1 = UUID.fromString("992e4141-add3-49ba-875b-d92da4ea9a18");
    var id2 = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");

    when(mockRepository.findByCollectionId(eq(collectionId)))
      .thenReturn(List.of(
        Entry.builder().id(id1).build(),
        Entry.builder().id(id2).build()));

    var result = objectUnderTest.findByCollectionId(collectionId);

    assertNotNull(result);
    assertEquals(result.size(), 2);
    assertEquals(result.get(0).getId(), id1);
    assertEquals(result.get(1).getId(), id2); 
  }

  @Test
  void findByCollectionId_NotFound_ReturnsEmpty() {
    when(mockRepository.findByCollectionId(any())).thenReturn(List.of());

    var result = objectUnderTest.findByCollectionId(UUID.fromString("f3381a9d-ee1a-5fdc-aa1a-1ffab2acaf01"));

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void findByCollectionId_GivenCollectionIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.findByCollectionId(null);
    });
    assertEquals("collectionId is null", exception.getMessage());     
  }  

  @Test
  void findById_IsFound_ReturnsCollection() {
    var id = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");

    when(mockRepository.findById(eq(id))).thenReturn(Optional.of(Entry.builder().id(id).build()));

    var result = objectUnderTest.findById(id);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(id, result.get().getId());   
  }

  @Test
  void findById_NotFound_ReturnsEmpty() {
    when(mockRepository.findById(any())).thenReturn(Optional.empty());

    var result = objectUnderTest.findById(UUID.fromString("c725efeb-de77-46df-916a-2fc195376386"));

    assertNotNull(result);
    assertTrue(result.isEmpty());  
  }

  @Test
  void findById_GivenIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.findById(null);
    });
    assertEquals("id is null", exception.getMessage());     
  }

  @Test
  void delete_GivenIdIsUUID_MethodInvoked() {
    var id = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");
    objectUnderTest.delete(id);
    verify(mockRepository).deleteById(id);
  }

  @Test
  void delete_GivenIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.delete(null);
    });
    assertEquals("id is null", exception.getMessage());     
  }
}
