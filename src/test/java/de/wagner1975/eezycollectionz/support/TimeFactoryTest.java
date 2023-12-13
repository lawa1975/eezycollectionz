package de.wagner1975.eezycollectionz.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoField;

import org.junit.jupiter.api.Test;

class TimeFactoryTest {

  @Test
  void now_Invoked_BetweenBeforeAndAfter() throws InterruptedException {

    var before = new TimeFactory().now();
    Thread.sleep(100);
    var now = new TimeFactory().now();
    Thread.sleep(100);
    var after = new TimeFactory().now();

    assertNotNull(now);
    
    assertNotNull(before);
    assertTrue(before.isBefore(now));

    assertNotNull(after);
    assertTrue(after.isAfter(now));
  }

  @Test
  void now_Invoked_CorrectPrecision() throws InterruptedException {
    var seconds = 1702468152L;
    var nanos = 888888;

    var timeFactory = new TimeFactory() {
      protected Instant retrieveNow() {
        return Instant.ofEpochSecond(seconds, nanos);
      };
    };

    var result = timeFactory.now();

    assertNotNull(result);
    assertEquals(seconds, result.getLong(ChronoField.INSTANT_SECONDS));
    assertEquals(888000, result.getNano());
  }  
}
