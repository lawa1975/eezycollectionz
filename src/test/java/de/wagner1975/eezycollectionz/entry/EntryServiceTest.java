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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.wagner1975.eezycollectionz.collection.Collection;
import de.wagner1975.eezycollectionz.support.GenerateIdException;
import de.wagner1975.eezycollectionz.support.TimeFactory;

@ExtendWith(SpringExtension.class)
class EntryServiceTest {

  private static final UUID DEFAULT_COLLECTION_ID = UUID.fromString("f3381a9d-ee1a-5fdc-aa1a-1ffab2acaf01");
  private static final UUID DEFAULT_ENTRY_ID = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");
  private static final Instant INSTANT_NOW = Instant.parse("2023-12-13T08:11:22.963Z");
  private static final Instant INSTANT_PAST = Instant.parse("2023-12-12T07:35:54.864Z");  

  private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(1, 2, Sort.by(Direction.ASC, "id"));

  @Mock
  private EntryRepository repositoryMock;
  
  @Mock
  private EntryIdProvider idProviderMock;

  @Mock
  private PageImpl<Entry> pageMock;

  @Mock
  private TimeFactory timeFactoryMock;  

  @InjectMocks
  private EntryService objectUnderTest;
  
  @Test
  void findByCollectionId_IsFound_ReturnsPageWithEntries() {
    var id1 = UUID.fromString("00000001-1111-0000-0000-000000000001");
    var id2 = UUID.fromString("00000002-2222-0000-0000-000000000002");

    when(pageMock.getContent())
      .thenReturn(List.of(
        Entry.builder().id(id1).build(),
        Entry.builder().id(id2).build()));

    when(repositoryMock.findByCollectionId(eq(DEFAULT_COLLECTION_ID), eq(DEFAULT_PAGE_REQUEST)))
      .thenReturn(pageMock);

    var result = objectUnderTest.findByCollectionId(DEFAULT_COLLECTION_ID, DEFAULT_PAGE_REQUEST);

    assertNotNull(result);
    var content = result.getContent();
    assertNotNull(content);
    assertEquals(content.size(), 2);
    assertEquals(content.get(0).getId(), id1);
    assertEquals(content.get(1).getId(), id2);
  }

  @Test
  void findByCollectionId_NotFound_ReturnsEmptyPage() {
    when(pageMock.getContent())
      .thenReturn(List.of());

    when(repositoryMock.findByCollectionId(any(UUID.class), any(Pageable.class)))
      .thenReturn(pageMock);

    var result = objectUnderTest.findByCollectionId(DEFAULT_COLLECTION_ID, DEFAULT_PAGE_REQUEST);

    assertNotNull(result);
    var content = result.getContent();
    assertNotNull(content);
    assertEquals(content.size(), 0);
  }

  @Test
  void findByCollectionId_GivenCollectionIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.findByCollectionId(null, DEFAULT_PAGE_REQUEST);
    });
    assertEquals("collectionId is null", exception.getMessage());     
  }
  
  @Test
  void findByCollectionId_GivenPageableIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.findByCollectionId(DEFAULT_COLLECTION_ID, null);
    });
    assertEquals("pageable is null", exception.getMessage());     
  }   

  @Test
  void findById_IsFound_ReturnsCollection() {
    var id = DEFAULT_ENTRY_ID;

    when(repositoryMock.findById(eq(id))).thenReturn(Optional.of(Entry.builder().id(id).build()));

    var result = objectUnderTest.findById(id);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(id, result.get().getId());   
  }

  @Test
  void findById_NotFound_ReturnsEmpty() {
    when(repositoryMock.findById(any())).thenReturn(Optional.empty());

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

    when(idProviderMock.generateId()).thenReturn(DEFAULT_ENTRY_ID);
    when(timeFactoryMock.now()).thenReturn(INSTANT_NOW);
    when(repositoryMock.save(any(Entry.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var result = objectUnderTest.create(EntryInput.builder().name(name).build(), DEFAULT_COLLECTION_ID);

    assertNotNull(result);
    assertTrue(result.isPresent());
    
    var savedEntry = result.get();
    assertNotNull(savedEntry);     
    assertEquals(DEFAULT_ENTRY_ID, savedEntry.getId());
    assertEquals(name, savedEntry.getName());
    assertEquals(INSTANT_NOW, savedEntry.getCreatedAt());
    assertEquals(INSTANT_NOW, savedEntry.getLastModifiedAt());

    var collection = savedEntry.getCollection();
    assertNotNull(collection);
    assertEquals(DEFAULT_COLLECTION_ID, collection.getId());
  }

  @Test
  void create_GenerateIdThrowsException_ReturnsEmpty() {
    when(idProviderMock.generateId()).thenThrow(new GenerateIdException());

    var result = objectUnderTest.create(
      EntryInput.builder().name("xyz").build(),
      DEFAULT_COLLECTION_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());    
  }

  @Test
  void create_SaveReturnsNull_ReturnsEmpty() {
    when(idProviderMock.generateId()).thenReturn(DEFAULT_ENTRY_ID);
    when(repositoryMock.save(any(Entry.class))).thenReturn(null);

    var result = objectUnderTest.create(
      EntryInput.builder().name("xyz").build(),
      DEFAULT_COLLECTION_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void create_GivenEntryInputIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.create(null, DEFAULT_COLLECTION_ID);
    });
    assertEquals("entryInput is null", exception.getMessage());     
  }

  @Test
  void create_GivenCollectionIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.create(EntryInput.builder().name("xyz").build(), null);
    });
    assertEquals("collectionId is null", exception.getMessage());     
  } 

  @Test
  void update_Saved_ReturnsEntry() {
    var originalEntry = Entry.builder()
      .id(DEFAULT_ENTRY_ID)
      .createdAt(INSTANT_PAST)
      .lastModifiedAt(INSTANT_PAST)
      .name("Shiny stuff")
      .collection(Collection.builder().id(DEFAULT_COLLECTION_ID).build())
      .build();
      
    var modifiedName = "New words";

    when(repositoryMock.findById(eq(DEFAULT_ENTRY_ID))).thenReturn(Optional.of(originalEntry));
    when(timeFactoryMock.now()).thenReturn(INSTANT_NOW);
    when(repositoryMock.save(any(Entry.class))).thenAnswer(invocation -> invocation.getArgument(0));
    
    var result = objectUnderTest.update(EntryInput.builder().name(modifiedName).build(), DEFAULT_ENTRY_ID);

    assertNotNull(result);
    assertTrue(result.isPresent());
    
    var savedEntry = result.get();
    assertNotNull(savedEntry);
    assertEquals(DEFAULT_ENTRY_ID, savedEntry.getId());
    assertEquals(modifiedName, savedEntry.getName());
    assertEquals(INSTANT_PAST, savedEntry.getCreatedAt());
    assertEquals(INSTANT_NOW, savedEntry.getLastModifiedAt());     

    var relatedCollection = savedEntry.getCollection();
    assertNotNull(relatedCollection);
    assertEquals(DEFAULT_COLLECTION_ID, relatedCollection.getId());
  }

  @Test
  void update_NotFound_ReturnsEmpty() {
    when(repositoryMock.findById(any())).thenReturn(Optional.empty());

    var result = objectUnderTest.update(EntryInput.builder().name("New words").build(), DEFAULT_ENTRY_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void update_SaveReturnsNull_ReturnsEmpty() {
    when(repositoryMock.findById(eq(DEFAULT_ENTRY_ID))).thenReturn(Optional.of(Entry.builder().id(DEFAULT_ENTRY_ID).build()));    
    when(repositoryMock.save(any(Entry.class))).thenReturn(null);

    var result = objectUnderTest.update(EntryInput.builder().name("New words").build(), DEFAULT_ENTRY_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void update_GivenEntryInputIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.update(null, DEFAULT_ENTRY_ID);
    });
    assertEquals("entryInput is null", exception.getMessage());     
  }

  @Test
  void update_GivenIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.update(EntryInput.builder().name("New words").build(), null);
    });
    assertEquals("id is null", exception.getMessage());     
  }  

  @Test
  void delete_GivenIdIsUUID_MethodInvoked() {
    objectUnderTest.delete(DEFAULT_ENTRY_ID);
    verify(repositoryMock).deleteById(DEFAULT_ENTRY_ID);
  }

  @Test
  void delete_GivenIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.delete(null);
    });
    assertEquals("id is null", exception.getMessage());     
  }
}
