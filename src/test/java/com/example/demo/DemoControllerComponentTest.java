package com.example.demo;

import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.Locale;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoControllerComponentTest {

  private static final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
  @LocalServerPort
  private int port;

  @Autowired
  ApplicationContext context;

  WebTestClient client;

  @BeforeEach
  public void setup() {
    final var baseUrl = "http://localhost:" + port;
    this.client = WebTestClient.bindToServer()
        .baseUrl(baseUrl) // Configure the base URL
        .filter(new ObservationFilterFunction(new SwaggerValidator(baseUrl + "/api/v1")))
        .build();
  }

  @Test
  void getHello() {
    client.get()
        .uri("/api/v1/hello")
        .exchange()
        .expectStatus().isOk()
        .expectBody(Greeting.class)
        .isEqualTo(new Greeting("Hello World!"))
        .consumeWith(result -> System.out.println(result.getResponseBody()));
  }


  private static class ObservationFilterFunction implements ExchangeFilterFunction {

    final SwaggerValidator validator;

    ObservationFilterFunction(SwaggerValidator validator) {
      this.validator = validator;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {

      return next.exchange(request)
          .flatMap(response -> {
            final var headers = response.headers().asHttpHeaders();
            final var responseBody = response.body(BodyExtractors.toDataBuffers());

            // Validate the request and response
            final var method = Request.Method.valueOf(request.method().name().toUpperCase(Locale.ROOT));
            final var simpleRequest = new SimpleRequest
                .Builder(method, request.url().toString())
                .build();

            return response.bodyToMono(String.class)
                .map(body -> {
                  final var simpleResponse = new SimpleResponse
                      .Builder(response.statusCode().value())
                      .withContentType(response.headers().contentType().map(MimeType::toString).orElse(null))
                      .withBody(body)
                      .build();
                  validator.validate(simpleRequest, simpleResponse);

                  // Return the response with the original headers and body
                  return response.mutate()
                      .headers(h -> h.addAll(headers))
                      .body(body)
                      .build();
                });
          });
    }
  }
}
