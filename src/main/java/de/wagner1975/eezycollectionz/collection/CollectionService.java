package de.wagner1975.eezycollectionz.collection;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import de.wagner1975.eezycollectionz.ApplicationProperties;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
class CollectionService {
  
  private final CollectionRepository repository;

  private final ApplicationProperties appProps;

  List<Collection> findAll() {
    return repository.findAll();      
  }

  Optional<Collection> findById(UUID id) {
    return repository.findById(id);
  }

  Optional<Collection> create(CollectionInput collectionInput) {
    UUID generatedId = null;
    var maxRetries = appProps.maxRetriesToGenerateId();
    var i = 0;
    do {
      generatedId = UUID.randomUUID();      
      i++;
    }
    while (repository.existsById(generatedId) && i <= maxRetries);

    if (i > maxRetries || Objects.isNull(generatedId)) {
      return Optional.empty();    
    }
    
    var now = Instant.now();

    var newCollection = Collection.builder()
      .id(generatedId)
      .createdAt(now)
      .lastModifiedAt(now)
      .name(collectionInput.getName())
      .build();

    return Optional.of(repository.save(newCollection));    
  }

  Optional<Collection> update(CollectionInput collectionInput, UUID id) {
    var foundCollection = repository.findById(id);
    
    if (foundCollection.isEmpty()) {
      return foundCollection;
    }

    var existingCollection = foundCollection.get();

    existingCollection.setLastModifiedAt(Instant.now());
    existingCollection.setName(collectionInput.getName());

    return Optional.of(repository.save(existingCollection));
  }

  void delete(UUID id) {
    repository.deleteById(id);
  }  
}
