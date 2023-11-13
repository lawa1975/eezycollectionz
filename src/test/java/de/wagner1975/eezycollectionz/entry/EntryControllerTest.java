package de.wagner1975.eezycollectionz.entry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(EntryController.class)
@ActiveProfiles("test")
class EntryControllerTest {

  private static final String REQUEST_PATH = "/api/entries";
  private static final String REQUEST_WITH_COLLECTION_PATH = REQUEST_PATH + "/collection";

  private static final String DEFAULT_COLLECTION_ID = "992e4141-add3-49ba-875b-d92da4ea9a18";
  private static final String INVALID_COLLECTION_ID = "_";

  private static final String DEFAULT_ENTRY_ID = "c725efeb-de77-46df-916a-2fc195376386";
  private static final String INVALID_ENTRY_ID = "_";

  private static final String DEFAULT_NAME = "uvw";
  private static final String INVALID_NAME = "";
  private static final String MODIFIED_NAME = "other-name";

  private static final String CREATED_AT = "2023-10-05T10:44:09.295558200Z";
  private static final String LAST_MODIFIED_AT = "2023-10-06T12:51:55.321115900Z";  
  
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private EntryService serviceMock;

  @Mock
  private PageImpl<Entry> pageMock;

  @Captor
  private ArgumentCaptor<Pageable> pageableCaptor;  

  @Test
  void getByCollectionId_Success_Ok() throws Exception {
    var id1 = "00000001-1111-aaaa-aaaa-000000000001";
    var id2 = "00000002-2222-aaaa-aaaa-000000000002";

    when(pageMock.getContent())
      .thenReturn(List.of(
        Entry.builder().id(UUID.fromString(id2)).build(),
        Entry.builder().id(UUID.fromString(id1)).build())); 
        
    when(serviceMock.findByCollectionId(eq(UUID.fromString(DEFAULT_COLLECTION_ID)), pageableCaptor.capture()))
      .thenReturn(pageMock);

    mockMvc
      .perform(get(REQUEST_PATH + "?collectionId=" + DEFAULT_COLLECTION_ID + "&page=5&size=2&sort=id,desc"))
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(id2))
      .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").value(id1));

	  var allValues = pageableCaptor.getAllValues();
    assertEquals(1, allValues.size());
    var capturedPageable = allValues.get(0);
    assertNotNull(capturedPageable);
    assertEquals(5, capturedPageable.getPageNumber());
    assertEquals(2, capturedPageable.getPageSize());
    var sort = capturedPageable.getSort();
    assertNotNull(sort);
    var order = sort.toList().get(0);
    assertEquals("id", order.getProperty());
    assertTrue(order.isDescending());
  }  

  @Test
  void getByCollectionId_InvalidCollectionId_BadRequest() throws Exception {
      mockMvc
        .perform(get(REQUEST_PATH + "?collectionId=" + INVALID_COLLECTION_ID))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
  }  

  @Test
  void getById_Success_Ok() throws Exception {
      when(serviceMock.findById(eq(UUID.fromString(DEFAULT_ENTRY_ID))))
        .thenReturn(Optional.of(Entry.builder()
          .id(UUID.fromString(DEFAULT_ENTRY_ID))
          .createdAt(Instant.parse(CREATED_AT))
          .lastModifiedAt(Instant.parse(LAST_MODIFIED_AT))          
          .name(DEFAULT_NAME)
          .build()));

      mockMvc
        .perform(get(REQUEST_PATH + "/" + DEFAULT_ENTRY_ID))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(DEFAULT_ENTRY_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(CREATED_AT))
        .andExpect(MockMvcResultMatchers.jsonPath("$.lastModifiedAt").value(LAST_MODIFIED_AT))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(DEFAULT_NAME));      
  }

  @Test
  void getById_InvalidId_BadRequest() throws Exception {
      mockMvc
        .perform(get(REQUEST_PATH + "/" + INVALID_ENTRY_ID))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
  }

  @Test
  void getById_NoEntryReturned_NotFound() throws Exception {
      when(serviceMock.findById(any())).thenReturn(Optional.empty());

      mockMvc
        .perform(get(REQUEST_PATH + "/" + DEFAULT_ENTRY_ID))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }  

  @Test
  void post_Success_Created() throws Exception {
      var entryInput = EntryInput.builder().name(DEFAULT_NAME).build(); 
    
      when(serviceMock.create(any(), eq(UUID.fromString(DEFAULT_COLLECTION_ID))))
        .thenReturn(Optional.of(Entry.builder()
          .id(UUID.fromString(DEFAULT_ENTRY_ID))
          .createdAt(Instant.parse(CREATED_AT))
          .lastModifiedAt(Instant.parse(CREATED_AT))            
          .name(entryInput.getName())
          .build()));

      mockMvc
        .perform(post(REQUEST_WITH_COLLECTION_PATH + "/" + DEFAULT_COLLECTION_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(entryInput)))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(DEFAULT_ENTRY_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(CREATED_AT))
        .andExpect(MockMvcResultMatchers.jsonPath("$.lastModifiedAt").value(CREATED_AT))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(DEFAULT_NAME));          
  }

  @Test
  void post_FailureOnIdGeneration_UnprocessableEntity() throws Exception {
      when(serviceMock.create(any(), any())).thenReturn(Optional.empty());

      mockMvc
        .perform(post(REQUEST_WITH_COLLECTION_PATH + "/" + DEFAULT_COLLECTION_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(EntryInput.builder().name(DEFAULT_NAME).build())))
        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
  }

  @Test
  void post_InvalidPayload_BadRequest() throws Exception {
      mockMvc
        .perform(post(REQUEST_WITH_COLLECTION_PATH + "/" + DEFAULT_COLLECTION_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(EntryInput.builder().name(INVALID_NAME).build())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }

  @Test
  void post_InvalidCollectionId_BadRequest() throws Exception {
      mockMvc
        .perform(post(REQUEST_WITH_COLLECTION_PATH + "/" + INVALID_COLLECTION_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(EntryInput.builder().name(DEFAULT_NAME).build())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }    

  @Test
  void put_Success_Ok() throws Exception {
      var entryInput = EntryInput.builder().name(MODIFIED_NAME).build();

      when(serviceMock.update(any(), eq(UUID.fromString(DEFAULT_ENTRY_ID))))
        .thenReturn(Optional.of(Entry.builder()
          .id(UUID.fromString(DEFAULT_ENTRY_ID))
          .createdAt(Instant.parse(CREATED_AT))
          .lastModifiedAt(Instant.parse(LAST_MODIFIED_AT))           
          .name(MODIFIED_NAME)
          .build()));

      mockMvc
        .perform(put(REQUEST_PATH + "/" + DEFAULT_ENTRY_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(entryInput)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(DEFAULT_ENTRY_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(CREATED_AT))
        .andExpect(MockMvcResultMatchers.jsonPath("$.lastModifiedAt").value(LAST_MODIFIED_AT))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(MODIFIED_NAME));   
  }

  @Test
  void put_InvalidId_BadRequest() throws Exception {
      mockMvc
        .perform(put(REQUEST_PATH + "/" + INVALID_ENTRY_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(EntryInput.builder().name(MODIFIED_NAME).build())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }

  @Test
  void put_InvalidPayload_BadRequest() throws Exception {
      mockMvc
        .perform(put(REQUEST_PATH + "/" + DEFAULT_ENTRY_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(EntryInput.builder().name(INVALID_NAME).build())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }

  @Test
  void put_FailureOnUpdate_NotFound() throws Exception {
      when(serviceMock.update(any(), any())).thenReturn(Optional.empty());

      mockMvc
        .perform(put(REQUEST_PATH + "/" + DEFAULT_ENTRY_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(EntryInput.builder().name(MODIFIED_NAME).build())))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }  

  @Test
  void delete_Success_NoContent() throws Exception {
      mockMvc
        .perform(delete(REQUEST_PATH + "/" + DEFAULT_ENTRY_ID))
        .andExpect(MockMvcResultMatchers.status().isNoContent());
  }

  @Test
  void delete_InvalidId_BadRequest() throws Exception {
      mockMvc
        .perform(delete(REQUEST_PATH + "/" + INVALID_ENTRY_ID))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }
}
