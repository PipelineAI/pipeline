echo '...**** TABLES CREATED WITH A NON-HIVE-SUPPORTED SerDe LIKE com.databricks.spark.csv WILL NOT BE QUERYABLE BY HIVE ****...'

export HADOOP_USER_CLASSPATH_FIRST=true
hive
