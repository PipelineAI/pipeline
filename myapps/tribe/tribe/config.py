# tribe.config
# Configuration for the Tribe project
#
# Author:   Benjamin Bengfort <benjamin@bengfort.com>
# Created:  Wed Nov 12 20:50:40 2014 -0500
#
# Copyright (C) 2014 District Data Labs
# For license information, see LICENSE.txt
#
# ID: config.py [a537f84] benjamin@bengfort.com $

"""
Uses confire to get meaningful configurations from a yaml file
"""

##########################################################################
## Imports
##########################################################################

import os
import confire

##########################################################################
## Configuration
##########################################################################

class TribeConfiguration(confire.Configuration):
    """
    Meaningful defaults and required configurations.

    debug:    the app will print or log debug statements
    testing:  the app will not overwrite important resources
    """

    CONF_PATHS = [
        "/etc/tribe.yaml",                      # System configuration
        os.path.expanduser("~/.tribe.yaml"),    # User specific config
        os.path.abspath("conf/tribe.yaml"),     # Local configuration
    ]

    debug    = True
    testing  = True


## Load settings immediately for import
settings = TribeConfiguration.load()

if __name__ == '__main__':
    print settings
