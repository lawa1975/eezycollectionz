package de.wagner1975.eezycollectionz.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
class GenerateIdForRepositoryOperationTest {

  @Mock
  private IdGenerator<String> mockGenerator;

  @Mock
  private CrudRepository<Object, String> mockRepository;
  
  @Test
  void execute() {
    when(mockGenerator.generate()).thenReturn("ab777cd");
    when(mockRepository.existsById(any())).thenReturn(false);

    var generatedId = new GenerateIdForRepositoryOperation<>(mockGenerator, mockRepository, 0).execute();

    assertEquals("ab777cd", generatedId);
  }
}
