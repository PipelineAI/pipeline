PYSPARK_DRIVER_PYTHON=ipython PYSPARK_DRIVER_PYTHON_OPTS="notebook --no-browser --port=7778" pyspark --packages com.databricks:spark-csv_2.10:1.1.0 --master spark://spark_master_hostname:7077 --executor-memory 6400M --driver-memory 6400M

# RUN THIS EVER TIME FOR SOME REASON
ssh -N -f -L 127.0.0.1:7776:127.0.0.1:7777

