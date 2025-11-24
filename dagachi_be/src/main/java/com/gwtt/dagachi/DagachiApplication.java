package com.gwtt.dagachi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DagachiApplication {

  public static void main(String[] args) {
    SpringApplication.run(DagachiApplication.class, args);
  }
}
