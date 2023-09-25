package de.wagner1975.eezycollectionz;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "eezycollectionz")
public record ApplicationProperties(
  String welcomeMessage,
  String author
) {
}
