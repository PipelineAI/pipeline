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

dag = DAG('deploy_prediction_codegen', default_args=default_args)

# TODO:  dockerFileTag and dockerFilePath should be passed in from webhook
build_image = BashOperator(
    task_id='build_docker_image',
    bash_command='sudo docker build -t fluxcapacitor/prediction-codegen /root/pipeline/prediction.ml/codegen/',
    dag=dag)

push_image = BashOperator(
    task_id='push_docker_image',
    bash_command='sudo docker push fluxcapacitor/prediction-codegen',
    dag=dag)

switch_to_aws = BashOperator(
    task_id='switch_to_aws',
    bash_command='sudo kubectl config use-context awsdemo',
    dag=dag)

deploy_container_aws = BashOperator(
    task_id='deploy_container_aws',
    bash_command='sudo kubectl create -f /root/pipeline/prediction.ml/codegen-rc.yaml',
    dag=dag)

switch_to_gcp = BashOperator(
    task_id='switch_to_gcp',
    bash_command='sudo kubectl config use-context gcpdemo', 
    dag=dag)

deploy_container_gcp = BashOperator(
    task_id='deploy_container_gcp',
    bash_command='sudo kubectl create -f /root/pipeline/prediction.ml/codegen-rc.yaml',
    dag=dag)

# Setup Airflow DAG
push_image.set_upstream(build_image)
switch_to_aws.set_upstream(push_image)
deploy_container_aws.set_upstream(switch_to_aws)
switch_to_gcp.set_upstream(deploy_container_aws)
deploy_container_gcp.set_upstream(switch_to_gcp)
