#include <cuda_runtime.h>
#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <ctime>

#define CheckErrorUtil(err) CheckError(err, __FUNCTION__, __LINE__)
#define CheckErrorMsgUtil(err, msg) CheckErrorMsg(err, msg, __FUNCTION__, __LINE__)

inline void CheckError(cudaError_t const err, char const* const fun, const int line)
{
    if (err)
    {
        printf("CUDA Error Code[%d]: %s\n%s() Line:%d\n", err, cudaGetErrorString(err), fun, line);
        exit(1);
    }
}

inline void CheckErrorMsg(cudaError_t const err, char const* const msg, char const* const fun, int const line)
{
    if (err)
    {
        printf("CUDA Error Code[%d]: %s\n%s() Line:%d\n%s\n", err, cudaGetErrorString(err), fun, line, msg);
        exit(1);
    }
}

void GenerateTestArrays(int const N, float* const a, float* const b, float* const c, float* const ref);
void CompareArrays(int const N, float const* const a, float const* const b);

__global__ void ArraysSum(float* const a, float* const b, float* const c, int const N)
{
    int i = blockIdx.x*blockDim.x + threadIdx.x;

    if (i < N)
        c[i] = a[i] + b[i];
}

void GenerateTestArrays(int const N, float* const a, float* const b, float* const c, float* const ref)
{
    int i;

    srand((unsigned)time(NULL));

    for(i = 0; i < N; i++)
    {
        a[i] = (float)rand();
        b[i] = (float)rand();
        c[i] = 0.0f;
        ref[i] = a[i] + b[i];
    }
}

void CompareArrays(int const N, float const* const a, float const* const b)
{
    int i;
    int different = 0;

    for(i = 0; i < N; i++)
    {
        different = (a[i] != b[i]);
        if(different)
            break;
    }

    if(different)
    {
        printf("\n\n*** Oh No!  The GPU failed to sum the arrays. ***\n\n\n");
    }
    else
    {
        printf("\n\n*** Awesome!  The GPU summed the arrays!! ***\n\n\n");
    }
}

int main()
{
    std::clock_t start;
    start = std::clock();

    dim3 gridSize;
    dim3 blockSize;

    int    const N       = 8192000;
    size_t const N_BYTES = N * sizeof(float);
    int const BLOCK_SIZE = 512;

    float *aH, *bH, *cH, *refH;
    float *aD, *bD, *cD;

    aH = (float*)malloc(N_BYTES);
    bH = (float*)malloc(N_BYTES);
    cH = (float*)malloc(N_BYTES);
    refH = (float*)malloc(N_BYTES);

    printf("\n\nGenerating 2 random float arrays on Host - each of size %lu bytes...\n", N_BYTES);
    GenerateTestArrays(N, aH, bH, cH, refH);

    printf("Allocating %lu bytes on Device GPU to store the 2 generated arrays...\n", 2 * N_BYTES);
    CheckErrorUtil(cudaMalloc((void**)&aD, N_BYTES));
    CheckErrorUtil(cudaMalloc((void**)&bD, N_BYTES));

    printf("Allocating %lu bytes on Device GPU to store the result array after summing the 2 arrays...\n", N_BYTES);
    CheckErrorUtil(cudaMalloc((void**)&cD, N_BYTES));

    printf("Copying 2 arrays from Host to Device GPU...\n");
    CheckErrorUtil(cudaMemcpy(aD, aH, N_BYTES, cudaMemcpyHostToDevice));
    CheckErrorUtil(cudaMemcpy(bD, bH, N_BYTES, cudaMemcpyHostToDevice));

    blockSize.x = BLOCK_SIZE; blockSize.y = 1; blockSize.z = 1;
    gridSize.x = ((N + BLOCK_SIZE - 1) / BLOCK_SIZE); gridSize.y = 1; gridSize.z = 1;

    printf("Summing the 2 arrays and storing the result array on Device GPU...\n");
    ArraysSum<<<gridSize, blockSize>>>(aD, bD, cD, N);

    printf("Synchronizing the Device GPU memory before copying the result array back to Host...\n");
    CheckErrorUtil(cudaDeviceSynchronize());
    CheckErrorUtil(cudaGetLastError());

    printf("Copying result array from Device GPU to Host...\n");
    CheckErrorUtil(cudaMemcpy(cH, cD, N_BYTES, cudaMemcpyDeviceToHost));

    printf("Comparing expected result array stored on Host with actual result calculated on Device GPU...\n");
    CompareArrays(N, cH, refH);

    printf("Freeing %lu bytes on Device GPU...\n", 3 * N_BYTES);
    CheckErrorUtil(cudaFree(aD));
    CheckErrorUtil(cudaFree(bD));
    CheckErrorUtil(cudaFree(cD));

    printf("Freeing memory on Host...\n");
    free(aH);
    free(bH); 
    free(cH); 
    free(refH);

    printf("Resetting Device GPU as though nothing ever happened!\n\n");
    CheckErrorUtil(cudaDeviceReset());

    printf("Executed in %.f milliseconds.\n\n", (std::clock() - start) / (double)(CLOCKS_PER_SEC / 1000));

    return 0;
}
