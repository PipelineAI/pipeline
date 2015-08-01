Real-time Rating Simulator Setup
================================

1. ZooKeeper and Kafka need to be running before this runs.
2. src/main/resources/application.conf contains the application config and specifies the following:
  a. the data file is located relative to the runtime path at 'datasets/dating/ratings.dat'
  b. kafka is running on 'localhost:9200'
  c. kafka topic name is 'ratings'
3. Run with `sbt run`
