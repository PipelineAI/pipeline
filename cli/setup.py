# -*- coding: utf-8 -*-

"""setup.py: setuptools control."""

import re
from setuptools import setup

import sys
if not sys.version_info[0] == 3:
#    print("\n \
    sys.exit("\n \
              ****************************************************************\n \
              * The CLI has only been tested with Python 3+ at this time.    *\n \
              * Report any issues with Python 2 by emailing help@pipeline.io *\n \
              ****************************************************************\n")

version = re.search(
    '^__version__\s*=\s*"(.*)"',
    open('cli/__init__.py').read(),
    re.M
    ).group(1)


# Get the long description from the relevant file
with open('README.rst') as f:
    long_description = f.read()

with open('requirements.txt') as f:
    requirements = [line.rstrip() for line in f.readlines()]

setup(
    name = "pipeline-ai-cli",
    packages = ["cli"],
    entry_points = {
        "console_scripts": ['pipeline = cli.__init__:main']
    },
    version = version,
    description = "PipelineAI CLI",
    long_description = "%s\n\nRequirements:\n%s" % (long_description, requirements),
    author = "Chris Fregly",
    author_email = "chris@pipeline.io",
    url = "https://github.com/fluxcapacitor/pipeline/cli",
    install_requires=requirements,
    dependency_links=[
    ],
    data_files=[('templates', ['templates/predict-Dockerfile-tensorflow.template', 
                               'templates/predict-svc.yaml.template', 
                               'templates/predict-deploy.yaml.template', 
                               'templates/predict-autoscale.yaml.template'])]
 )
