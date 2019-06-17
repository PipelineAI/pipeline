import TensorFlow

@differentiable(vjp: vjpNewtonianSquareRoot)
func newtonianSquareRoot(_ x: Tensor<Float>) -> Tensor<Float> {
    func error(_ x: Tensor<Float>, _ g: Tensor<Float>) -> Tensor<Float> {
        return abs(x - g.squared())
    }
    
    func dError(_ x: Tensor<Float>, _ g: Tensor<Float>) -> Tensor<Float> {
        return -(((2 * g) * (x - g.squared())) / error(x, g))
    }
    
    var g = x / 2
    while error(x, g) > 0.000001 {
        g -= error(x, g) / dError(x, g)
    }
    
    return g
}

func vjpNewtonianSquareRoot(_ x: Tensor<Float>) -> (Tensor<Float>, (Tensor<Float>) -> (Tensor<Float>)) {
    let value = newtonianSquareRoot(x)
    return (value, { v -> Tensor<Float> in
        return 1 / (2 * value)
    })
}

print(newtonianSquareRoot(Tensor<Float>(3)))
print(Tensor<Float>(3).gradient { x -> Tensor<Float> in
    newtonianSquareRoot(x)
})
