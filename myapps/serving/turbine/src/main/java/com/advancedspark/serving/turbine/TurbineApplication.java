package com.advancedspark.serving.turbine;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.turbine.EnableTurbine;

@SpringBootApplication
@EnableTurbine
public class TurbineApplication {
    public static void main(String[] args) {
        boolean cloudEnvironment = new StandardEnvironment().acceptsProfiles("cloud");
        new SpringApplicationBuilder(TurbineApplication.class).web(!cloudEnvironment).run(args);
    }
}
