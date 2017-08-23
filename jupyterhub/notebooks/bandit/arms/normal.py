import random

class NormalArm():
  def __init__(self, mu, sigma):
    self.mu = mu
    self.sigma = sigma
  
  def draw(self):
    return random.gauss(self.mu, self.sigma)

  
