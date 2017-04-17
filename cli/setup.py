# -*- coding: utf-8 -*-

"""setup.py: setuptools control."""

import re
from setuptools import setup

version = re.search(
    '^__version__\s*=\s*"(.*)"',
    open('pio/pio.py').read(),
    re.M
    ).group(1)

#with open("README.md", "rb") as f:
#    long_descr = f.read().decode("utf-8")

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
        "sklearn_pandas",
        "kubernetes",
        "fire",
        "requests",
        "pyyaml",
        "pick",
    ],
    dependency_links=[
        "git+https://github.com/jpmml/sklearn2pmml.git"
    ]
)
