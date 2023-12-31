package de.wagner1975.eezycollectionz.entry;

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
import org.springframework.web.bind.annotation.RequestParam;
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
  name = "Entries",
  description = "This part of the API provides retrieval and management operations on entries of any collection")  
@RestController
@RequestMapping("/api/entries")
@AllArgsConstructor
public class EntryController {

  private final EntryService service;

  @Operation(
    summary = "Get entries of a collection using pagination",
    description = "Finds page of entries which are contained in a single collection")
  @Parameter(
    name = "collectionId",
    description = "Identifies the collection, in which to find the entries",
    required = true)    
  @ApiResponse(
    responseCode = "200",
    description ="Page with collections and additional information",
    useReturnTypeSchema = true)
  @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public Page<Entry> findByCollectionId(
    @RequestParam UUID collectionId,
    @ParameterObject
    @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Direction.ASC)
    Pageable pageable) {
    return service.findByCollectionId(collectionId, pageable);
  }

  @Operation(
    summary = "Get a single entry",
    description = "Finds an existing entry by its identifier (UUID)") 
  @Parameter(
    name = "id",
    description = "Identifies the entry to find",
    required = true)
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description ="Found entry is returned",
      content = {
        @Content(
          schema = @Schema(implementation = Entry.class),
          mediaType = "application/json") }),
    @ApiResponse(
      responseCode = "404",
      description = "No entry for given id was found",
      content = { @Content(schema = @Schema()) })})
  @GetMapping("/{id}")
  public Entry findById(@PathVariable UUID id) {
    return service.findById(id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
  }

  @Operation(
    summary = "Create an entry",
    description = "Creates a new entry and adds it to a single collection",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Entry data",
      content = @Content(schema = @Schema(implementation = EntryInput.class),
      mediaType = "application/json"),
      required = true))
  @Parameter(
    name = "collectionId",
    description = "Identifies the collection, to which the new entry will be added",
    required = true)    
  @ApiResponses({
    @ApiResponse(
      responseCode = "201",
      description = "New entry is returned",
      content = { @Content(schema = @Schema(implementation = Entry.class),
      mediaType = "application/json") }),
    @ApiResponse(
      responseCode = "422",
      description = "Auto-generation of unique entry id failed",
      content = { @Content(schema = @Schema()) })})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/collection/{collectionId}")
  public Entry create(@Valid @RequestBody EntryInput entryInput, @PathVariable UUID collectionId) {
    return service.create(entryInput, collectionId).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Auto-generation of unique entry id failed")); 
  }

  @Operation(
    summary = "Update an entry",
    description = "Gets an existing entry by its identifier (UUID) and updates the data",    
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Entry data",
      content = @Content(schema = @Schema(implementation = EntryInput.class),
      mediaType = "application/json"),
      required = true))
  @Parameter(
    name = "id",
    description = "Identifies the entry to be updated",
    required = true)
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description ="Updated entry is returned",
      content = {
        @Content(
          schema = @Schema(implementation = Entry.class),
          mediaType = "application/json") }),
    @ApiResponse(
      responseCode = "404",
      description = "No entry for given id was found",
      content = { @Content(schema = @Schema()) })})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public Entry update(@Valid @RequestBody EntryInput entryInput, @PathVariable UUID id) {
    return service.update(entryInput, id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
  }

  @Operation(
    summary = "Delete an entry",
    description = "Deletes an existing entry by its identifier (UUID)")
  @Parameter(
    name = "id",
    description = "Identifies the entry to be deleted",
    required = true)
  @ApiResponse(
    responseCode = "204",
    description ="Entry is deleted")      
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }  
}
