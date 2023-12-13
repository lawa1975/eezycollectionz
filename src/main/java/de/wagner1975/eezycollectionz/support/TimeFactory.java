package de.wagner1975.eezycollectionz.support;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class TimeFactory {
  
  public Instant now() {
    return retrieveNow().truncatedTo(ChronoUnit.MICROS);
  }

  protected Instant retrieveNow() {
    return Instant.now();
  }
}
