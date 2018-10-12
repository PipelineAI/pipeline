#!/usr/bin/env python3

import importlib
import os, subprocess
import logging
from pipeline_monitor import prometheus_monitor as monitor
from pipeline_monitor import prometheus_monitor_registry as monitor_registry
from prometheus_client import CollectorRegistry, generate_latest, start_http_server, Summary, Counter, Histogram, Gauge

from confluent_kafka import Consumer, KafkaError

#define('PIPELINE_RESOURCE_NAME', default='', help='model name', type=str)
#define('PIPELINE_RESOURCE_TAG', default='', help='model tag', type=str)
#define('PIPELINE_RESOURCE_SUBTYPE', default='', help='model type', type=str)
#define('PIPELINE_RUNTIME', default='', help='model runtime', type=str)
#define('PIPELINE_CHIP', default='', help='model chip', type=str)
#define('PIPELINE_RESOURCE_PATH', default='', help='model path', type=str)

#define('PIPELINE_STREAM_LOGGER_URL', default='', help='model stream logger url', type=str)
#define('PIPELINE_STREAM_LOGGER_TOPIC', default='', help='model stream logger topic', type=str)
#define('PIPELINE_STREAM_INPUT_URL', default='', help='model stream input url', type=str)
#define('PIPELINE_STREAM_INPUT_TOPIC', default='', help='model stream input topic', type=str)
#define('PIPELINE_STREAM_OUTPUT_URL', default='', help='model stream output url', type=str)
#define('PIPELINE_STREAM_OUTPUT_TOPIC', default='', help='model stream output topic', type=str)

_logger = logging.getLogger(__name__)
_logger .setLevel(logging.INFO)

def main():
    try:
#        if not (    options.PIPELINE_RESOURCE_NAME
#                and options.PIPELINE_RESOURCE_TAG
#                and options.PIPELINE_RESOURCE_SUBTYPE
#                and options.PIPELINE_RUNTIME
#                and options.PIPELINE_CHIP
#                and options.PIPELINE_RESOURCE_PATH
#                and options.PIPELINE_STREAM_LOGGER_URL
#                and options.PIPELINE_STREAM_LOGGER_TOPIC
#                and options.PIPELINE_STREAM_INPUT_URL
#                and options.PIPELINE_STREAM_INPUT_TOPIC
#                and options.PIPELINE_STREAM_OUTPUT_URL
#                and options.PIPELINE_STREAM_OUTPUT_TOPIC):

#            _logger.error('--PIPELINE_RESOURCE_NAME \
#                           --PIPELINE_RESOURCE_TAG \
#                           --PIPELINE_RESOURCE_SUBTYPE \
#                           --PIPELINE_RUNTIME \
#                           --PIPELINE_CHIP \
#                           --PIPELINE_RESOURCE_PATH \
#                           --PIPELINE_STREAM_LOGGER_URL \
#                           --PIPELINE_STREAM_LOGGER_TOPIC \
#                           --PIPELINE_STREAM_INPUT_URL \
#                           --PIPELINE_STREAM_INPUT_TOPIC \
#                           --PIPELINE_STREAM_OUTPUT_URL \
#                           --PIPELINE_STREAM_OUTPUT_TOPIC \
#                           must be set')
#            return

#        _logger.info('Model Kafka Server main: begin start kafka based consumer listening for stream logger url {0}, stream logger topic {1}, stream input url {2}, stream input topic {3}, and stream output url {4} and stream output topic {5}'.format(options.PIPELINE_STREAM_LOGGER_URL, options.PIPELINE_STREAM_LOGGER_TOPIC, options.PIPELINE_STREAM_INPUT_URL, options.PIPELINE_STREAM_INPUT_TOPIC, options.PIPELINE_STREAM_OUTPUT_URL, options.PIPELINE_STREAM_OUTPUT_TOPIC))

        settings = {
            'bootstrap.servers': 'community.pipeline.ai:31092',
            # 'bootstrap.servers': options.PIPELINE_STREAM_INPUT_URL,
            'group.id': 'consumer-1',
            'client.id': 'client-1',
            'enable.auto.commit': True,
            'session.timeout.ms': 6000,
            'default.topic.config': {'auto.offset.reset': 'smallest'}
        }  
    except Exception as e:
        _logger.info('model_kafka_server_python.main: Exception {0}'.format(str(e)))
        _logger.exception('model_kafka_server_python.main: Exception {0}'.format(str(e)))
        return


    try:
        consumer = Consumer(settings)
        consumer.subscribe(['input'])
        # consumer.subscribe([options.PIPELINE_STREAM_INPUT_TOPIC])

        while True:
            msg = consumer.poll(0.1)
            if msg is None:
                continue
            elif not msg.error():
                print('Received message: {0}'.format(msg.value()))

                # TODO:  model.predict()

                # TODO:  Write prediction to `PIPELINE_STREAM_OUTPUT_URL`:`PIPELINE_STREAM_OUTPUT_TOPIC`
            elif msg.error().code() == KafkaError._PARTITION_EOF:
                print('End of partition reached {0}/{1}'
                      .format(msg.topic(), msg.partition()))
            else:
                print('Error occured: {0}'.format(msg.error().str()))
    except Exception as e:
        _logger.info('model_kafka_server_python.main: Exception {0}'.format(str(e)))
        _logger.exception('model_kafka_server_python.main: Exception {0}'.format(str(e)))
    finally:
        if consumer:
            consumer.close()


if __name__ == '__main__':
    main()
