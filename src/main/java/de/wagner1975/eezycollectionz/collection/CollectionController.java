package de.wagner1975.eezycollectionz.collection;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Tag(
  name = "Collections",
  description = "This part of the API provides retrieval and management operations on collections")
@RestController
@RequestMapping("/api/collections")
@AllArgsConstructor
public class CollectionController {

  private final CollectionService service;
  
  @Operation(
    summary = "Get collections using pagination")
  @ApiResponse(
    responseCode = "200",
    description ="Page with collections and additional information",
    useReturnTypeSchema = true)
  @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public Page<Collection> findAll(
    @ParameterObject
    @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Direction.ASC)
    Pageable pageable) {
    return service.findAll(pageable);
  }

  @Operation(
    summary = "Get a single collection",
    description = "Finds an existing collection by its identifier (UUID)")
  @Parameter(
    name = "id",
    description = "Identifies the collection to find",
    required = true)    
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Found collection is returned",
      useReturnTypeSchema = true),
    @ApiResponse(
      responseCode = "404",
      description = "No collection with the given id was found.",
      content = { @Content(schema = @Schema()) })})  
  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection findById(@PathVariable UUID id) {
    return service.findById(id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
  }

  @Operation(
    summary = "Create a collection",
    description = "Creates a new collection and stores it in repository",    
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Collection data",
      content = @Content(
        schema = @Schema(implementation = CollectionInput.class),
        mediaType = MediaType.APPLICATION_JSON_VALUE),
      required = true))
  @ApiResponses({
    @ApiResponse(
      responseCode = "201",
      description = "New collection is returned",
      useReturnTypeSchema = true),
    @ApiResponse(
      responseCode = "422",
      description = "Auto-generation of unique collection id failed",
      content = { @Content(schema = @Schema()) })})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection create(@Valid @RequestBody CollectionInput collectionInput) {
    return service.create(collectionInput).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Auto-generation of unique collection id failed")); 
  }

  @Operation(
    summary = "Update a collection",
    description = "Gets an existing collection by its identifier (UUID) and updates the data",    
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Collection data",
      content = @Content(schema = @Schema(implementation = CollectionInput.class),
      mediaType = MediaType.APPLICATION_JSON_VALUE),
      required = true))
  @Parameter(
    name = "id",
    description = "Identifies the collection to be updated",
    required = true)
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description ="Updated collection is returned",
      useReturnTypeSchema = true),
    @ApiResponse(
      responseCode = "404",
      description = "No collection for given id was found",
      content = { @Content(schema = @Schema()) })})    
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection update(@Valid @RequestBody CollectionInput collectionInput, @PathVariable UUID id) {
    return service.update(collectionInput, id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
  }

  @Operation(
    summary = "Delete a collection",
    description = "Deletes an existing collection by its identifier (UUID)")
  @Parameter(
    name = "id",
    description = "Identifies the collection to be deleted",
    required = true)
  @ApiResponse(
    responseCode = "204",
    description ="Collection is deleted")     
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
