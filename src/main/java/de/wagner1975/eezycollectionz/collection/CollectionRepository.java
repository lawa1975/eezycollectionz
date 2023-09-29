package de.wagner1975.eezycollectionz.collection;

import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

interface CollectionRepository extends ListCrudRepository<Collection, UUID> {
}
