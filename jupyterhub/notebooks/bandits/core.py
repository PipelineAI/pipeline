# Convenience functions
def ind_max(x):
  m = max(x)
  return x.index(m)

# Need access to random numbers
import random

# Definitions of bandit arms
from arms.adversarial import *
from arms.bernoulli import *
from arms.normal import *

# Definitions of bandit algorithms
from algorithms.epsilon_greedy.standard import *
from algorithms.epsilon_greedy.annealing import *
from algorithms.softmax.standard import *
from algorithms.softmax.annealing import *
from algorithms.ucb.ucb1 import *
from algorithms.ucb.ucb2 import *
from algorithms.exp3.exp3 import *
from algorithms.hedge.hedge import *

# # Testing framework
from testing_framework.tests import *
