package de.wagner1975.eezycollectionz.collection;

import java.util.UUID;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import de.wagner1975.eezycollectionz.support.GenerateIdOperationFactory;
import de.wagner1975.eezycollectionz.support.RandomUUIDGenerator;

@Component
@AllArgsConstructor
class CollectionIdProvider {

  private final GenerateIdOperationFactory factory;

  private final RandomUUIDGenerator generator;

  private final CollectionRepository repository;

  UUID generateId() {
    return factory.createForUUIDInRepository(generator, repository).execute();
  };
}
