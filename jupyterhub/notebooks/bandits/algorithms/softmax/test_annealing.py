execfile("core.py")

import random

random.seed(1)
means = [0.1, 0.1, 0.1, 0.1, 0.9]
n_arms = len(means)
random.shuffle(means)
arms = map(lambda (mu): BernoulliArm(mu), means)
print("Best arm is " + str(ind_max(means)))

algo = AnnealingSoftmax([], [])
algo.initialize(n_arms)
results = test_algorithm(algo, arms, 5000, 250)

f = open("algorithms/softmax/annealing_softmax_results.tsv", "w")

for i in range(len(results[0])):
  f.write("\t".join([str(results[j][i]) for j in range(len(results))]) + "\n")

f.close()
