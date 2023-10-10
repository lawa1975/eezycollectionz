package de.wagner1975.eezycollectionz.collection;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(CollectionController.class)
@ActiveProfiles("test")
class CollectionControllerTest {

  private static final String REQUEST_PATH = "/api/collections";

  private static final String DEFAULT_COLLECTION_ID = "2a7a1f1c-f4fe-4b4e-9e11-79a0e353eae0";
  private static final String INVALID_COLLECTION_ID = "_";

  private static final String DEFAULT_NAME = "xyz";
  private static final String INVALID_NAME = "";
  private static final String MODIFIED_NAME = "other-name";

  private static final String CREATED_AT = "2023-10-05T10:44:09.295558200Z";
  private static final String LAST_MODIFIED_AT = "2023-10-06T12:51:55.321115900Z";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private CollectionService mockService;

  @Test
  void get_Success_Ok() throws Exception {
      when(mockService.findAll()).thenReturn(Collections.emptyList());

      mockMvc
        .perform(get(REQUEST_PATH))
        .andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  void getById_Success_Ok() throws Exception {
      when(mockService.findById(eq(UUID.fromString(DEFAULT_COLLECTION_ID))))
        .thenReturn(Optional.of(Collection.builder()
          .id(UUID.fromString(DEFAULT_COLLECTION_ID))
          .createdAt(Instant.parse(CREATED_AT))
          .lastModifiedAt(Instant.parse(LAST_MODIFIED_AT))
          .name(DEFAULT_NAME)
          .build()));

      mockMvc
        .perform(get(REQUEST_PATH + "/" + DEFAULT_COLLECTION_ID))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(DEFAULT_COLLECTION_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(CREATED_AT))
        .andExpect(MockMvcResultMatchers.jsonPath("$.lastModifiedAt").value(LAST_MODIFIED_AT))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(DEFAULT_NAME));
  }

  @Test
  void getById_InvalidId_BadRequest() throws Exception {
      mockMvc
        .perform(get(REQUEST_PATH + "/" + INVALID_COLLECTION_ID))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
  }

  @Test
  void getById_NoCollectionReturned_NotFound() throws Exception {
      when(mockService.findById(any())).thenReturn(Optional.empty());

      mockMvc
        .perform(get(REQUEST_PATH + "/" + DEFAULT_COLLECTION_ID))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  } 

  @Test
  void post_Success_Created() throws Exception {
      var collectionInput = CollectionInput.builder().name(DEFAULT_NAME).build(); 
    
      when(mockService.create(any()))
        .thenReturn(Optional.of(Collection.builder()
          .id(UUID.fromString(DEFAULT_COLLECTION_ID))
          .createdAt(Instant.parse(CREATED_AT))
          .lastModifiedAt(Instant.parse(CREATED_AT))          
          .name(collectionInput.getName())
          .build()));

      mockMvc
        .perform(post(REQUEST_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(collectionInput)))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(DEFAULT_COLLECTION_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(CREATED_AT))
        .andExpect(MockMvcResultMatchers.jsonPath("$.lastModifiedAt").value(CREATED_AT))        
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(DEFAULT_NAME));
  }

  @Test
  void post_FailureOnIdGeneration_UnprocessableEntity() throws Exception {
      when(mockService.create(any())).thenReturn(Optional.empty());

      mockMvc
        .perform(post(REQUEST_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(CollectionInput.builder().name(DEFAULT_NAME).build())))
        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
  }

  @Test
  void post_InvalidPayload_BadRequest() throws Exception {
      mockMvc
        .perform(post(REQUEST_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(CollectionInput.builder().name(INVALID_NAME).build())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }

  @Test
  void put_Success_Ok() throws Exception {
      var collectionInput = CollectionInput.builder().name(MODIFIED_NAME).build();

      when(mockService.update(any(), eq(UUID.fromString(DEFAULT_COLLECTION_ID))))
        .thenReturn(Optional.of(Collection.builder()
          .id(UUID.fromString(DEFAULT_COLLECTION_ID))
          .createdAt(Instant.parse(CREATED_AT))
          .lastModifiedAt(Instant.parse(LAST_MODIFIED_AT))          
          .name(MODIFIED_NAME)
          .build()));

      mockMvc
        .perform(put(REQUEST_PATH + "/" + DEFAULT_COLLECTION_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(collectionInput)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(DEFAULT_COLLECTION_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(CREATED_AT))
        .andExpect(MockMvcResultMatchers.jsonPath("$.lastModifiedAt").value(LAST_MODIFIED_AT))        
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(MODIFIED_NAME)); 
  }

  @Test
  void put_InvalidId_BadRequest() throws Exception {
      mockMvc
        .perform(put(REQUEST_PATH + "/" + INVALID_COLLECTION_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(CollectionInput.builder().name(MODIFIED_NAME).build())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }  

  @Test
  void put_InvalidPayload_BadRequest() throws Exception {
      mockMvc
        .perform(put(REQUEST_PATH + "/" + DEFAULT_COLLECTION_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(CollectionInput.builder().name(INVALID_NAME).build())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }

  @Test
  void put_FailureOnUpdate_NotFound() throws Exception {
      when(mockService.update(any(), any())).thenReturn(Optional.empty());

      mockMvc
        .perform(put(REQUEST_PATH + "/" + DEFAULT_COLLECTION_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(CollectionInput.builder().name(MODIFIED_NAME).build())))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }   

  @Test
  void delete_Success_NoContent() throws Exception {
      mockMvc
        .perform(delete(REQUEST_PATH + "/" + DEFAULT_COLLECTION_ID))
        .andExpect(MockMvcResultMatchers.status().isNoContent());
  }

  @Test
  void delete_InvalidId_BadRequest() throws Exception {
      mockMvc
        .perform(delete(REQUEST_PATH + "/" + INVALID_COLLECTION_ID))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }
}
