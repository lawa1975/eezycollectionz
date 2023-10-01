package de.wagner1975.eezycollectionz.entry;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface EntryRepository extends ListCrudRepository<Entry, UUID> {
  List<Entry> findByCollectionId(UUID collectionId);
}
