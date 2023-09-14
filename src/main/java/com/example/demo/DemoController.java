package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1")
public class DemoController {

      @GetMapping(path = "/hello", produces = "application/json")
      public Greeting hello() {
          return new Greeting("Hello World!");
      }
}
