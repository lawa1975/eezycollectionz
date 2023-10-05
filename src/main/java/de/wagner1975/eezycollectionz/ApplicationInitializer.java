package de.wagner1975.eezycollectionz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import de.wagner1975.eezycollectionz.collection.CollectionController;
import de.wagner1975.eezycollectionz.collection.CollectionInput;
import de.wagner1975.eezycollectionz.entry.EntryController;
import de.wagner1975.eezycollectionz.entry.EntryInput;

@Profile("!test")
@Component
public class ApplicationInitializer implements CommandLineRunner {

  @Autowired
  private CollectionController collectionController;

  @Autowired
  private EntryController entryController;

  @Override
  public void run(String... args) throws Exception {
			var firstCollectionId = collectionController.create(CollectionInput.builder().name("First collection").build()).getId();
			entryController.create(EntryInput.builder().name("First entry (1)").build(), firstCollectionId);
			entryController.create(EntryInput.builder().name("Second entry (1)").build(), firstCollectionId);
			entryController.create(EntryInput.builder().name("Third entry (1)").build(), firstCollectionId);

			var secondCollectionId = collectionController.create(CollectionInput.builder().name("Second collection").build()).getId();
			entryController.create(EntryInput.builder().name("First entry (2)").build(), secondCollectionId);
			entryController.create(EntryInput.builder().name("Second entry (2)").build(), secondCollectionId);
			entryController.create(EntryInput.builder().name("Third entry (2)").build(), secondCollectionId);
  }
}
