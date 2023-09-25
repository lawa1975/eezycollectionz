package de.wagner1975.eezycollectionz.collection;

import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface CollectionRepository extends ListCrudRepository<Collection, UUID> {
}
