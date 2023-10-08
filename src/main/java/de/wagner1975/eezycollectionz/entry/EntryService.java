package de.wagner1975.eezycollectionz.entry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

import de.wagner1975.eezycollectionz.collection.Collection;
import de.wagner1975.eezycollectionz.support.GenerateIdException;

@Service
@AllArgsConstructor
class EntryService {

  private final EntryRepository repository;

  private final EntryIdProvider provider;

  List<Entry> findByCollectionId(UUID collectionId) {
    return repository.findByCollectionId(collectionId);   
  }

  Optional<Entry> findById(UUID id) {
    return repository.findById(id);
  }

  Optional<Entry> create(EntryInput entryInput, UUID collectionId) {
    try {
      var generatedId = provider.generateId();

      var now = Instant.now();

    var newEntry = Entry.builder()
      .id(generatedId)
      .createdAt(now)
      .lastModifiedAt(now)
      .name(entryInput.getName())
      .collection(Collection.builder().id(collectionId).build())
      .build();

      return Optional.of(repository.save(newEntry));         
    }
    catch (GenerateIdException ex) {
      return Optional.empty();
    }
  }

  Optional<Entry> update(EntryInput entryInput, UUID id) {
    var foundEntry = repository.findById(id);
    
    if (foundEntry.isEmpty()) {
      return Optional.empty();
    }

    var existingEntry = foundEntry.get();

    existingEntry.setLastModifiedAt(Instant.now());
    existingEntry.setName(entryInput.getName());

    return Optional.of(repository.save(existingEntry));
  }  

  void delete(UUID id) {
    repository.deleteById(id);
  } 
}
