package com.advancedspark.serving.servlet;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableAutoConfiguration
public class SampleApp {

    @RequestMapping("/predict/{userId}/{itemId}")
    String predict(@PathVariable int userId, @PathVariable int itemId) {
        return "Hello World!";
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SampleApp.class, args);
    }
}
