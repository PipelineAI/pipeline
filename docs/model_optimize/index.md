# Optimize a Model 
Upon deploying your model, PipelineIO will attempt to optimize your model for high-performance serving and predicting.

Various model optimization and simplification techniques include folding batch normalizations, quantizing weights, and generating native code for both CPU and GPU.

## Examples

### TensorFlow: Quantizing Weights
Unoptimized Linear Regression 

Optimized Linear Regression

### Spark ML: Generating Native Code
Unoptimized Decision Tree

Optimized Decision Tree
 
{!contributing.md!}
