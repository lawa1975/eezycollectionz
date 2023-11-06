package de.wagner1975.eezycollectionz.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
class GenerateIdForRepositoryOperationTest {

  @Mock
  private IdGenerator<String> generatorMock;

  @Mock
  private CrudRepository<Object, String> repositoryMock;
  
  @Test
  void execute_NoRetryNeeded_ReturnsGeneratedId() {
    when(generatorMock.generate()).thenReturn("ab777cd");
    when(repositoryMock.existsById(any())).thenReturn(false);

    var generatedId = new GenerateIdForRepositoryOperation<>(generatorMock, repositoryMock, 0).execute();

    assertEquals("ab777cd", generatedId);
  }

  @Test
  void execute_SuccessOnRetry_ReturnsGeneratedId() {
    when(generatorMock.generate()).thenReturn("xx456pp");
    when(repositoryMock.existsById(any())).thenReturn(true, true, false);
    
    var generatedId = new GenerateIdForRepositoryOperation<>(generatorMock, repositoryMock, 2).execute();

    assertEquals("xx456pp", generatedId);
  }

  @Test
  void execute_ExceedsMaxRetries_ThrowsGeneratedIdException() {
    when(generatorMock.generate()).thenReturn("fg222nm");
    when(repositoryMock.existsById(any())).thenReturn(true, true, false);
    
    assertThrows(GenerateIdException.class, () -> {
      new GenerateIdForRepositoryOperation<>(generatorMock, repositoryMock, 1).execute();
    });
  }

  @Test  
  void execute_GeneratedIdIsAlwaysNull_ThrowsGeneratedIdException() {
    when(generatorMock.generate()).thenReturn(null);
    when(repositoryMock.existsById(any())).thenReturn(false);
    
    assertThrows(GenerateIdException.class, () -> {
      new GenerateIdForRepositoryOperation<>(generatorMock, repositoryMock, 1).execute();
    });
  }  
}
