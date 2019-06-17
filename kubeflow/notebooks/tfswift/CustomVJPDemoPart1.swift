import TensorFlow

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

print(newtonianSquareRoot(Tensor<Float>(3)))
