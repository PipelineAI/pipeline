from prometheus_client import CollectorRegistry, generate_latest, start_http_server, Summary, Counter, Histogram, Gauge
from timeit import default_timer

REGISTRY = CollectorRegistry()

__version__ = "0.1"

class PipelineMonitor(object):
    def __init__(self,
                 labels: dict,
                 action: str,
                 description: str):
        self._labels = labels

        self._counter = Counter('%s_counter' % action, description, list(labels.keys()))
        self._summary = Summary('%s_summary' % action, description, list(labels.keys()))
        REGISTRY.register(self._counter)
        REGISTRY.register(self._summary)        

    def __enter__(self, *args, **kwargs):
        self._counter.labels(*list(self._labels.values())).inc()
        self._start = default_timer()

    def __exit__(self, *args, **kwargs):
        self._summary.labels(*list(self._labels.values())).observe(max(default_timer() - self._start, 0))
