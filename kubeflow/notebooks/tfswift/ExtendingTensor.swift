import TensorFlow

extension Tensor: ExpressibleByFloatLiteral where Scalar == Float {
    public typealias FloatLiteralType = Float
    
    public init(floatLiteral value: Float) {
        self = Tensor<Float>(value)
    }
}

let abc: Tensor<Float> = 5.0
print(abc.gradient { abc -> Tensor<Float> in
    return abc.squared()
})
