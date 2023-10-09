package de.wagner1975.eezycollectionz.entry;

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

import de.wagner1975.eezycollectionz.collection.CollectionInput;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(EntryController.class)
@ActiveProfiles("test")
class EntryControllerTest {

  private static final String DEFAULT_COLLECTION_ID = "992e4141-add3-49ba-875b-d92da4ea9a18";
  private static final String DEFAULT_ENTRY_ID = "c725efeb-de77-46df-916a-2fc195376386";
  
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private EntryService mockService;

  @Test
  void post_Success_IsCreated() throws Exception {
      var entryInput = EntryInput.builder().name("uvw").build(); 
    
      when(mockService.create(any(), eq(UUID.fromString(DEFAULT_COLLECTION_ID))))
        .thenReturn(Optional.of(Entry.builder()
          .id(UUID.fromString(DEFAULT_ENTRY_ID))
          .name(entryInput.getName())
          .build()));

      mockMvc
        .perform(post("/api/entries/collection/" + DEFAULT_COLLECTION_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(entryInput)))
        .andExpect(MockMvcResultMatchers.status().isCreated());
  }

  @Test
  void post_FailureOnIdGeneration_UnprocessableEntity() throws Exception {
      when(mockService.create(any(), any())).thenReturn(Optional.empty());

      mockMvc
        .perform(post("/api/entries/collection/" + DEFAULT_COLLECTION_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(CollectionInput.builder().name("uvw").build())))
        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
  }

  @Test
  void post_PayloadInvalid_BadRequest() throws Exception {
      mockMvc
        .perform(post("/api/entries/collection/" + DEFAULT_COLLECTION_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(CollectionInput.builder().name("").build())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }

  @Test
  void post_CollectionIdInvalid_BadRequest() throws Exception {
      mockMvc
        .perform(post("/api/entries/collection/*")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(CollectionInput.builder().name("uvw").build())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }    

  @Test
  void delete_Success_NoContent() throws Exception {
      mockMvc
        .perform(delete("/api/entries/" + DEFAULT_ENTRY_ID))
        .andExpect(MockMvcResultMatchers.status().isNoContent());
  }

  @Test
  void delete_IdInvalid_BadRequest() throws Exception {
      mockMvc
        .perform(delete("/api/entries/3tt3"))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }
}
