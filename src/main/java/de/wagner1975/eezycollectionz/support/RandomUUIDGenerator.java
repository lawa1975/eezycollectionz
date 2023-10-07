package de.wagner1975.eezycollectionz.support;

import java.util.UUID;
import org.springframework.stereotype.Component;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class RandomUUIDGenerator implements IdGenerator<UUID> {
  public UUID generate() {
    return UUID.randomUUID();
  }
}
