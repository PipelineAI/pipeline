from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from datetime import datetime, timedelta

default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime.now(),
    'email': ['chris@fregly.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 0,
    'retry_delay': timedelta(minutes=5),
    # 'queue': 'bash_queue',
    # 'pool': 'backfill',
    # 'priority_weight': 10,
    # 'end_date': datetime(2016, 4, 24),
}

dag = DAG('pyspark_submit', default_args=default_args)

# t1 is an example of tasks created by instatiating operators
sparksubmit_pyspark = BashOperator(
    task_id='pyspark_submit',
    bash_command='spark-submit --master local[*] /root/src/spark/pi.py 10',
    dag=dag)

# Setup Airflow DAG
sparksubmit_pyspark
