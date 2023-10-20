package de.wagner1975.eezycollectionz.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class EntryServiceTest {

  @Mock
  private EntryRepository mockRepository;
  
  @Mock
  private EntryIdProvider mockIdProvider;

  @InjectMocks
  private EntryService objectUnderTest;
  
  @Test
  void findById_IsFound_ReturnsCollection() {
    var id = UUID.fromString("c725efeb-de77-46df-916a-2fc195376386");

    when(mockRepository.findById(eq(id))).thenReturn(Optional.of(Entry.builder().id(id).build()));

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
}
