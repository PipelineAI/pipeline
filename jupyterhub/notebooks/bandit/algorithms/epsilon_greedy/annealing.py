import random
import math

def ind_max(x):
  m = max(x)
  return x.index(m)

class AnnealingEpsilonGreedy():
  def __init__(self, counts, values):
    self.counts = counts
    self.values = values
    return

  def initialize(self, n_arms):
    self.counts = [0 for col in range(n_arms)]
    self.values = [0.0 for col in range(n_arms)]
    return

  def select_arm(self):
    t = sum(self.counts) + 1
    epsilon = 1 / math.log(t + 0.0000001)
    
    if random.random() > epsilon:
      return ind_max(self.values)
    else:
      return random.randrange(len(self.values))
  
  def update(self, chosen_arm, reward):
    self.counts[chosen_arm] = self.counts[chosen_arm] + 1
    n = self.counts[chosen_arm]
    
    value = self.values[chosen_arm]
    new_value = ((n - 1) / float(n)) * value + (1 / float(n)) * reward
    self.values[chosen_arm] = new_value
    return
