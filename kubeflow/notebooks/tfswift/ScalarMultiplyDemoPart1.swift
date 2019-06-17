import TensorFlow

struct ScalarMultiply {
    
    var w: Tensor<Float>
    
    init() {
        w = Tensor<Float>(randomUniform: [1])
    }
    
    func call(_ input: Tensor<Float>) -> Tensor<Float> {
        return input * w
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

let layer = ScalarMultiply()
print(layer.call(inputs))
