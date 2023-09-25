package de.wagner1975.eezycollectionz;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class HomeController {

  private final ApplicationProperties properties;
  
  @GetMapping("/")
  public ApplicationProperties home() {
    return properties;
  } 
}
