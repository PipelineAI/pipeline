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
    open('pipeline_runtimes/__init__.py').read(),
    re.M
    ).group(1)

setup(
    name = "pipeline-runtimes",
    packages = ["pipeline_runtimes"],
    version = version,
    description = "PipelineAI Runtimes",
    long_description = "PipelineAI Runtimes",
    author = "Chris Fregly",
    author_email = "chris@pipeline.io",
    url = "https://github.com/fluxcapacitor/pipeline/lib/runtimes",
    install_requires=[
        "pipeline-loggers==0.6",
        "pipeline-monitors==0.5",
        "pipeline-models==0.2",
        "cloudpickle==0.3.1",
        "tornado==4.5.1",
    ],
    dependency_links=[
    ]
)
