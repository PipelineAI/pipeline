package com.advancedspark.serving.metrics.turbine;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;

@Configuration
@EnableAutoConfiguration
public class TurbineApplication {
    public static void main(String[] args) {
        boolean cloudEnvironment = new StandardEnvironment().acceptsProfiles("cloud");
        new SpringApplicationBuilder(TurbineApplication.class).web(!cloudEnvironment).run(args);
    }
}
