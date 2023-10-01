package de.wagner1975.eezycollectionz;

import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import de.wagner1975.eezycollectionz.collection.CollectionController;
import de.wagner1975.eezycollectionz.collection.CollectionInput;
import de.wagner1975.eezycollectionz.entry.EntryController;
import de.wagner1975.eezycollectionz.entry.EntryInput;

@EnableConfigurationProperties(ApplicationProperties.class)
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

  @Bean
  public CommandLineRunner demo(CollectionController collectionController, EntryController entryController) {
		return (args) -> {
			UUID firstCollectionId = collectionController.create(CollectionInput.builder().name("First collection").build()).getId();
			entryController.create(EntryInput.builder().name("First entry (1)").build(), firstCollectionId);
			entryController.create(EntryInput.builder().name("Second entry (1)").build(), firstCollectionId);
			entryController.create(EntryInput.builder().name("Third entry (1)").build(), firstCollectionId);

			UUID secondCollectionId = collectionController.create(CollectionInput.builder().name("Second collection").build()).getId();
			entryController.create(EntryInput.builder().name("First entry (2)").build(), secondCollectionId);
			entryController.create(EntryInput.builder().name("Second entry (2)").build(), secondCollectionId);
			entryController.create(EntryInput.builder().name("Third entry (2)").build(), secondCollectionId);
		};
	}
}
