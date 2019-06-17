import Foundation
import Python

let np = Python.import("numpy")

// Linear Algebra-based trilateration

let p1 = np.array([2, -2])
let p2 = np.array([10, 8])
let p3 = np.array([-1, 6])
let r1 = 4
let r2 = 10
let r3 = 5

func norm(_ v: PythonObject) -> PythonObject {
    return np.sqrt(np.sum(np.power(v, 2)))
}

let ex = (p2 - p1) / norm(p2 - p1)
let i = np.dot(ex, p3 - p1)
var a = (p3 - p1) - ex * i
let ey = a / norm(a)
let ez = np.cross(ex, ey)
let d = norm(p2 - p1)
let j = np.dot(ey, p3 - p1)
let x = (np.power(r1, 2) - np.power(r2, 2) + np.power(d, 2)) / (2 * d)
let y = (np.power(r1, 2) - np.power(r3, 2) + np.power(i, 2) + np.power(j, 2)) / (2 * j) - (i / j) * x
var b = np.power(r1, 2) - np.power(x, 2) - np.power(y, 2)

if np.abs(b) < 0.0000000001 {
    b = 0
}

let z = np.sqrt(b)
if Bool(np.isnan(z))! {
    print("Couldn't solve.")
    exit(0)
}

a = p1 + ex * x + ey * y
let location = [Float](a + ez * z)!

print(location) // Expecting [2, 2]
