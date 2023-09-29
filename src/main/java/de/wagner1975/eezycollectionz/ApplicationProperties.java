package de.wagner1975.eezycollectionz;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(value = "eezycollectionz")
public record ApplicationProperties(
  String welcomeMessage,
  String author,

  @DefaultValue("3")
  int maxRetriesToGenerateId
) {
}
