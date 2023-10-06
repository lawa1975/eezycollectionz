package de.wagner1975.eezycollectionz.collection;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(CollectionController.class)
@ActiveProfiles("test")
class CollectionControllerTest {
  
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private CollectionService mockService;

  @Test
  void post_Success_IsCreated() throws Exception {
      var collectionInput = CollectionInput.builder().name("xyz").build(); 
    
      when(mockService.create(any()))
        .thenReturn(Optional.of(Collection.builder()
          .id(UUID.fromString("c725efeb-de77-46df-916a-2fc195376386"))
          .name(collectionInput.getName())
          .build()));

      mockMvc
        .perform(post("/api/collections")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(collectionInput)))
        .andExpect(MockMvcResultMatchers.status().isCreated());
  }

  @Test
  void post_FailureOnIdGeneration_UnprocessableEntity() throws Exception {
      when(mockService.create(any())).thenReturn(Optional.empty());

      mockMvc
        .perform(post("/api/collections")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(CollectionInput.builder().name("xyz").build())))
        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
  }

  @Test
  void post_PayloadInvalid_BadRequest() throws Exception {
      mockMvc
        .perform(post("/api/collections")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(CollectionInput.builder().name("").build())))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }

  @Test
  void delete_Success_NoContent() throws Exception {
      mockMvc
        .perform(delete("/api/collections/c725efeb-de77-46df-916a-2fc195376386"))
        .andExpect(MockMvcResultMatchers.status().isNoContent());
  }

  @Test
  void delete_IdInvalid_BadRequest() throws Exception {
      mockMvc
        .perform(delete("/api/collections/1gg1"))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());    
  }
}
