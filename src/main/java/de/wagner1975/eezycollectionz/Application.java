package de.wagner1975.eezycollectionz;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import de.wagner1975.eezycollectionz.collection.CollectionController;
import de.wagner1975.eezycollectionz.collection.CollectionInput;

@EnableConfigurationProperties(ApplicationProperties.class)
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

  @Bean
  public CommandLineRunner demo(CollectionController controller) {
		return (args) -> {
			controller.create(CollectionInput.builder().name("Erste Merkliste").build());
			controller.create(CollectionInput.builder().name("Zweite Merkliste").build());
			controller.create(CollectionInput.builder().name("Dritte Merkliste").build());
		};
	}
}
