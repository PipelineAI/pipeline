import model
import tensorflow as tf
import utils


def train(target,
          num_param_servers,
          is_chief,
          lstm_size=64,
          input_filenames=None,
          sentence_length=128,
          vocab_size=2**15,
          learning_rate=0.01,
          output_dir=None,
          batch_size=1024,
          embedding_size=128,
          num_epochs=2):

    graph = tf.Graph()
    with graph.as_default():
        sentences, scores = model.get_inputs(
            input_filenames, batch_size, num_epochs, sentence_length)

        with tf.device(tf.train.replica_device_setter()):
            lstm = model.BasicRegressionLSTM(
                sentences,
                scores,
                num_param_servers,
                vocab_size,
                learning_rate,
                embedding_size,
                lstm_size
            )

    tf.contrib.learn.train(
        graph,
        output_dir,
        lstm.train_op,
        lstm.loss,
        global_step_tensor=lstm.global_step,
        supervisor_is_chief=is_chief,
        supervisor_master=target
    )


if __name__ == "__main__":
    parser = utils.base_parser()
    parser.add_argument(
        '--learning-rate',
        type=float,
        default=0.01
    )
    utils.dispatch(
        train,
        **parser.parse_args().__dict__
    )
