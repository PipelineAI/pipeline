# Optimize Model 
Upon deploying your model, PipelineIO will attempt to optimize your model for high-performance serving and predicting.

Various model optimization and simplification techniques include folding batch normalizations, quantizing weights, and generating native code for both CPU and GPU.

## Examples

### TensorFlow: Quantizing Weights
![TensorFlow](/img/tensorflow-logo-150x128.png)

Unoptimized Linear Regression 

Optimized Linear Regression

### Spark ML: Generating Native Code
![Generate and Optimize Spark ML Model](/img/ml-model-generating-and-optimizing.png) 

![Nvidia GPU](/img/nvidia-cuda-338x181.png) 

![Intel CPU](/img/intel-logo-250x165.png)

Unoptimized Decision Tree

Optimized Decision Tree
 
{!contributing.md!}
