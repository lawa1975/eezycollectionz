package de.wagner1975.eezycollectionz.collection;

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
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.wagner1975.eezycollectionz.support.GenerateIdException;
import de.wagner1975.eezycollectionz.support.TimeFactory;

@ExtendWith(SpringExtension.class)
class CollectionServiceTest {

  private static final UUID DEFAULT_ID = UUID.fromString("f3381a9d-ee1a-5fdc-aa1a-1ffab2acaf01");
  private static final Instant INSTANT_NOW = Instant.parse("2023-12-13T08:11:22.963Z");
  private static final Instant INSTANT_PAST = Instant.parse("2023-12-12T07:35:54.864Z");

  @Mock
  private CollectionRepository repositoryMock;
  
  @Mock
  private CollectionIdProvider idProviderMock;

  @Mock
  private PageImpl<Collection> pageMock;

  @Mock
  private TimeFactory timeFactoryMock;

  @InjectMocks
  private CollectionService objectUnderTest;

  @Test
  void findAll_Success_ReturnsPageWithCollections() {
    var id1 = UUID.fromString("00000001-1111-0000-0000-000000000001");
    var id2 = UUID.fromString("00000002-2222-0000-0000-000000000002");

    var pageable = PageRequest.of(1, 2, Sort.by(Direction.ASC, "id"));

    when(pageMock.getContent())
      .thenReturn(List.of(
        Collection.builder().id(id1).build(),
        Collection.builder().id(id2).build()));

    when(repositoryMock.findAll(pageable)).thenReturn(pageMock);
 
    var result = objectUnderTest.findAll(pageable);

    assertNotNull(result);
    var content = result.getContent();
    assertNotNull(content);
    assertEquals(content.size(), 2);
    assertEquals(content.get(0).getId(), id1);
    assertEquals(content.get(1).getId(), id2);
  }  

  @Test
  void findAll_GivenPageableIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.findAll(null);
    });
    assertEquals("pageable is null", exception.getMessage());     
  }

  @Test
  void findById_IsFound_ReturnsCollection() {
    var id = DEFAULT_ID;

    when(repositoryMock.findById(eq(id))).thenReturn(Optional.of(Collection.builder().id(id).build()));

    var result = objectUnderTest.findById(id);

    assertNotNull(result);
    assertTrue(result.isPresent());
    assertEquals(id, result.get().getId());   
  }

  @Test
  void findById_NotFound_ReturnsEmpty() {
    when(repositoryMock.findById(any())).thenReturn(Optional.empty());

    var result = objectUnderTest.findById(DEFAULT_ID);

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

    when(idProviderMock.generateId()).thenReturn(DEFAULT_ID);
    when(timeFactoryMock.now()).thenReturn(INSTANT_NOW);
    when(repositoryMock.save(any(Collection.class))).thenAnswer(invocation -> invocation.getArgument(0));


    var result = objectUnderTest.create(CollectionInput.builder().name(name).build());

    assertNotNull(result);
    assertTrue(result.isPresent());
    
    var savedCollection = result.get();
    assertNotNull(savedCollection);
    assertEquals(DEFAULT_ID, savedCollection.getId());
    assertEquals(name, savedCollection.getName());
    assertEquals(INSTANT_NOW, savedCollection.getCreatedAt());
    assertEquals(INSTANT_NOW, savedCollection.getLastModifiedAt());
  }

  @Test
  void create_GenerateIdThrowsException_ReturnsEmpty() {
    when(idProviderMock.generateId()).thenThrow(new GenerateIdException());

    var result = objectUnderTest.create(CollectionInput.builder().name("abc").build());

    assertNotNull(result);
    assertTrue(result.isEmpty());    
  }

  @Test
  void create_SaveReturnsNull_ReturnsEmpty() {
    when(idProviderMock.generateId()).thenReturn(DEFAULT_ID);
    when(repositoryMock.save(any(Collection.class))).thenReturn(null);

    var result = objectUnderTest.create(CollectionInput.builder().name("abc").build());

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void create_GivenCollectionInputIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.create(null);
    });
    assertEquals("collectionInput is null", exception.getMessage());     
  }  

  @Test
  void update_Saved_ReturnsCollection() {
    var originalCollection = Collection.builder()
      .id(DEFAULT_ID)
      .createdAt(INSTANT_PAST)
      .lastModifiedAt(INSTANT_PAST)
      .name("Shiny stuff")
      .build();

    var modifiedName = "New words";

    when(repositoryMock.findById(eq(DEFAULT_ID))).thenReturn(Optional.of(originalCollection));
    when(timeFactoryMock.now()).thenReturn(INSTANT_NOW);
    when(repositoryMock.save(any(Collection.class))).thenAnswer(invocation -> invocation.getArgument(0));    

    var result = objectUnderTest.update(CollectionInput.builder().name(modifiedName).build(), DEFAULT_ID);

    assertNotNull(result);
    assertTrue(result.isPresent());
    
    var savedCollection = result.get();
    assertNotNull(savedCollection);    
    assertEquals(DEFAULT_ID, savedCollection.getId());
    assertEquals(modifiedName, savedCollection.getName());
    assertEquals(INSTANT_PAST, savedCollection.getCreatedAt());
    assertEquals(INSTANT_NOW, savedCollection.getLastModifiedAt());
  }

  @Test
  void update_NotFound_ReturnsEmpty() {
    when(repositoryMock.findById(any())).thenReturn(Optional.empty());

    var result = objectUnderTest.update(CollectionInput.builder().name("New words").build(), DEFAULT_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void update_SaveReturnsNull_ReturnsEmpty() {
    when(repositoryMock.findById(any())).thenReturn(Optional.of(Collection.builder().id(DEFAULT_ID).build()));    
    when(repositoryMock.save(any(Collection.class))).thenReturn(null);

    var result = objectUnderTest.update(CollectionInput.builder().name("New words").build(), DEFAULT_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }  

  @Test
  void update_GivenCollectionInputIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.update(null, DEFAULT_ID);
    });
    assertEquals("collectionInput is null", exception.getMessage());     
  }

  @Test
  void update_GivenIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.update(CollectionInput.builder().name("New words").build(), null);
    });
    assertEquals("id is null", exception.getMessage());     
  }

  @Test
  void delete_GivenIdIsUUID_MethodInvoked() {
    objectUnderTest.delete(DEFAULT_ID);
    verify(repositoryMock).deleteById(DEFAULT_ID);
  }

  @Test
  void delete_GivenIdIsNull_ThrowsException() {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      objectUnderTest.delete(null);
    });
    assertEquals("id is null", exception.getMessage());     
  }
}
