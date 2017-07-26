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
    open('pipeline_loggers/__init__.py').read(),
    re.M
    ).group(1)

setup(
    name = "pipeline-loggers",
    packages = ["pipeline_loggers"],
    version = version,
    description = "PipelineAI Loggers",
    long_description = "PipelineAI Loggers",
    author = "Chris Fregly",
    author_email = "chris@pipeline.io",
    url = "https://github.com/fluxcapacitor/pipeline/lib/loggers",
    install_requires=[
        "pipeline-monitors>=0.6"
    ],
    dependency_links=[
    ]
)
