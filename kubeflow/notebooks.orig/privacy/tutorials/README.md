# Tutorials

As demonstrated on MNIST in `mnist_dpsgd_tutorial.py`, the easiest way to use
a differentially private optimizer is to modify an existing training loop
to replace an existing vanilla optimizer with its differentially private
counterpart implemented in the library.

## Parameters

All of the optimizers share some privacy-specific parameters that need to
be tuned in addition to any existing hyperparameter. There are currently three:
* num_microbatches (int): The input data for each step (i.e., batch) of your
  original training algorithm is split into this many microbatches. Generally, 
  increasing this will improve your utility but slow down your training in terms
  of wall-clock time. The total number of examples consumed in one global step
  remains the same. This number should evenly divide your input batch size.
* l2_norm_clip  (float): The cumulative gradient across all network parameters
  from each microbatch will be clipped so that its L2 norm is at most this 
  value. You should set this to something close to some percentile of what 
  you expect the gradient from each microbatch to be. In previous experiments,
  we've found numbers from 0.5 to 1.0 to work reasonably well.
* noise_multiplier (float): This governs the amount of noise added during 
  training. Generally, more noise results in better privacy and lower utility. 
  This generally has to be at least 0.3 to obtain rigorous privacy guarantees,
  but smaller values may still be acceptable for practical purposes.

## Measuring Privacy

Differential privacy can be expressed using two values, epsilon and delta.
Roughly speaking, they mean the following:

* epsilon gives a ceiling on how much the probability of a particular output
  can increase by including (or removing) a single training example. We usually
  want it to be a small constant (less than 10, or, for more stringent privacy
  guarantees, less than 1). However, this is only an upper bound, and a large
  value of epsilon may still mean good practical privacy.
* delta bounds the probability of an arbitrary change in model behavior.
  We can usually set this to a very small number (1e-7 or so) without
  compromising utility. A rule of thumb is to set it to be less than the inverse
  of the training data size.

To find out the epsilon given a fixed delta value for your model, follow the
approach demonstrated in the `compute_epsilon` of the `mnist_dpsgd_tutorial.py`
where the arguments used to call the RDP accountant (i.e., the tool used to
compute the privacy guarantee) are:

* q : The sampling ratio, defined as (number of examples consumed in one
  step) / (total training examples).
* noise_multiplier : The noise_multiplier from your parameters above.
* steps : The number of global steps taken.
