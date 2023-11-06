package de.wagner1975.eezycollectionz.entry;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

import lombok.AllArgsConstructor;

import de.wagner1975.eezycollectionz.collection.Collection;
import de.wagner1975.eezycollectionz.support.GenerateIdException;

@Service
@AllArgsConstructor
class EntryService {

  private final EntryRepository repository;

  private final EntryIdProvider provider;

  List<Entry> findByCollectionId(UUID collectionId) {
    Preconditions.checkArgument(Objects.nonNull(collectionId), "collectionId is null");   
    return repository.findByCollectionId(collectionId, PageRequest.of(0, 100)).getContent();   
  }

  Optional<Entry> findById(UUID id) {
    Preconditions.checkArgument(Objects.nonNull(id), "id is null");
    return repository.findById(id);
  }

  Optional<Entry> create(EntryInput entryInput, UUID collectionId) {
    Preconditions.checkArgument(Objects.nonNull(entryInput), "entryInput is null");    
    Preconditions.checkArgument(Objects.nonNull(collectionId), "collectionId is null");

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

      return Optional.ofNullable(repository.save(newEntry));         
    }
    catch (GenerateIdException ex) {
      return Optional.empty();
    }
  }

  Optional<Entry> update(EntryInput entryInput, UUID id) {
    Preconditions.checkArgument(Objects.nonNull(entryInput), "entryInput is null");
    Preconditions.checkArgument(Objects.nonNull(id), "id is null");

    var foundEntry = repository.findById(id);
    
    if (foundEntry.isEmpty()) {
      return Optional.empty();
    }

    var existingEntry = foundEntry.get();

    existingEntry.setLastModifiedAt(Instant.now());
    existingEntry.setName(entryInput.getName());

    return Optional.ofNullable(repository.save(existingEntry));
  }  

  void delete(UUID id) {
    Preconditions.checkArgument(Objects.nonNull(id), "id is null");
    repository.deleteById(id);
  } 
}
