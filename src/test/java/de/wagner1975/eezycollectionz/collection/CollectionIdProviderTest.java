package de.wagner1975.eezycollectionz.collection;

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
class CollectionIdProviderTest {

  @Mock
  private GenerateIdOperationFactory factoryMock;

  @Mock
  private GenerateIdForRepositoryOperation<UUID> operationMock;

  @InjectMocks
  private CollectionIdProvider objectUnderTest;

  @Test
  void generateId_Success_ReturnsUUID() {
    var id = UUID.fromString("00000003-3333-0000-0000-000000000003");

    when(factoryMock.createForUUIDInRepository(any(), any())).thenReturn(operationMock);
    when(operationMock.execute()).thenReturn(id);

    var result = objectUnderTest.generateId();

    assertNotNull(result);
    assertEquals(id, result);
  }
}
