from prometheus_client import CollectorRegistry, generate_latest, start_http_server, Summary, Counter, Histogram, Gauge
from timeit import default_timer

__version__ = "1.0.1"

prometheus_monitor_registry = CollectorRegistry()

class prometheus_monitor(object):

    _instances = {}

    def __new__(cls, labels: dict, name: str):
        if name in cls._instances:
            return cls._instances[name]
        instance = super().__new__(cls)
        instance._labels = labels
        instance._name = name
        instance._counter = Counter('%s_counter' % instance._name, instance._name, list(instance._labels.keys()))
        instance._summary = Summary('%s_summary' % instance._name, instance._name, list(instance._labels.keys()))
        prometheus_monitor_registry.register(instance._counter)
        prometheus_monitor_registry.register(instance._summary)        
        cls._instances[name] = instance
        return instance

    def __enter__(self, *args, **kwargs):
        self._counter.labels(*list(self._labels.values())).inc()
        self._start = default_timer()

    def __exit__(self, *args, **kwargs):
        self._summary.labels(*list(self._labels.values())).observe(max(default_timer() - self._start, 0))

    # Interface for decorator
    def __call__(self, function, *args):

        def wrapped_function(*args):
            with self:
                return function(*args)

        return wrapped_function
