execfile("core.py")
from algorithms.ucb.ucb2 import *
import random

random.seed(1)
means = [0.1, 0.1, 0.1, 0.1, 0.9]
n_arms = len(means)
random.shuffle(means)
arms = map(lambda (mu): BernoulliArm(mu), means)
print("Best arm is " + str(ind_max(means)))

for alpha in [0.1, 0.3, 0.5, 0.7, 0.9]:
    algo = UCB2(alpha, [], [])
    algo.initialize(n_arms)
    results = test_algorithm(algo, arms, 5000, 250)

    f = open("algorithms/ucb/ucb2_results_%s.tsv" % alpha, "w")

    for i in range(len(results[0])):
        f.write("\t".join([str(results[j][i]) for j in range(len(results))]))
        f.write("\t%s\n" % alpha)

    f.close()
