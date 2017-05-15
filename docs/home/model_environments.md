# Environments

Below is the list of environments supported by PipelineIO. 

| Framework            | Environment / Model Type | Description                                      | GPU |
| -------------------- | ------------------------ | ------------------------------------------------ | --- |
| TensorFlow           | tensorflow               | Tensorflow 1.1, Python3                          |  Y  |
| Scikit-Learn         | scikit                   | Scikit-Learn 0.18, Python3, Numba                |  Y  |
| Spark                | spark                    | Spark 2.0.1, Scala 2.11, Java 1.8, Python3       |  Y  |
| XGBoost              | xgboost                  | XGBoost, Python3                                 |  Y  |
| Deep Learning 4J     | dl4j                     | Deep Learning 4J, Java 1.8                       |  Y  |
| R                    | R                        | R                                                |  Y  |
| Cassandra Key-Value  | cassandra-kv             | Cassandra 3.1, Java 1.8                          |  N  |
| Redis Key-Value      | redis-kv                 | Redis Key-Value, C++                             |  N  |
| TensorFlow Key-Value | tensorflow-kv            | TensorFlow Serving Key-Value, C++                |  Y  |
| Native Java          | java                     | Java 1.8                                         |  Y  |
| Native Python3       | python3                  | Python3, Numba                                   |  Y  |
| Native C++           | cpp                      | C++ 11                                           |  Y  |
| PMML                 | pmml                     | PMML 4.3, Java 1.8                               |  N  |
| Ensembles            | ensemble                 | All of the Above!                                |  Y  |


The following software packages (in addition to many other common libraries) are available in all the environments:
```
conda, h5py, iPython, Jupyter, matplotlib, numpy, OpenCV, Pandas, Pillow, scikit-learn, scipy, sklearn
```

{!contributing.md!}
