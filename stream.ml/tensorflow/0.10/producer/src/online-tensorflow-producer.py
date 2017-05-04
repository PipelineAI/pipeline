from confluent_kafka import Producer
import glob
import os

def open_producer(stream_host_and_port_list):
    producer = Producer({'bootstrap.servers': stream_host_and_port_list, # kafka broker
                         'api.version.request':'true'
                        })
    return producer

def produce(producer, topic_name, image_paths_glob_str):
    image_paths = glob.glob(image_paths_glob_str)

    # TODO:  shuffle
    for image_path in image_paths:
        image_file = open(image_path, 'rb')
        image_bytes = image_file.read()
        image_file.close()
        print image_bytes

        image_name = os.path.basename(image_path)
        print image_name

        producer.produce(topic_name, key=image_name, value=image_bytes)
        producer.flush()

if __name__ == '__main__':
    stream_host_and_port_list = os.getenv('STREAM_HOST_AND_PORT_LIST')
    topic_name  = os.getenv('TOPIC_NAME')
    image_paths_glob_str = os.getenv('IMAGE_PATHS_GLOB_STRING')

    producer = open_producer(stream_host_and_port_list)

    produce(producer, topic_name, image_paths_glob_str)
