"""
Code that goes along with the Airflow located at:
http://airflow.readthedocs.org/en/latest/tutorial.html
"""
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
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
    # 'queue': 'bash_queue',
    # 'pool': 'backfill',
    # 'priority_weight': 10,
    # 'end_date': datetime(2016, 4, 24),
}

dag = DAG(
    'update_pmml', default_args=default_args, schedule_interval=timedelta(0))

t1 = BashOperator(
    task_id='rid_old_pmml',
    bash_command='sudo docker stop pmmlsvc||true;sudo docker rm pmmlsvc||true;sudo docker rmi canary||true',
    dag=dag)

t2 = BashOperator(
    task_id='deploy_new_pmml',
    bash_command='sudo docker build -t canary /home/pipeline-training/pipeline/prediction.ml/pmml;sudo docker run -d -p 9050:9050 --name pmmlsvc -t canary',
    dag=dag)

t2.set_upstream(t1)
