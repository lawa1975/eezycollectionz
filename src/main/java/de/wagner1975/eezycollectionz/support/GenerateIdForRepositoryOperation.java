package de.wagner1975.eezycollectionz.support;

import java.util.Objects;
import org.springframework.data.repository.CrudRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GenerateIdForRepositoryOperation<ID> {

  private final IdGenerator<ID> generator;
  
  private final CrudRepository<?, ID> repository;
  
  private final int maxRetries;

  public ID execute() {
    ID generatedId = null;
    var i = 0;
    do {
      generatedId = generator.generate();
      i++;
    }
    while (repository.existsById(generatedId) && i <= maxRetries);

    if (i > maxRetries + 1 || Objects.isNull(generatedId)) {
      throw new GenerateIdException();
    }

    return generatedId;
  }
}
