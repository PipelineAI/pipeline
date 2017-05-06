from confluent_kafka import Consumer, KafkaError
import time
import os

class image:
    image_name = ''
    image_bytes = ''
    partition = ''
    offset = ''

def open_consumer(stream_host_and_port_list, topic_name, group_name):
    consumer = Consumer({'bootstrap.servers': stream_host_and_port_list, # kafka broker
                           'group.id': group_name, # consumer group
                           'api.version.request':'true'
                        })
    consumer.subscribe([topic_name])
    return consumer

def next_batch(consumer, batch_size):
    msg = consumer.poll(timeout=200)

    # TODO:  return batch_size # of images
    batch = []

    if msg is not None:
        img = image()
        img.image_name = msg.key()
        img.image_bytes = msg.value()
        img.partition = msg.partition()
        img.offset = msg.offset()

        batch.append(img)

    return batch

if __name__ == '__main__':
    stream_host_and_port_list = os.getenv('STREAM_HOST_AND_PORT_LIST')
    topic_name  = os.getenv('TOPIC_NAME')
    group_name = os.getenv('GROUP_NAME')

    consumer = open_consumer(stream_host_and_port_list, topic_name, group_name)

    batch_size = 16

    while True:
        batch = next_batch(consumer, batch_size)
        if len(batch) > 0:
            print(batch[0].image_bytes)
            print(batch[0].image_name)
        time.sleep(2)
