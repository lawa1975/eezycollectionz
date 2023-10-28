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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Tag(
  name = "Collections",
  description = "Part of the API that provides retrieval and management operations on collections.")
@RestController
@RequestMapping("/api/collections")
@AllArgsConstructor
public class CollectionController {

  private final CollectionService service;
  
  @Operation(
    summary = "Get all collections",
    description = "Returns all available collections.",
    tags = { "Collections" })
  @GetMapping("")
  public List<Collection> findAll() {
    return service.findAll();    
  }

  @Operation(
    summary = "Get a collection by its id",
    description = "Finds a single collection by its identifier (UUID).",
    tags = { "Collections" })
  @ApiResponses({
    @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = Collection.class), mediaType = "application/json") }),
    @ApiResponse(responseCode = "404", description = "A collection with the given id was not found.", content = { @Content(schema = @Schema()) })
  })  
  @GetMapping("/{id}")
  public Collection findById(@PathVariable UUID id) {
    return service.findById(id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
  }

  @Operation(
    summary = "Add a new collection",
    tags = { "Collections" })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("")
  public Collection create(@Valid @RequestBody CollectionInput collectionInput) {
    return service.create(collectionInput).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Failed to generate non-existing ID")); 
  }

  @Operation(
    summary = "Modify an existing collection",
    tags = { "Collections" }) 
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public Collection update(@Valid @RequestBody CollectionInput collectionInput, @PathVariable UUID id) {
    return service.update(collectionInput, id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
  }

  @Operation(
    summary = "Remove an existing collection",
    tags = { "Collections" })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
