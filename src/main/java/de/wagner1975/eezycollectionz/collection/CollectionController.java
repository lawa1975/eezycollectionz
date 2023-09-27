package de.wagner1975.eezycollectionz.collection;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/collection")
@AllArgsConstructor
public class CollectionController {

  private final CollectionRepository repository;
  
  @GetMapping("")
  public List<Collection> findAll() {
    return repository.findAll();    
  }

  @GetMapping("/{id}")
  public Collection findById(@PathVariable UUID id) {
    return repository.findById(id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("")
  public Collection create(@Valid @RequestBody CollectionInput collectionInput) {
    UUID generatedId = UUID.randomUUID();

    if (repository.existsById(generatedId)) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "ID already exists");     
    }

    Instant now = Instant.now();

    Collection newCollection = Collection.builder()
      .id(generatedId)
      .createdAt(now)
      .lastModifiedAt(now)
      .name(collectionInput.getName())
      .build();

    return repository.save(newCollection);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public Collection update(@Valid @RequestBody CollectionInput collectionInput, @PathVariable UUID id) {
    Collection existingCollection = repository.findById(id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));

    existingCollection.setLastModifiedAt(Instant.now());
    existingCollection.setName(collectionInput.getName());

    return repository.save(existingCollection);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    repository.deleteById(id);
  }
}
