package de.wagner1975.eezycollectionz.collection;

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
@RequestMapping("/api/collections")
@AllArgsConstructor
public class CollectionController {

  private final CollectionService service;
  
  @GetMapping("")
  public List<Collection> findAll() {
    return service.findAll();    
  }

  @GetMapping("/{id}")
  public Collection findById(@PathVariable UUID id) {
    return service.findById(id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("")
  public Collection create(@Valid @RequestBody CollectionInput collectionInput) {
    return service.create(collectionInput).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Failed to generate non-existing ID")); 
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public Collection update(@Valid @RequestBody CollectionInput collectionInput, @PathVariable UUID id) {
    return service.update(collectionInput, id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
