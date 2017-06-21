"""
Example usage:
  $ python images_to_json.py -o request.json img1.jpg img2.jpg
"""

import argparse
import base64
from cStringIO import StringIO
import json
import sys

from PIL import Image

desired_width = 299
desired_height = 299


def parse_args():
  """Handle the command line arguments.
  Returns:
    Output of argparse.ArgumentParser.parse_args.
  """

  parser = argparse.ArgumentParser()
  parser.add_argument('-o', '--output', default='request.json',
                      help='Output file to write encoded images to.')
  parser.add_argument('-r', '--resize', dest='resize', action='store_true',
                      help='Will resize images locally first.  Not needed, but'
                      ' will reduce network traffic.')
  parser.add_argument('inputs', nargs='+', type=argparse.FileType('r'),
                      help='A list of .jpg or .jpeg files to serialize into a '
                      'request json')

  args = parser.parse_args()

  check = lambda filename: filename.lower().endswith(('jpeg', 'jpg'))
  if not all(check(input_file.name) for input_file in args.inputs):
    sys.stderr.write('All inputs must be .jpeg or .jpg')
    sys.exit(1)

  return args


def make_request_json(input_images, output_json, do_resize):
  """Produces a JSON request suitable to send to CloudML Prediction API.
  Args:
    input_images: List of file handles corresponding to images to be encoded.
    output_json: File handle of the output json where request will be written.
    do_resize: Boolean specifying if script should resize images.
  """

  with open(output_json, 'w') as ff:
    for image_handle in input_images:
      # Uses argparse to check permissions, but ignore pre-opened file handle.
      image = Image.open(image_handle.name)
      image_handle.close()
      resized_handle = StringIO()
      is_too_big = ((image.size[0] * image.size[1]) >
                    (desired_width * desired_height))
      if do_resize and is_too_big:
        image = image.resize((299, 299), Image.BILINEAR)

      image.save(resized_handle, format='JPEG')
      encoded_contents = base64.b64encode(resized_handle.getvalue())

      # key can be any UTF-8 string, since it goes in a HTTP request.
      row = json.dumps({'key': image_handle.name,
                        'image_bytes': {'b64': encoded_contents}})

      ff.write(row)
      ff.write('\n')

  print 'Wrote {} images to {}'.format(len(input_images), output_json)


def main():
  args = parse_args()
  make_request_json(args.inputs, args.output, args.resize)


if __name__ == '__main__':
  main()
