# tests.emails_tests
# Testing for the emails module in Tribe
#
# Author:   Benjamin Bengfort <benjamin@bengfort.com>
# Created:  Thu Nov 13 15:20:41 2014 -0500
#
# Copyright (C) 2014 District Data Labs
# For license information, see LICENSE.txt
#
# ID: emails_tests.py [5232e54] benjamin@bengfort.com $

"""
Testing for the utilities library in Tribe
"""

##########################################################################
## Imports
##########################################################################

import unittest

from tribe.emails import *

##########################################################################
## EmailAddress Tests
##########################################################################

class EmailAddressTests(unittest.TestCase):

    def test_full_email_parse(self):
        """
        Assert a full email is parsed correctly
        """
        data  = "Benjamin Bengfort <benjamin@bengfort.com>"
        email = EmailAddress(data)

        self.assertEqual(email.name, "Benjamin Bengfort")
        self.assertEqual(email.email, "benjamin@bengfort.com")
        self.assertEqual(email.domain, "bengfort.com")
        self.assertEqual(unicode(email), data)

    def test_partial_email_parse(self):
        """
        Assert a standalone email is parsed correctly
        """
        data = "benjamin@bengfort.com"
        email = EmailAddress(data)

        self.assertEqual(email.name, "")
        self.assertEqual(email.email, "benjamin@bengfort.com")
        self.assertEqual(email.domain, "bengfort.com")
        self.assertEqual(unicode(email), data)
