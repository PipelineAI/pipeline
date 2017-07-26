from prometheus_client import CollectorRegistry, generate_latest, start_http_server, Summary, Counter, Histogram, Gauge
from timeit import default_timer

__version__ = "0.6"

prometheus_monitor_registry = CollectorRegistry()

class prometheus_monitor(object):

    # Interface for 'with' scope context manager
    def __init__(self,
                 labels: dict,
                 name: str):
        self._labels = labels
        self._name = name
        self._counter = Counter('%s_counter' % self._name, self._name, list(self._labels.keys()))
        self._summary = Summary('%s_summary' % self._name, self._name, list(self._labels.keys()))
        prometheus_monitor_registry.register(self._counter)
        prometheus_monitor_registry.register(self._summary)        

    def __enter__(self, *args, **kwargs):
        self._counter.labels(*list(self._labels.values())).inc()
        self._start = default_timer()

    def __exit__(self, *args, **kwargs):
        self._summary.labels(*list(self._labels.values())).observe(max(default_timer() - self._start, 0))


    # Interface for decorator
    def __call__(self, function, *args):

        def wrapped_function(*args):
            self.__enter__(*args)
            response = function(*args)
            self.__exit__(*args)
            return response

        return wrapped_function
