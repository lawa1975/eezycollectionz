package de.wagner1975.eezycollectionz.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.wagner1975.eezycollectionz.support.GenerateIdForRepositoryOperation;
import de.wagner1975.eezycollectionz.support.GenerateIdOperationFactory;

@ExtendWith(SpringExtension.class)
class EntryIdProviderTest {

  @Mock
  private GenerateIdOperationFactory mockFactory;

  @Mock
  private GenerateIdForRepositoryOperation<UUID> mockOperation;

  @InjectMocks
  private EntryIdProvider objectUnderTest;

  @Test
  void generateId_Success_ReturnsUUID() {
    var id = UUID.fromString("00000004-4444-0000-0000-000000000004");

    when(mockFactory.createForUUIDInRepository(any(), any())).thenReturn(mockOperation);
    when(mockOperation.execute()).thenReturn(id);

    var result = objectUnderTest.generateId();

    assertNotNull(result);
    assertEquals(id, result);
  }
}
