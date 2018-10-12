# -*- coding: utf-8 -*-

"""setup.py: setuptools control."""

import re
from setuptools import setup

#import sys
#if not sys.version_info[0] == 3:
#    print("\n \
#    sys.exit("\n \
#              ****************************************************************\n \
#              * The CLI has only been tested with Python 3+ at this time.    *\n \
#              * Report any issues with Python 2 by emailing help@pipeline.io *\n \
#              ****************************************************************\n")

version = re.search(
    '^__version__\s*=\s*"(.*)"',
    open('pipeline_monitor/__init__.py').read(),
    re.M
    ).group(1)

# Get the long description from the relevant file
with open('README.rst', encoding='utf-8') as f:
    long_description = f.read()

with open('requirements.txt', encoding='utf-8') as f:
    requirements = [line.rstrip() for line in f.readlines()]

setup(
    name = "pipeline-monitor",
    packages = ["pipeline_monitor"],
    version = version,
    description = "PipelineAI Monitor",
    long_description = "%s\n\nRequirements:\n%s" % (long_description, requirements),
    author = "Chris Fregly",
    author_email = "github@pipeline.ai",
    url = "https://github.com/PipelineAI/",
    install_requires=requirements,
    dependency_links=[
    ]
)
