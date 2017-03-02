"""
Code that goes along with the Airflow located at:
http://airflow.readthedocs.org/en/latest/tutorial.html
"""
from airflow import DAG
from airflow.contrib.operators.dataproc_operator import DataprocClusterCreateOperator
from datetime import datetime, timedelta


default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime.now(),
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

dag = DAG('pyspark_pi', default_args=default_args)

# t1 is an example of tasks created by instatiating operators
t1 = DataprocClusterCreateOperator(
    cluster_name='airflow_cluster',
    project_id='zeta-handler-147008',
    num_workers=2,
    zone='europe-west1-c',
    storage_bucket='shareactor-recommendations',
    dag=dag)
