# tribe.utils
# Utility functions and decorators for Tribe
#
# Author:   Benjamin Bengfort <benjamin@bengfort.com>
# Created:  Thu Nov 13 13:44:09 2014 -0500
#
# Copyright (C) 2014 Bengfort.com
# For license information, see LICENSE.txt
#
# ID: utils.py [5232e54] benjamin@bengfort.com $

"""
Utility functions and decorators for Tribe
"""

##########################################################################
## Imports
##########################################################################

import time

from dateutil import parser
from dateutil.tz import tzlocal, tzutc
from datetime import date, datetime, timedelta
from email.utils import unquote as email_unquote
from email.utils import parsedate_tz, parsedate, mktime_tz

##########################################################################
## Format constants
##########################################################################

EMAIL_DATETIME   = "%a, %d %b %Y %H:%M:%S %z"
EMAIL_TZ_DATE    = "%a, %d %b %Y %H:%M:%S %z (%Z)"
HUMAN_DATETIME   = "%a %b %d %H:%M:%S %Y %z"
HUMAN_DATE       = "%b %d, %Y"
HUMAN_TIME       = "%I:%M:%S %p"
JSON_DATETIME    = "%Y-%m-%dT%H:%M:%S.%fZ" # Must be UTC
ISO8601_DATETIME = "%Y-%m-%dT%H:%M:%S%z"
ISO8601_DATE     = "%Y-%m-%d"
ISO8601_TIME     = "%H:%M:%S"
COMMON_DATETIME  = "%d/%b/%Y:%H:%M:%S %z"

##########################################################################
## Date Parser Utility
##########################################################################

def parse_date(dtstr):
    """
    Attempts to parse a date with given formats first, then default formats
    """
    # Handle empty string or None
    if not dtstr: return None

    try:
        # Attempt to use the email utils parser first
        dt = parsedate_tz(dtstr)
        if dt is not None:
            return datetime.utcfromtimestamp(mktime_tz(dt)).replace(tzinfo=tzutc())

        # Otherwise use the dateutil parser
        return parser.parse(dtstr)
    except:
        return None

def strfnow(fmt=HUMAN_DATETIME):
    """
    Returns a string representation of the current timestamp
    """
    return datetime.now(tzlocal()).strftime(fmt)

##########################################################################
## Other Helpers and Decorators
##########################################################################

def unquote(str):
    """
    Return a new string which is an unquoted version of str. If str ends
    and begins with double quotes, they are stripped off. Likewise if str
    ends and begins with angle brackets, they are stripped off.

    This method continues to unquote until the string is unchanged.
    """
    new = email_unquote(str)
    if new != str:
        return unquote(new)
    return new

def timeit(func):
    """
    Decorator that times the execution of a function
    """
    def wrapper(*args, **kwargs):
        start  = time.time()
        result = func(*args, **kwargs)
        delta  = time.time() - start
        return result, delta
    return wrapper
