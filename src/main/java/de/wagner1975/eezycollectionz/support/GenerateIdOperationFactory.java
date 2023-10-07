package de.wagner1975.eezycollectionz.support;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import de.wagner1975.eezycollectionz.ApplicationProperties;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class GenerateIdOperationFactory {

  private final ApplicationProperties appProps;

  public GenerateIdForRepositoryOperation<UUID> createForUUIDInRepository(IdGenerator<UUID> generator, CrudRepository<?, UUID> repository) {
    return new GenerateIdForRepositoryOperation<>(generator, repository, appProps.maxRetriesToGenerateId());
  }
}
