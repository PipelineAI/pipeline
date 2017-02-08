# Derived from https://github.com/rxin/spark-prometheus.git

# Prometheus Data Source for Apache Spark

This library is still work-in-progress. The main remaining TODO before GA are:

- Create unit tests and end-to-end tests (via a mock HTTP server)
- Polish the interface
  - Automatically infer start and end time via query predicates
  - Allow reading endpoint from a session wide config
- Clean up the code (especially the JSON parsing part)
- Error handling

Once polished, I will move this over to Databricks' official repository.


[![Build Status](https://travis-ci.org/rxin/spark-prometheus.svg?branch=master)](https://travis-ci.org/rxin/spark-prometheus)
[![codecov.io](http://codecov.io/github/rxin/spark-prometheus/coverage.svg?branch=master)](http://codecov.io/github/rxin/spark-prometheus?branch=master)

## Requirements

This library only works with Spark 2.0 or newer and Java 8.
