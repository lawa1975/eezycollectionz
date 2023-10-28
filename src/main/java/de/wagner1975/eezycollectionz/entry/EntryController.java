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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Tag(
  name = "Entries",
  description = "Part of the API that provides retrieval and management operations on entries of any collection.")  
@RestController
@RequestMapping("/api/entries")
@AllArgsConstructor
public class EntryController {

  private final EntryService service;

  @Operation(
    summary = "Get all entries of a single collection",
    tags = { "Entries" })
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      content = {
        @Content(
          array = @ArraySchema(schema = @Schema(implementation = Entry.class)),
          mediaType = "application/json") })})
  @GetMapping("")
  public List<Entry> findByCollectionId(@RequestParam UUID collectionId) {
    return service.findByCollectionId(collectionId);    
  }

  @Operation(
    summary = "Get a single entry by its id",
    tags = { "Entries" })
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      content = {
        @Content(
          schema = @Schema(implementation = Entry.class),
          mediaType = "application/json") }),
    @ApiResponse(
      responseCode = "404",
      description = "An entry with the given id was not found.",
      content = { @Content(schema = @Schema()) })})
  @GetMapping("/{id}")
  public Entry findById(@PathVariable UUID id) {
    return service.findById(id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
  }

  @Operation(
    summary = "Add new entry to a collection",
    tags = { "Entries" })
  @ApiResponses({
    @ApiResponse(
      responseCode = "201",
      description = "Returns successfully created entry",
      content = { @Content(schema = @Schema(implementation = Entry.class),
      mediaType = "application/json") }),
    @ApiResponse(
      responseCode = "422",
      description = "Auto-generation of unique entry id failed.",
      content = { @Content(schema = @Schema()) })})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/collection/{collectionId}")
  public Entry create(@Valid @RequestBody EntryInput entryInput, @PathVariable UUID collectionId) {
    return service.create(entryInput, collectionId).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Failed to auto-generate unique entry id")); 
  }

  @Operation(
    summary = "Modify an existing entry",
    tags = { "Entries" })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}")
  public Entry update(@Valid @RequestBody EntryInput entryInput, @PathVariable UUID id) {
    return service.update(entryInput, id).orElseThrow(
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
  }

  @Operation(
    summary = "Delete an existing entry",
    tags = { "Entries" })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }  
}
