import math

def ind_max(x):
  m = max(x)
  return x.index(m)

class UCB2(object):
  def __init__(self, alpha, counts, values):
    """
    UCB2 algorithm. Implementation of the slides at:
    http://lane.compbio.cmu.edu/courses/slides_ucb.pdf
    """
    self.alpha = alpha
    self.counts = counts
    self.values = values
    self.__current_arm = 0
    self.__next_update = 0
    return
  
  def initialize(self, n_arms):
    self.counts = [0 for col in range(n_arms)]
    self.values = [0.0 for col in range(n_arms)]
    self.r = [0 for col in range(n_arms)]
    self.__current_arm = 0
    self.__next_update = 0
  
  def __bonus(self, n, r):
    tau = self.__tau(r)
    bonus = math.sqrt((1. + self.alpha) * math.log(math.e * float(n) / tau) / (2 * tau))
    return bonus
  
  def __tau(self, r):
    return int(math.ceil((1 + self.alpha) ** r))
  
  def __set_arm(self, arm):
    """
    When choosing a new arm, make sure we play that arm for
    tau(r+1) - tau(r) episodes.
    """
    self.__current_arm = arm
    self.__next_update += max(1, self.__tau(self.r[arm] + 1) - self.__tau(self.r[arm]))
    self.r[arm] += 1
  
  def select_arm(self):
    n_arms = len(self.counts)
    
    # play each arm once
    for arm in range(n_arms):
      if self.counts[arm] == 0:
        self.__set_arm(arm)
        return arm
    
    # make sure we aren't still playing the previous arm.
    if self.__next_update > sum(self.counts):
      return self.__current_arm
    
    ucb_values = [0.0 for arm in range(n_arms)]
    total_counts = sum(self.counts)
    for arm in xrange(n_arms):
      bonus = self.__bonus(total_counts, self.r[arm])
      ucb_values[arm] = self.values[arm] + bonus
    
    chosen_arm = ind_max(ucb_values)
    self.__set_arm(chosen_arm)
    return chosen_arm
  
  def update(self, chosen_arm, reward):
    self.counts[chosen_arm] = self.counts[chosen_arm] + 1
    n = self.counts[chosen_arm]
    
    value = self.values[chosen_arm]
    new_value = ((n - 1) / float(n)) * value + (1 / float(n)) * reward
    self.values[chosen_arm] = new_value
