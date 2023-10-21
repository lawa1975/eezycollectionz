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

  @Mock
  private CollectionRepository mockRepository;
  
  @Mock
  private CollectionIdProvider mockIdProvider;

  @InjectMocks
  private CollectionService objectUnderTest;

  @Test
  void findAll_Success_ReturnsList() {
    var id1 = UUID.fromString("992e4141-add3-49ba-875b-d92da4ea9a18");
    var id2 = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");

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
    var id = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");

    when(mockRepository.findById(eq(id))).thenReturn(Optional.of(Collection.builder().id(id).build()));

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
  void create_Saved_ReturnsCollection() {
    var id = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");
    var name = "Shiny stuff";

    when(mockIdProvider.generateId()).thenReturn(id);
    when(mockRepository.save(any(Collection.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var millisBefore = Instant.now().toEpochMilli();
    var result = objectUnderTest.create(CollectionInput.builder().name(name).build());
    var millisAfter = Instant.now().toEpochMilli();

    assertNotNull(result);
    assertTrue(result.isPresent());
    
    var savedCollection = result.get();
    assertEquals(id, savedCollection.getId());
    assertEquals(name, savedCollection.getName());

    var createdAt = savedCollection.getCreatedAt();
    var lastModifiedAt = savedCollection.getLastModifiedAt();
    assertNotNull(createdAt);  
    assertTrue(createdAt.toEpochMilli() >= millisBefore && createdAt.toEpochMilli() <= millisAfter);
    assertEquals(createdAt, lastModifiedAt);
  }

  @Test
  void create_GeneratedIdThrowsException_ReturnsEmpty() {
    when(mockIdProvider.generateId()).thenThrow(new GenerateIdException());

    var result = objectUnderTest.create(CollectionInput.builder().name("abc").build());

    assertNotNull(result);
    assertTrue(result.isEmpty());    
  }

  @Test
  void create_SaveReturnsNull_ReturnsEmpty() {
    when(mockIdProvider.generateId()).thenReturn(UUID.fromString("c725efeb-de77-46df-916a-2fc195376386"));
    when(mockRepository.save(any(Collection.class))).thenReturn(null);

    var result = objectUnderTest.create(CollectionInput.builder().name("abc").build());

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void update_Saved_ReturnsCollection() {
    var id = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");
    
    var originalInstant = Instant.parse("2010-10-10T11:11:11.295558200Z");
    var originalCollection = Collection.builder()
      .id(id)
      .createdAt(originalInstant)
      .lastModifiedAt(originalInstant)
      .name("Shiny stuff")
      .build();

    var modifiedName = "New words";

    when(mockRepository.findById(eq(id))).thenReturn(Optional.of(originalCollection));
    when(mockRepository.save(any(Collection.class))).thenAnswer(invocation -> invocation.getArgument(0));    

    var millisBefore = Instant.now().toEpochMilli();
    var result = objectUnderTest.update(CollectionInput.builder().name(modifiedName).build(), id);
    var millisAfter = Instant.now().toEpochMilli();

    assertNotNull(result);
    assertTrue(result.isPresent());
    
    var savedCollection = result.get();
    assertEquals(id, savedCollection.getId());
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

    var result = objectUnderTest.update(
      CollectionInput.builder().name("New words").build(),
      UUID.fromString("c725efeb-de77-46df-916a-2fc195376386"));

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void update_SaveReturnsNull_ReturnsEmpty() {
    when(mockRepository.findById(any())).thenReturn(Optional.of(Collection.builder()
      .id(UUID.fromString("c725efeb-de77-46df-916a-2fc195376386"))
      .name("Shiny stuff")
      .build()));    
    when(mockRepository.save(any(Collection.class))).thenReturn(null);

    var result = objectUnderTest.create(CollectionInput.builder().name("New words").build());

    assertNotNull(result);
    assertTrue(result.isEmpty());
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
