package com.advancedspark.serving.recommendations;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class GetRecommendationsModelCommand extends HystrixCommand<String> {
    private final String modelName;

    public GetRecommendationsModelCommand(String modelName) {
        super(HystrixCommandGroupKey.Factory.asKey("GetRecommendationsModel"));
        this.modelName = modelName;
    }

    @Override
    protected String run() {
        // a real example would do work like a network call here
        return "Hello " + modelName + "!";
    }
}
