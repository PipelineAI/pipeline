import TensorFlow

struct ScalarMultiply: Layer {
    
    var w: Tensor<Float>
    
    typealias Input = Tensor<Float>
    typealias Output = Tensor<Float>
    
    init() {
        w = Tensor<Float>(randomUniform: [1])
    }
    
    @differentiable
    func callAsFunction(_ input: Tensor<Float>) -> Tensor<Float> {
        return input * w
    }
    
    @differentiable
    func call(_ input: Tensor<Float>) -> Tensor<Float> {
        return callAsFunction(input)
    }
    
}

let inputs = Tensor<Float>([
    Tensor<Float>([1]),
    Tensor<Float>([3]),
    Tensor<Float>([-4]),
    Tensor<Float>([5]),
    Tensor<Float>([-1]),
    Tensor<Float>([-4]),
])
let outputs = Tensor<Float>([
    Tensor<Float>([-1]),
    Tensor<Float>([-3]),
    Tensor<Float>([4]),
    Tensor<Float>([-5]),
    Tensor<Float>([1]),
    Tensor<Float>([4]),
])

@differentiable
func loss(model: ScalarMultiply) -> Tensor<Float> {
    return (outputs - model.call(inputs)).squared().mean()
}

let layer = ScalarMultiply()
print(layer.call(inputs))
print(loss(model: layer))
print(layer.gradient { layer -> Tensor<Float> in
    loss(model: layer)
})
