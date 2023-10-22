package de.wagner1975.eezycollectionz.collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
class CollectionServiceTest {

  private static final UUID DEFAULT_COLLECTION_ID = UUID.fromString("f3381a9d-ee1a-5fdc-aa1a-1ffab2acaf01");

  @Mock
  private CollectionRepository mockRepository;
  
  @Mock
  private CollectionIdProvider mockIdProvider;

  @InjectMocks
  private CollectionService objectUnderTest;

  @Test
  void findAll_Success_ReturnsList() {
    var id1 = UUID.fromString("00000001-1111-0000-0000-000000000001");
    var id2 = UUID.fromString("00000002-2222-0000-0000-000000000002");

    when(mockRepository.findAll())
      .thenReturn(List.of(
        Collection.builder().id(id1).build(),
        Collection.builder().id(id2).build()));
      
    var result = objectUnderTest.findAll();

    assertNotNull(result);
    assertEquals(result.size(), 2);
    assertEquals(result.get(0).getId(), id1);
    assertEquals(result.get(1).getId(), id2);
  }

  @Test
  void findById_IsFound_ReturnsCollection() {
    var id = DEFAULT_COLLECTION_ID;

    when(mockRepository.findById(eq(id))).thenReturn(Optional.of(Collection.builder().id(id).build()));

    var result = objectUnderTest.findById(id);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(id, result.get().getId());   
  }

  @Test
  void findById_NotFound_ReturnsEmpty() {
    when(mockRepository.findById(any())).thenReturn(Optional.empty());

    var result = objectUnderTest.findById(DEFAULT_COLLECTION_ID);

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
  void create_Saved_ReturnsCollection() {
    var name = "Shiny stuff";

    when(mockIdProvider.generateId()).thenReturn(DEFAULT_COLLECTION_ID);
    when(mockRepository.save(any(Collection.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var millisBefore = Instant.now().toEpochMilli();
    var result = objectUnderTest.create(CollectionInput.builder().name(name).build());
    var millisAfter = Instant.now().toEpochMilli();

    assertNotNull(result);
    assertTrue(result.isPresent());
    
    var savedCollection = result.get();
    assertEquals(DEFAULT_COLLECTION_ID, savedCollection.getId());
    assertEquals(name, savedCollection.getName());

    var createdAt = savedCollection.getCreatedAt();
    var lastModifiedAt = savedCollection.getLastModifiedAt();
    assertNotNull(createdAt);  
    assertTrue(createdAt.toEpochMilli() >= millisBefore && createdAt.toEpochMilli() <= millisAfter);
    assertEquals(createdAt, lastModifiedAt);
  }

  @Test
  void create_GenerateIdThrowsException_ReturnsEmpty() {
    when(mockIdProvider.generateId()).thenThrow(new GenerateIdException());

    var result = objectUnderTest.create(CollectionInput.builder().name("abc").build());

    assertNotNull(result);
    assertTrue(result.isEmpty());    
  }

  @Test
  void create_SaveReturnsNull_ReturnsEmpty() {
    when(mockIdProvider.generateId()).thenReturn(DEFAULT_COLLECTION_ID);
    when(mockRepository.save(any(Collection.class))).thenReturn(null);

    var result = objectUnderTest.create(CollectionInput.builder().name("abc").build());

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void update_Saved_ReturnsCollection() {
    var originalInstant = Instant.parse("2010-10-10T11:11:11.295558200Z");
    var originalCollection = Collection.builder()
      .id(DEFAULT_COLLECTION_ID)
      .createdAt(originalInstant)
      .lastModifiedAt(originalInstant)
      .name("Shiny stuff")
      .build();

    var modifiedName = "New words";

    when(mockRepository.findById(eq(DEFAULT_COLLECTION_ID))).thenReturn(Optional.of(originalCollection));
    when(mockRepository.save(any(Collection.class))).thenAnswer(invocation -> invocation.getArgument(0));    

    var millisBefore = Instant.now().toEpochMilli();
    var result = objectUnderTest.update(CollectionInput.builder().name(modifiedName).build(), DEFAULT_COLLECTION_ID);
    var millisAfter = Instant.now().toEpochMilli();

    assertNotNull(result);
    assertTrue(result.isPresent());
    
    var savedCollection = result.get();
    assertEquals(DEFAULT_COLLECTION_ID, savedCollection.getId());
    assertEquals(modifiedName, savedCollection.getName());

    var createdAt = savedCollection.getCreatedAt();
    assertNotNull(createdAt);
    assertEquals(originalInstant, createdAt); 

    var lastModifiedAt = savedCollection.getLastModifiedAt();
    assertNotNull(lastModifiedAt);
    assertNotEquals(originalInstant, lastModifiedAt); 
    assertTrue(lastModifiedAt.toEpochMilli() >= millisBefore && lastModifiedAt.toEpochMilli() <= millisAfter);
  }

  @Test
  void update_NotFound_ReturnsEmpty() {
    when(mockRepository.findById(any())).thenReturn(Optional.empty());

    var result = objectUnderTest.update(CollectionInput.builder().name("New words").build(), DEFAULT_COLLECTION_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void update_SaveReturnsNull_ReturnsEmpty() {
    when(mockRepository.findById(any())).thenReturn(Optional.of(Collection.builder()
      .id(DEFAULT_COLLECTION_ID)
      .name("Shiny stuff")
      .build()));    
    when(mockRepository.save(any(Collection.class))).thenReturn(null);

    var result = objectUnderTest.create(CollectionInput.builder().name("New words").build());

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }  

  @Test
  void delete_GivenIdIsUUID_MethodInvoked() {
    objectUnderTest.delete(DEFAULT_COLLECTION_ID);
    verify(mockRepository).deleteById(DEFAULT_COLLECTION_ID);
  }

  @Test
  void delete_GivenIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.delete(null);
    });
    assertEquals("id is null", exception.getMessage());     
  }
}
