package de.wagner1975.eezycollectionz;

import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import de.wagner1975.eezycollectionz.collection.Collection;
import de.wagner1975.eezycollectionz.collection.CollectionRepository;

@EnableConfigurationProperties(ApplicationProperties.class)
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

  @Bean
  public CommandLineRunner demo(CollectionRepository repository) {
		return (args) -> {
    	repository.save(Collection.builder().id(UUID.randomUUID()).name("Erste Merkliste").build());
    	repository.save(Collection.builder().id(UUID.randomUUID()).name("Zweite Merkliste").build());	
    	repository.save(Collection.builder().id(UUID.randomUUID()).name("Dritte Merkliste").build());
		};
	}
}
