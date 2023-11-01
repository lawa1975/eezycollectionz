package de.wagner1975.eezycollectionz.collection;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

interface CollectionRepository extends
  PagingAndSortingRepository<Collection, UUID>,
  ListCrudRepository<Collection, UUID>
{}
