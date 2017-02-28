
# Create a small TensorFlow graph

In this section we'll create and run a simple TensorFlow graph that does matrix multiplication.

This is a good way to check that everything in your installation is working okay, and lets you get a feel for creating a graph, defining inputs, and running a graph in a session.

## Step 1: Run a matrix multiplier graph

We'll first take a look at [`tf_matrix_mul.py`](tf_matrix_mul.py)
This code shows not only the use of matrix ops, but things like input placeholders, variable initialization, use of the `tf.Print` op, and running a graph in a session.

Next, we'll run the code and see if it does what we expect.
In this directory, from the command line, run:

```sh
$ python tf_matrix_mul.py
```

## Step 2: Alter the graph: add matrix addition

Try altering this graph to add `m3` to itself, and store the result in `m4`. Then, alter the `session.run()` call to return the evaluation results for both `m3` and `m4`.

Don't look at it first, but `tf_matrix_mul_add.py` shows how you can do this.

