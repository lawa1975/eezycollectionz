package de.wagner1975.eezycollectionz;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestConstants {

  public static final String UUID_V4_REGEX = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-4[a-fA-F0-9]{3}-[89abAB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}$";
  public static final String ISO_8601_DATE_REGEX = "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2}(?:\\.\\d*)?)((-(\\d{2}):(\\d{2})|Z)?)$";

  public static final String POSTGRESQL_DOCKER_IMAGE_NAME = "postgres:16.1-alpine3.18";
}
