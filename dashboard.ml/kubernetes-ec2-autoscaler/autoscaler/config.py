import os


class Config(object):
    CAPACITY_DATA = os.environ.get('CAPACITY_DATA', 'data/capacity.json')
    CAPACITY_CPU_RESERVE = float(os.environ.get('CAPACITY_CPU_RESERVE', 0.3))
