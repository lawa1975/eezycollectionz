package de.wagner1975.eezycollectionz.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.wagner1975.eezycollectionz.support.GenerateIdException;

@ExtendWith(SpringExtension.class)
public class EntryServiceTest {

  private static final UUID DEFAULT_COLLECTION_ID = UUID.fromString("f3381a9d-ee1a-5fdc-aa1a-1ffab2acaf01");
  private static final UUID DEFAULT_ENTRY_ID = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");

  @Mock
  private EntryRepository mockRepository;
  
  @Mock
  private EntryIdProvider mockIdProvider;

  @InjectMocks
  private EntryService objectUnderTest;
  
  @Test
  void findByCollectionId_IsFound_ReturnsList() {
    var id1 = UUID.fromString("00000001-1111-0000-0000-000000000001");
    var id2 = UUID.fromString("00000002-2222-0000-0000-000000000002");

    when(mockRepository.findByCollectionId(eq(DEFAULT_COLLECTION_ID)))
      .thenReturn(List.of(
        Entry.builder().id(id1).build(),
        Entry.builder().id(id2).build()));

    var result = objectUnderTest.findByCollectionId(DEFAULT_COLLECTION_ID);

    assertNotNull(result);
    assertEquals(result.size(), 2);
    assertEquals(result.get(0).getId(), id1);
    assertEquals(result.get(1).getId(), id2); 
  }

  @Test
  void findByCollectionId_NotFound_ReturnsEmpty() {
    when(mockRepository.findByCollectionId(any())).thenReturn(List.of());

    var result = objectUnderTest.findByCollectionId(DEFAULT_COLLECTION_ID);

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
    var id = DEFAULT_ENTRY_ID;

    when(mockRepository.findById(eq(id))).thenReturn(Optional.of(Entry.builder().id(id).build()));

    var result = objectUnderTest.findById(id);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(id, result.get().getId());   
  }

  @Test
  void findById_NotFound_ReturnsEmpty() {
    when(mockRepository.findById(any())).thenReturn(Optional.empty());

    var result = objectUnderTest.findById(DEFAULT_ENTRY_ID);

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
  void create_Saved_ReturnsEntry() {
    var name = "Shiny stuff";

    when(mockIdProvider.generateId()).thenReturn(DEFAULT_ENTRY_ID);
    when(mockRepository.save(any(Entry.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var millisBefore = Instant.now().toEpochMilli();
    var result = objectUnderTest.create(EntryInput.builder().name(name).build(), DEFAULT_COLLECTION_ID);
    var millisAfter = Instant.now().toEpochMilli();

    assertNotNull(result);
    assertTrue(result.isPresent());
    
    var savedEntry = result.get();
    assertEquals(DEFAULT_ENTRY_ID, savedEntry.getId());
    assertEquals(name, savedEntry.getName());

    var createdAt = savedEntry.getCreatedAt();
    var lastModifiedAt = savedEntry.getLastModifiedAt();
    assertNotNull(createdAt);  
    assertTrue(createdAt.toEpochMilli() >= millisBefore && createdAt.toEpochMilli() <= millisAfter);
    assertEquals(createdAt, lastModifiedAt);

    var collection = savedEntry.getCollection();
    assertNotNull(collection);
    assertEquals(DEFAULT_COLLECTION_ID, collection.getId());
  }

  @Test
  void create_GenerateIdThrowsException_ReturnsEmpty() {
    when(mockIdProvider.generateId()).thenThrow(new GenerateIdException());

    var result = objectUnderTest.create(
      EntryInput.builder().name("xyz").build(),
      DEFAULT_COLLECTION_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());    
  }

  @Test
  void create_SaveReturnsNull_ReturnsEmpty() {
    when(mockIdProvider.generateId()).thenReturn(DEFAULT_ENTRY_ID);
    when(mockRepository.save(any(Entry.class))).thenReturn(null);

    var result = objectUnderTest.create(
      EntryInput.builder().name("xyz").build(),
      DEFAULT_COLLECTION_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void create_GivenCollectionIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.create(EntryInput.builder().name("xyz").build(), null);
    });
    assertEquals("collectionId is null", exception.getMessage());     
  } 

  @Test
  void delete_GivenIdIsUUID_MethodInvoked() {
    objectUnderTest.delete(DEFAULT_ENTRY_ID);
    verify(mockRepository).deleteById(DEFAULT_ENTRY_ID);
  }

  @Test
  void delete_GivenIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.delete(null);
    });
    assertEquals("id is null", exception.getMessage());     
  }
}
