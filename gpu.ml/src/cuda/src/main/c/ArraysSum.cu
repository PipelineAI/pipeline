#include <cuda_runtime.h>
#include <stdio.h>
#include <time.h>
#include <stdlib.h>

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

int main()
{
    dim3 gridSize;
    dim3 blockSize;

    int    const N       = 2053;
    size_t const N_BYTES = N * sizeof(float);
    int const BLOCK_SIZE = 512; 
   
    float *aH, *bH, *cH, *refH;
    float *aD, *bD, *cD;

    aH = (float*)malloc(N_BYTES);
    bH = (float*)malloc(N_BYTES);
    cH = (float*)malloc(N_BYTES);
    refH = (float*)malloc(N_BYTES);

    GenerateTestArrays(N, aH, bH, cH, refH);

    CheckErrorUtil(cudaMalloc((void**)&aD, N_BYTES));
    CheckErrorUtil(cudaMalloc((void**)&bD, N_BYTES));
    CheckErrorUtil(cudaMalloc((void**)&cD, N_BYTES));

    CheckErrorUtil(cudaMemcpy(aD, aH, N_BYTES, cudaMemcpyHostToDevice));
    CheckErrorUtil(cudaMemcpy(bD, bH, N_BYTES, cudaMemcpyHostToDevice));

    blockSize.x = BLOCK_SIZE; blockSize.y = 1; blockSize.z = 1;
    gridSize.x = ((N + BLOCK_SIZE - 1) / BLOCK_SIZE); gridSize.y = 1; gridSize.z = 1;

    ArraysSum<<<gridSize, blockSize>>>(aD, bD, cD, N);

    // Get Errors from kernel
    CheckErrorUtil(cudaDeviceSynchronize());
    CheckErrorUtil(cudaGetLastError());

    CheckErrorUtil(cudaMemcpy(cH, cD, N_BYTES, cudaMemcpyDeviceToHost));

    CompareArrays(N, cH, refH);

    CheckError(cudaFree(aD));
    CheckError(cudaFree(bD));
    CheckError(cudaFree(cD));

    free(aH); free(bH); free(cH); free(refH);
    
    CheckError(cudaDeviceReset());
    return 0;
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
        printf("Arrays do not match.\n");
    }
    else
    {
        printf("Arrays match.\n");
    }
}
