# tribe.version
# Stores version information such that it can be read by setuptools.
#
# Author:   Benjamin Bengfort <bbengfort@districtdatalabs.com>
# Created:  Thu Feb 18 20:14:16 2016 -0500
#
# Copyright (C) 2016 District Data Labs
# For license information, see LICENSE.txt
#
# ID: version.py [3467699] benjamin@bengfort.com $

"""
Stores version information such that it can be read by setuptools.
"""

##########################################################################
## Imports
##########################################################################

__version_info__ = {
    'major': 1,
    'minor': 1,
    'micro': 3,
    'releaselevel': 'final',
    'serial': 0,
}


def get_version(short=False):
    """
    Computes a string representation of the version from __version_info__.
    """
    assert __version_info__['releaselevel'] in ('alpha', 'beta', 'final')
    vers = ["%(major)i.%(minor)i" % __version_info__, ]
    if __version_info__['micro']:
        vers.append(".%(micro)i" % __version_info__)
    if __version_info__['releaselevel'] != 'final' and not short:
        vers.append('%s%i' % (__version_info__['releaselevel'][0],
                              __version_info__['serial']))
    return ''.join(vers)
