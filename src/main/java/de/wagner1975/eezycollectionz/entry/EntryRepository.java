package de.wagner1975.eezycollectionz.entry;

import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface EntryRepository extends ListCrudRepository<Entry, UUID> {
}
