package com.advancedspark.serving.recommendations;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class GetRecommendationsModelCommand extends HystrixCommand<Transformer> {
    private final String modelName;

    public GetRecommendationsModelCommand(String modelName) {
        super(HystrixCommandGroupKey.Factory.asKey("GetRecommendationsModel"));
        this.modelName = modelName;
    }

    @Override
    protected Transformer run() {
	// Retrieve latest model from source (disk, Redis, whatever)
        //S modelJson = scala.io.Source.fromFile("/root/pipeline/work/serving/recommendations/als/model.json")
        //val fallbackRecommendationsModel = try source.mkString finally source.close()
        //return fallbackRecommendationsModel;    
	return null;
    }

    @Override
    protected Transformer getFallback() {
 	// Retrieve fallback (ie. non-personalized top k)
        //val source = scala.io.Source.fromFile("/root/pipeline/datasets/serving/recommendations/fallback/model.json")
        //val fallbackRecommendationsModel = try source.mkString finally source.close()
        //return fallbackRecommendationsModel;
	return null;
    }
}
