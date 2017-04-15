import config
from queues import create_image_queue_graph
from models import create_model_graph
from train import train_model
from data import get_data


def main():
    """
    """
    placeholders = ['input', 'label']
    train_ops = ['train']
    log_ops = ['accuracy']
    files = get_data(config.DATA_DIRECTORY)
    queue_graph = create_image_queue_graph(files, config.PIXEL_DEPTH,
                                           config.HEIGHT, config.WIDTH,
                                           config.CHANNELS,
                                           config.BATCH_SIZE, config.CAPACITY)
    model_graph = create_model_graph(config.HEIGHT, config.WIDTH,
                                     config.CHANNELS, config.NUM_LABELS)
    train_model(queue_graph, model_graph, placeholders, train_ops, log_ops)


if __name__ == '__main__':
    main()
