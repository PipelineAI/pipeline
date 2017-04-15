import os


def get_data(data_directory):
    """
    Given a string directory path, returns a list of file paths to images
    inside that directory.

    Data must be in jpeg format.

    Args:
        data_directory (str): String directory location of input data

    Returns:
        :obj:`list` of :obj:`str`: list of image file paths
    """
    return [
        data_directory + f
        for
        f
        in
        os.listdir(data_directory)
        if
        f.endswith('.jpeg') or f.endswith('.jpg')
    ]
