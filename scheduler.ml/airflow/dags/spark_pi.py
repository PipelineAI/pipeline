"""
Code that goes along with the Airflow located at:
http://airflow.readthedocs.org/en/latest/tutorial.html
"""
from airflow import DAG
from airflow.operators import BashOperator
from datetime import datetime, timedelta


default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime(2016, 4, 22),
    'email': ['chris@fregly.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
    # 'queue': 'bash_queue',
    # 'pool': 'backfill',
    # 'priority_weight': 10,
    # 'end_date': datetime(2016, 4, 24),
}

dag = DAG('spark_pi', default_args=default_args)

# t1 is an example of tasks created by instatiating operators
t1 = BashOperator(
    task_id='spark_pi',
    bash_command='spark-submit --class org.apache.spark.examples.SparkPi --master local[*] $SPARK_HOME/examples/jars/spark-examples_2.11-2.0.1-SNAPSHOT.jar 10',
    dag=dag)
