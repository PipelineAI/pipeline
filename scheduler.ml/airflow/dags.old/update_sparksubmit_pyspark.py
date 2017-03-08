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

dag = DAG('update_sparksubmit_pyspark', default_args=default_args)
#, schedule_interval=timedelta(0))

pull_git = BashOperator(
    task_id='pull_git',
    bash_command='cd /root/pipeline && git pull',
    dag=dag)

# t1 is an example of tasks created by instatiating operators
sparksubmit_pyspark = BashOperator(
    task_id='sparksubmit_pyspark',
    bash_command='spark-submit --master local[*] /root/pipeline/jupyterhub.ml/scripts/pi.py 10',
    dag=dag)

# Setup Airflow DAG
sparksubmit_pyspark.set_upstream(pull_git)
