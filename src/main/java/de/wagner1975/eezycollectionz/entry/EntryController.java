package de.wagner1975.eezycollectionz.entry;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/entries")
@AllArgsConstructor
public class EntryController {

  private final EntryService service;

  @Operation(summary = "Get all entries of a single collection")
  @GetMapping("")
  public List<Entry> findByCollectionId(@RequestParam UUID collectionId) {
    return service.findByCollectionId(collectionId);    
  }

  @Operation(summary = "Get a single entry by its identifier")  
  @GetMapping("/{id}")
  public Entry findById(@PathVariable UUID id) {
    return service.findById(id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
  }

  @Operation(summary = "Add new entry to a collection")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/collection/{collectionId}")
  public Entry create(@Valid @RequestBody EntryInput entryInput, @PathVariable UUID collectionId) {
    return service.create(entryInput, collectionId).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Failed to generate non-existing ID")); 
  }

  @Operation(summary = "Modify an existing entry")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public Entry update(@Valid @RequestBody EntryInput entryInput, @PathVariable UUID id) {
    return service.update(entryInput, id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
  }

  @Operation(summary = "Delete an existing entry")  
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }  
}
