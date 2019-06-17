import os
import shutil
import cv2


def resize_data(size, path):
    for path in paths:
        print('Resizing {}'.format(path))
        src_path = os.path.abspath(path)
        dst_path = os.path.abspath('.') + '/data'

        filenames = os.listdir(src_path)
        for filename in filenames:
            src_filepath = os.path.join(src_path, filename)
            dst_filepath = os.path.join(dst_path, filename)
            try:
                img = cv2.imread(src_filepath)
                img = cv2.resize(img, size)
                cv2.imwrite(dst_filepath, img)
            except:
                pass

if __name__ == '__main__':
    resize_paths = ['images']
    resize_data((224, 224), resize_paths)