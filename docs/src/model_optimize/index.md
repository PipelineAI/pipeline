# Optimize Your Model 
Upon deploying your model, PipelineIO will optimize your model for high-performance serving and predicting.

Various model optimization and simplification techniques include folding batch normalizations, quantizing weights, and generating native code for both CPU and GPU.

![Nvidia GPU](/img/nvidia-cuda-338x181.png) 

![Intel CPU](/img/intel-logo-250x165.png)

## Examples

### TensorFlow
![TensorFlow](/img/tensorflow-logo-150x128.png)

**Unoptimized Linear Regression**

![Unoptimized TensorFlow Linear Regression](/img/unoptimized_tensorflow_linear.png)

**Optimized Linear Regression**

![Optimized TensorFlow Linear Regression](/img/optimized_tensorflow_linear.png)

**Weight Quantization**

![Weight Quantization 1](img/weight-quantization-1.png)

![Weight Quantization 2](img/weight-quantization-2.png)

### Spark ML
![Spark](/img/spark-logo-150x78.png)

![Generate and Optimize Spark ML Model](/img/ml-model-generating-and-optimizing.png) 

**Native Code Generation: Airbnb Price Prediction(Linear Regression)**
```
public class Airbnb_Price_LinearRegressionModel {
  public Object predict(Map<String, Object> inputs) {
        Double bathrooms = (Double)inputs.get("bathrooms");
        Double bedrooms = (Double)inputs.get("bedrooms");
        Double square_feet = (Double)inputs.get("square_feet");
        String room_type = (String)inputs.get("room_type");
        String host_is_super_host = (String)inputs.get("host_is_super_host");
        String cancellation_policy = (String)inputs.get("cancellation_policy");
        ...        
        Double __regressionTableNumber00 = -31.985533969928042;
        if (room_type != null) {
            __regressionTableNumber00 += 25.89385313144676 * ((room_type.equals("Entire home/apt")) ? 1 : 0);
        }
        if (room_type != null) {
            __regressionTableNumber00 += -13.556162863521244 * ((room_type.equals("Private room")) ? 1 : 0);
        }
        if (host_is_super_host != null) {
            __regressionTableNumber00 += -5.761601400860295 * ((host_is_super_host.equals("0.0")) ? 1 : 0);
        }
        if (cancellation_policy != null) {
            __regressionTableNumber00 += 2.3566340923908315 * ((cancellation_policy.equals("strict")) ? 1 : 
        ...
        price = __regressionTableNumber00;

        return price;
    }
}
```

**Native Code Generation: Income Prediction (Decision Tree)**
``` 
public class Census_Income_DecisionTreeModel
    public Object predict(Map<String, Object> inputs) {
        String income = (String)nameToValue.get("income");
        String education = (String)nameToValue.get("education");
        String marital_status = (String)nameToValue.get("marital_status");
        String occupation = (String)nameToValue.get("occupation");
        String native_country = (String)nameToValue.get("native_country");
        Integer age = (Integer)nameToValue.get("age");
        Integer education_num = (Integer)nameToValue.get("education_num");
        Integer capital_gain = (Integer)nameToValue.get("capital_gain");
        Integer capital_loss = (Integer)nameToValue.get("capital_loss");
        Integer hours_per_week = (Integer)nameToValue.get("hours_per_week");
        income = "<=50K";
        Boolean __succ0 = false;
        if (!__succ0) {
            Integer __predicateValue1 = marital_status == null? 3 : (marital_status.equals("Married-civ-spouse") ? 2 : 1);
            if (__predicateValue1 == 1) {
                __succ0 = true;
                income = "<=50K";
                Boolean __succ2 = false;
                if (!__succ2) {
                    Integer __predicateValue3 = capital_gain == null? 3 : ((capital_gain <= 7688)? 1 : 2);
                    if (__predicateValue3 == 1) {
                        __succ2 = true;
                        income = "<=50K";
                        Boolean __succ4 = false;
                        if (!__succ4) {
                            Integer __predicateValue5 = education_num == null? 3 : ((education_num <= 13)? 1 : 2);
                            if (__predicateValue5 == 1) {
                                __succ4 = true;
                                income = "<=50K";
                                Boolean __succ6 = false;
        ...
        if (!__succ0) {
            income = null;
            resultExplanation = null;
        }

        return income;         
    }
} 
```

{!contributing.md!}
