# -*- coding: utf-8 -*-

"""setup.py: setuptools control."""

import re
from setuptools import setup

version = re.search(
    '^__version__\s*=\s*"(.*)"',
    open('pio/pio.py').read(),
    re.M
    ).group(1)

setup(
    name = "pio-cli",
    packages = ["pio"],
    entry_points = {
        "console_scripts": ['pio = pio.pio:main']
    },
    version = version,
    description = "PipelineIO CLI",
    long_description = "PipelineIO CLI",
    author = "Chris Fregly",
    author_email = "chris@fregly.com",
    url = "https://github.com/fluxcapacitor/pipeline/cli",
    install_requires=[
        "kubernetes==2.0.0",
        "fire==0.1.0",
        "requests==2.13.0",
        "pyyaml==3.12",
        "dill==0.2.5",
        "tabulate==0.7.7",
    ],
    dependency_links=[
    ]
)
