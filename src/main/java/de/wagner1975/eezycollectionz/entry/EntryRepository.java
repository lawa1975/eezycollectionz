package de.wagner1975.eezycollectionz.entry;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EntryRepository extends
  PagingAndSortingRepository<Entry, UUID>,
  ListCrudRepository<Entry, UUID>
{
  Page<Entry> findByCollectionId(UUID collectionId, Pageable pageable);
}
