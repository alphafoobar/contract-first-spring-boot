package com.example.demo;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.Response;

public class SwaggerValidator {

  private final String basePath;
  private final OpenApiInteractionValidator validator;

  public SwaggerValidator(String basePath) {
    this.basePath = basePath;
    this.validator = loadValidator("src/swagger.yaml");
  }

  public OpenApiInteractionValidator loadValidator(String path) {
    try {
      return OpenApiInteractionValidator.createForSpecificationUrl(path)
          .withBasePathOverride(basePath)
          .build();
    } catch (Exception e) {
      throw new RuntimeException("Error loading Swagger file: " + e.getMessage(), e);
    }
  }

  public void validate(Request request, Response response) {
    final var report = validator.validate(request, response);
    if (report.hasErrors()) {
      throw new RuntimeException("Swagger validation failed: " + report.getMessages());
    }
  }
}

