package de.wagner1975.eezycollectionz.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import lombok.NoArgsConstructor;

@Repository
@NoArgsConstructor
class CollectionTransientRepository {

  private final List<Collection> collectionList = new ArrayList<>();

  List<Collection> findAll() {
    return collectionList;
  }

  Optional<Collection> findById(UUID id) {
    return collectionList.stream().filter(w -> w.getId().equals(id)).findFirst();
  }

  boolean existsById(UUID id) {
    return collectionList.stream().filter(w -> w.getId().equals(id)).findFirst().isPresent() ;
  }

  void save(Collection watchlist) {
    deleteById(watchlist.getId());
    collectionList.add(watchlist);
  }

  void deleteById(UUID id) {
    collectionList.removeIf(w -> w.getId().equals(id));    
  }
}