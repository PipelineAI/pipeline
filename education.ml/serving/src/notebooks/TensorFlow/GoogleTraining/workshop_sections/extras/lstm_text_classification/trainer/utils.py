import argparse
import json
import os
import urlparse
import tensorflow as tf


def base_parser():
    """Shared arguments for training and evaluation."""
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--input-filenames',
        required=True,
        nargs='+',
        type=gcs_file
    )
    parser.add_argument(
        '--sentence-length',
        type=int,
        default=64,
        help='length of input sentence vectors'
    )
    parser.add_argument(
        '--embedding-size',
        type=int,
        default=128,
        help='The embedding size.'
    )
    parser.add_argument(
        '--batch-size',
        type=int,
        default=1024,
        help='Batch size during training.'
    )
    parser.add_argument(
        '--output-dir',
        required=True,
        help='Directory to save trained models to'
    )
    parser.add_argument(
        '--num-epochs',
        type=int,
        default=2,
        help='Total number of epoch to complete while training.'
    )
    parser.add_argument(
        '--lstm-size',
        default=64,
        type=int,
        help='Size of internal state of the lstms'
    )
    return parser


def dispatch(callback, **kwargs):
    """Runs the task associated with each machine."""
    config = json.loads(os.environ.get('TF_CONFIG', '{}'))
    cluster = config.get('cluster')
    task = config.get('task', {})
    trial_id = task.get('trial', '')
    kwargs['output_dir'] = file_or_gcs_join(
        kwargs['output_dir'], trial_id)

    if cluster:
        task_type = task.get('type', 'master')
        task_index = task.get('index', 0)
        server = tf.train.Server(
            cluster,
            job_name=task_type,
            task_index=task_index
        )
        if task_type == 'ps':
            server.join()
        else:
            callback(
                server.target,
                len(cluster['ps']),
                is_chief=(task_type == 'master'),
                **kwargs
            )
    else:
        callback('', 1, is_chief=True, **kwargs)


def file_or_gcs_join(path, *paths):
    if is_gcs(path):
        return '/'.join([p.strip('/') for p in [path] + list(paths)])
    else:
        return os.path.join(path, *paths)


def is_gcs(path):
    return urlparse.urlparse(path).scheme == 'gs'


def gcs_file(path):
    if is_gcs(path):
        return path
    else:
        raise argparse.ArgumentTypeError("Must be a valid GCS path")
