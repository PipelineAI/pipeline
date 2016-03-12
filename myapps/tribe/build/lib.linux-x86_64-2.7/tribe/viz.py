# tribe.viz
# Visualization utility for Email social network
#
# Author:   Benjamin Bengfort <benjamin@bengfort.com>
# Created:  Thu Nov 20 16:28:40 2014 -0500
#
# Copyright (C) 2014 District Data Labs
# For license information, see LICENSE.txt
#
# ID: viz.py [b96b383] benjamin@bengfort.com $

"""
Visualization utility for Email social network
"""

##########################################################################
## Imports
##########################################################################

import math
import networkx as nx
from operator import itemgetter
from functools import wraps

try:
    import matplotlib.pyplot as plt
except (ImportError, RuntimeError):
    import warnings
    plt = None

def configure(func):
    """
    Configures visualization environment.
    """
    @wraps(func)
    def wrapper(*args, **kwargs):
        if plt is None:
            warnings.warn("matplotlib is not installed or you are using a virtualenv!")
            return None
        return func(*args, **kwargs)

    return wrapper

@configure
def show_simple_network(nodes=12, prob=0.2, hot=False):

    G = nx.erdos_renyi_graph(nodes, prob)
    pos = nx.spring_layout(G)
    nx.draw_networkx_nodes(G, pos, node_color='#0080C9', node_size=500, linewidths=1.0)
    nx.draw_networkx_edges(G, pos, width=1.0, style='dashed', alpha=0.75)

    if hot:
        center, degree = sorted(G.degree().items(), key=itemgetter(1))[-1]
        nx.draw_networkx_nodes(G, pos, nodelist=[center], node_size=600, node_color="#D9AF0B")

    plt.axis('off')
    plt.show()

    return G

@configure
def draw_social_network(G, path="graph.png", **kwargs):

    # A   = nx.to_agraph(G)
    # A.layout()

    # A.draw(path)

    k = 1/math.sqrt(G.order()) * 2
    pos = nx.spring_layout(G, k=k)

    deg = [100*v for v in G.degree().values()]

    nx.draw_networkx_nodes(G, pos, node_size=deg, linewidths=1.0, alpha=0.90)
    nx.draw_networkx_edges(G, pos, width=1.0, style='dashed', alpha=0.75)

    plt.show()
