package de.wagner1975.eezycollectionz.entry;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.wagner1975.eezycollectionz.collection.Collection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
public class Entry {
  @Id
  @NotNull
  private UUID id;

  private Instant createdAt;
  
  private Instant lastModifiedAt;

  @NotBlank
  private String name;

  @ManyToOne
  @NotNull
  @JsonIgnore
  private Collection collection;
}
