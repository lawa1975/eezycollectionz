package de.wagner1975.eezycollectionz.entry;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.wagner1975.eezycollectionz.ApplicationProperties;
import de.wagner1975.eezycollectionz.collection.Collection;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/entries")
@AllArgsConstructor
public class EntryController {

  private final EntryRepository repository;

  private final ApplicationProperties appProps;  

  @GetMapping("")
  public List<Entry> findByCollectionId(@RequestParam UUID collectionId) {
    return repository.findByCollectionId(collectionId);    
  }

  @GetMapping("/{id}")
  public Entry findById(@PathVariable UUID id) {
    return repository.findById(id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/collection/{collectionId}")
  public Entry create(@Valid @RequestBody EntryInput entryInput, @PathVariable UUID collectionId) {
    UUID generatedId = null;
    var maxRetries = appProps.maxRetriesToGenerateId();
    var i = 0;
    do {
      generatedId = UUID.randomUUID();      
      i++;
    }
    while (repository.existsById(generatedId) && i <= maxRetries);

    if (i > maxRetries || Objects.isNull(generatedId)) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Failed to generate non-existing ID");     
    }    

    var now = Instant.now();

    var newEntry = Entry.builder()
      .id(generatedId)
      .createdAt(now)
      .lastModifiedAt(now)
      .name(entryInput.getName())
      .collection(Collection.builder().id(collectionId).build())
      .build();

    return repository.save(newEntry);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public Entry update(@Valid @RequestBody EntryInput entryInput, @PathVariable UUID id) {
    var existingEntry = repository.findById(id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

    existingEntry.setLastModifiedAt(Instant.now());
    existingEntry.setName(entryInput.getName());

    return repository.save(existingEntry);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    repository.deleteById(id);
  }  
}
