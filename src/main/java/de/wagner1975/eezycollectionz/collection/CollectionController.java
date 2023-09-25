package de.wagner1975.eezycollectionz.collection;

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
    return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("")
  public Collection create(@Valid @RequestBody Collection collection) {
    if (Objects.isNull(collection.getId())) {
      collection.setId(UUID.randomUUID());
    }
    if (repository.existsById(collection.getId())) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "ID already exists");     
    }
    return repository.save(collection);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}")
  public void update(@Valid @RequestBody Collection collection, @PathVariable UUID id) {
    if (Objects.isNull(collection.getId())) {
      collection.setId(id);
    }
    if (!id.equals(collection.getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID cannot be updated");
    }
    if (!repository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found");
    }
    repository.save(collection);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    repository.deleteById(id);
  }
}
