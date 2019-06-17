import TensorFlow

extension Tensor where Scalar: TensorFlowFloatingPoint {
    @differentiable
    func squaredError(expected: Tensor<Scalar>) -> Tensor<Scalar> {
        return (self - expected).squared()
    }
}

struct Coordinate: Differentiable {
    var x: Tensor<Float>
    var y: Tensor<Float>
    
    @differentiable
    func distance(to other: Coordinate) -> Tensor<Float> {
        let squaredErrorX = (x - other.x).squared()
        let squaredErrorY = (y - other.y).squared()
        let sumSquaredError = squaredErrorX + squaredErrorY
        return sqrt(sumSquaredError)
    }
}

extension Coordinate {
    init(x: Float, y: Float) {
        self.x = Tensor(x)
        self.y = Tensor(y)
    }
}

struct TrilaterationReferences: Differentiable {
    struct ReferenceCoordinate: Differentiable {
        var location: Coordinate
        var expectedDistance: Tensor<Float>
        
        init(location: Coordinate, expectedDistance: Float) {
            self.location = location
            self.expectedDistance = Tensor(expectedDistance)
        }
    }
    
    var ref1: ReferenceCoordinate
    var ref2: ReferenceCoordinate
    var ref3: ReferenceCoordinate
    
    @differentiable
    func error(for guess: Coordinate) -> Tensor<Float> {
        let error1 = guess
            .distance(to: ref1.location)
            .squaredError(expected: ref1.expectedDistance)
        let error2 = guess
            .distance(to: ref2.location)
            .squaredError(expected: ref2.expectedDistance)
        let error3 = guess
            .distance(to: ref3.location)
            .squaredError(expected: ref3.expectedDistance)
        return error1 + error2 + error3
    }
}

let references = TrilaterationReferences(
    ref1: .init(
        location: .init(x: 2, y: -2),
        expectedDistance: 4
    ),
    ref2: .init(
        location: .init(x: 10, y: 8),
        expectedDistance: 10
    ),
    ref3: .init(
        location: .init(x: -1, y: 6),
        expectedDistance: 5
    )
)

var guess = Coordinate(x: 1, y: 1)

for _ in 1...100 {
    let grad = guess.gradient { guess -> Tensor<Float> in
        return references.error(for: guess)
    }
    guess.x += grad.x * -0.1
    guess.y += grad.y * -0.1
}

print(references.error(for: guess)) // Expected to be close to zero
