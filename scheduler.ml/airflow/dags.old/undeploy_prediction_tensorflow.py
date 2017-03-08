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

dag = DAG('undeploy_prediction_tensorflow', default_args=default_args)

# TODO:  dockerFileTag and dockerFilePath should be passed in from webhook
switch_to_aws = BashOperator(
    task_id='switch_to_aws',
    bash_command='sudo kubectl config use-context awsdemo',
    dag=dag)

undeploy_container_aws = BashOperator(
    task_id='undeploy_container_to_aws',
    bash_command='sudo kubectl delete prediction-tensorflow',
    dag=dag)

switch_to_gcp = BashOperator(
    task_id='switch_to_gcp',
    bash_command='sudo kubectl config use-context gcpdemo', 
    dag=dag)

undeploy_container_gcp = BashOperator(
    task_id='undeploy_container_gcp',
    bash_command='sudo kubectl delete prediction-tensorflow',
    dag=dag)

# Setup Airflow DAG
undeploy_container_aws.set_upstream(switch_to_aws)
switch_to_gcp.set_upstream(undeploy_container_aws)
undeploy_container_gcp.set_upstream(switch_to_gcp)
