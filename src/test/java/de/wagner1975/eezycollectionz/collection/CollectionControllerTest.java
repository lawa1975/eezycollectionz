package de.wagner1975.eezycollectionz.collection;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(CollectionController.class)
@ActiveProfiles("test")
class CollectionControllerTest {
  
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private CollectionRepository mockRepository;

  @Test
  void shouldCreateWhenPayloadValid() throws Exception {
      var collectionInput = CollectionInput.builder().name("xyz").build(); 
    
      when(mockRepository.existsById(any())).thenReturn(false);
      
      when(mockRepository.save(any()))
        .thenReturn(Collection.builder()
          .id(UUID.fromString("c725efeb-de77-46df-916a-2fc195376386"))
          .name(collectionInput.getName())
          .build());

      mockMvc
        .perform(post("/api/collections")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(CollectionInput.builder().name("xyz").build())))
        .andExpect(MockMvcResultMatchers.status().isCreated());
  }
}
