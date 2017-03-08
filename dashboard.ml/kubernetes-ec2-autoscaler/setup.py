from setuptools import setup

setup(name='autoscaler',
      version='0.0.1',
      install_requires=[
          'pykube',
          'requests',
          'ipdb',
          'boto',
          'boto3',
          'botocore',
          'click',
      ]
)

