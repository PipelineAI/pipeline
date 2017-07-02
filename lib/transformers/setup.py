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
    open('pipeline_transformers/__init__.py').read(),
    re.M
    ).group(1)

setup(
    name = "pipeline_transformers",
    packages = ["pipeline_transformers"],
    version = version,
    description = "PipelineAI Transformers",
    long_description = "PipelineAI Transformers",
    author = "Chris Fregly",
    author_email = "chris@pipeline.io",
    url = "https://github.com/fluxcapacitor/pipeline/transformers",
    install_requires=[
        "pandas==0.20.2",
        "cloudpickle==0.3.1",
    ],
    dependency_links=[
    ]
)
