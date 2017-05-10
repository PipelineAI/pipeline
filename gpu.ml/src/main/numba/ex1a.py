import numpy as np

from numba import cuda

@cuda.jit
def histogram(x, xmin, xmax, histogram_out):
    nbins = histogram_out.shape[0]
    bin_width = (xmax - xmin) / nbins

    start = cuda.grid(1)
    stride = cuda.gridsize(1)

    for i in range(start, x.shape[0], stride):
        bin_number = np.int32((x[i] - xmin)/bin_width)
        if bin_number >= 0 and bin_number < histogram_out.shape[0]:
            histogram_out[bin_number] += 1
            print('in range', x[i], bin_number)
        else:
            print('out of range', x[i], bin_number)

x = np.random.normal(size=50, loc=0, scale=1).astype(np.float32)
xmin = np.float32(-4.0)
xmax = np.float32(4.0)
histogram_out = np.zeros(shape=10, dtype=np.int32)

histogram[64, 64](x, xmin, xmax, histogram_out)

print('input count:', x.shape[0])
print('histogram:', histogram_out)
print('count:', histogram_out.sum())
