# tests.utils_tests
# Testing for the utilities library in Tribe
#
# Author:   Benjamin Bengfort <benjamin@bengfort.com>
# Created:  Sat Nov 15 08:39:08 2014 -0500
#
# Copyright (C) 2014 District Data Labs
# For license information, see LICENSE.txt
#
# ID: utils_tests.py [5232e54] benjamin@bengfort.com $

"""
Testing for the utilities library in Tribe
"""

##########################################################################
## Imports
##########################################################################

import unittest

from tribe.utils import *
from datetime import datetime
from dateutil import tz

##########################################################################
## Fixtures
##########################################################################

FMT_EMAIL_DATETIME   = "%a, %d %b %Y %H:%M:%S %z"
FMT_EMAIL_TZ_DATE    = "%a, %d %b %Y %H:%M:%S %z (%Z)"
FMT_HUMAN_DATETIME   = "%a %b %d %H:%M:%S %Y %z"
FMT_HUMAN_DATE       = "%b %d, %Y"
FMT_HUMAN_TIME       = "%I:%M:%S %p"
FMT_JSON_DATETIME    = "%Y-%m-%dT%H:%M:%SZ" # Must be UTC
FMT_ISO8601_DATETIME = "%Y-%m-%dT%H:%M:%S%z"
FMT_ISO8601_DATE     = "%Y-%m-%d"
FMT_ISO8601_TIME     = "%H:%M:%S"
FMT_COMMON_DATETIME  = "%d/%b/%Y:%H:%M:%S %z"

# These are all the same date and time (UTC and EST)
STR_EMAIL_DATETIME   = 'Sat, 15 Nov 2014 08:55:41 -0500'
STR_EMAIL_TZ_DATE    = 'Sat, 15 Nov 2014 08:55:41 -0500 (EST)'
STR_HUMAN_DATETIME   = 'Sat Nov 15 08:55:41 2014 -0500'
STR_HUMAN_DATE       = 'Nov 15, 2014'
STR_HUMAN_TIME       = '08:55:41 AM'
STR_JSON_DATETIME    = '2014-11-15T13:55:41Z'
STR_ISO8601_DATETIME = '2014-11-15T08:55:41-0500'
STR_ISO8601_DATE     = '2014-11-15'
STR_ISO8601_TIME     = '08:55:41'
STR_COMMON_DATETIME  = '15/Nov/2014:08:55:41 -0500'
STR_EMAIL_CASE_1     = 'Sat, 15 Nov 2014 13:55:41 +0000 (GMT+00:00)'

TZ_EST               = tz.tzoffset(None, -18000)
TZ_UTC               = tz.tzutc()

FIXTURE_NAIVE        = datetime(2014, 11, 15, 13, 55, 41)
FIXTURE_UTC          = datetime(2014, 11, 15, 13, 55, 41, tzinfo=TZ_UTC)
FIXTURE_EST          = datetime(2014, 11, 15,  8, 55, 41, tzinfo=TZ_EST)

def tznow(tz=TZ_UTC):
    return datetime.now(tz)

def toutc(dt):
    return dt.astimezone(TZ_UTC)

##########################################################################
## EmailAddress Tests
##########################################################################

class DateParsingTests(unittest.TestCase):

    @unittest.skip("Need a better date or time comparison function")
    def test_naive_parses(self):
        """
        Test non-timezone aware date parsing
        """
        fixtures = (
            STR_HUMAN_DATE,
            STR_HUMAN_TIME,
            STR_ISO8601_DATE,
            STR_ISO8601_TIME
        )

        for fixture in fixtures:
            self.assertEqual(parse_date(fixture), FIXTURE_NAIVE)


    def test_est_parses(self):
        """
        Test timezone aware date parsing (EST)
        """

        fixtures = (
            STR_EMAIL_DATETIME,
            STR_EMAIL_TZ_DATE,
            STR_HUMAN_DATETIME,
            STR_ISO8601_DATETIME,
            # STR_COMMON_DATETIME,
        )

        for fixture in fixtures:
            self.assertEqual(parse_date(fixture), FIXTURE_EST)

    def test_utc_parses(self):
        """
        Test timezone aware date parsing (UTC)
        """

        fixtures = (
            STR_JSON_DATETIME,
        )

        for fixture in fixtures:
            self.assertEqual(parse_date(fixture), FIXTURE_UTC)

    def test_email_edge_case(self):
        """
        Test the first found edge case in email dataset
        """
        self.assertEqual(parse_date(STR_EMAIL_CASE_1), FIXTURE_UTC)

    def test_strfnow(self):
        """
        Test the strfnow function
        """
        dtstr = strfnow()
        dtpsd = parse_date(dtstr)
        self.assertIsNotNone(dtpsd)
        self.assertIsNotNone(dtpsd.tzinfo)
