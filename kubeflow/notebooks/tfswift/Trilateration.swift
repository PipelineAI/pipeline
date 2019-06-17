import TensorFlow

let p1: Tensor<Float> = Tensor([2, -2])
let p2: Tensor<Float> = Tensor([10, 8])
let p3: Tensor<Float> = Tensor([-1, 6])
let points = [p1, p2, p3]
let r1: Tensor<Float> = Tensor(4)
let r2: Tensor<Float> = Tensor(10)
let r3: Tensor<Float> = Tensor(5)
let distances = [r1, r2, r3]

@differentiable
func euclideanDistance(p1: Tensor<Float>, p2: Tensor<Float>) -> Tensor<Float> {
    return sqrt((p1 - p2).squared().sum())
}

@differentiable
func squaredError(predicted: Tensor<Float>, expected: Tensor<Float>) -> Tensor<Float> {
    return (expected - predicted).squared()
}

@differentiable(wrt: guess)
func trilaterationError(points: [Tensor<Float>], distances: [Tensor<Float>], guess: Tensor<Float>) -> Tensor<Float> {
    let p1G = euclideanDistance(p1: points[0], p2: guess)
    let p2G = euclideanDistance(p1: points[1], p2: guess)
    let p3G = euclideanDistance(p1: points[2], p2: guess)
    let p1E = squaredError(predicted: p1G, expected: distances[0])
    let p2E = squaredError(predicted: p2G, expected: distances[1])
    let p3E = squaredError(predicted: p3G, expected: distances[2])
    return p1E + p2E + p3E
}

var guess: Tensor<Float> = Tensor([1, 1])

for _ in 1...100 {
    let grad = guess.gradient { guess -> Tensor<Float> in
        return trilaterationError(points: points, distances: distances, guess: guess)
    }
    let scaledNegatedGrad = grad * -0.1
    guess = guess + scaledNegatedGrad
}

print(trilaterationError(points: points, distances: distances, guess: guess)) // Expected to be close to zero
