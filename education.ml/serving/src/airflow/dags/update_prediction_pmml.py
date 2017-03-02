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

dag = DAG('update_prediction_pmml', default_args=default_args)

pull_git = BashOperator(
    task_id='pull_git',
    bash_command='cd /root/pipeline && git pull',
    dag=dag)

# TODO:  dockerFileTag and dockerFilePath should be passed in from webhook
build_image = BashOperator(
    task_id='build_docker_image',
    bash_command='sudo docker build -t fluxcapacitor/prediction-pmml /root/pipeline/prediction.ml/pmml/',
    dag=dag)

push_image = BashOperator(
    task_id='push_docker_image',
    bash_command='sudo docker push fluxcapacitor/prediction-pmml',
    dag=dag)

update_container_aws = BashOperator(
    task_id='update_container_aws',
    bash_command='kubectl rolling-update prediction-pmml --context=awsdemo --image-pull-policy=Always --image=fluxcapacitor/prediction-pmml',
    dag=dag)

update_container_gcp = BashOperator(
    task_id='update_container_gcp',
    bash_command='kubectl rolling-update prediction-pmml --context=gcpdemo --image-pull-policy=Always --image=fluxcapacitor/prediction-pmml',
    dag=dag)

# Setup Airflow DAG
build_image.set_upstream(pull_git)
push_image.set_upstream(build_image)
update_container_aws.set_upstream(push_image)
update_container_gcp.set_upstream(push_image)
