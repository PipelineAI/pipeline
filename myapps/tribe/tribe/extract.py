# tribe.extract
# Extracts social network data from an email mbox
#
# Author:   Benjamin Bengfort <benjamin@bengfort.com>
# Created:  Wed Nov 12 21:19:51 2014 -0500
#
# Copyright (C) 2014 Bengfort.com
# For license information, see LICENSE.txt
#
# ID: extract.py [6c9398c] benjamin@bengfort.com $

"""
Extracts social network data from an email mbox and exports it as a GraphML
file format (which is suitable to use with Gephi, Neo4j etc)
"""

##########################################################################
## Imports
##########################################################################

import networkx as nx

from mailbox import mbox
from tribe.stats import FreqDist
from itertools import combinations
from email.utils import getaddresses
from tribe.utils import parse_date, strfnow
from tribe.emails import EmailMeta, EmailAddress

##########################################################################
## MBoxReader
##########################################################################

class MBoxReader(object):

    def __init__(self, path):
        self.path  = path
        self.mbox  = mbox(path)

    def __iter__(self):
        for msg in self.mbox:
            yield msg

    def header_analysis(self):
        """
        Performs an analysis of the frequency of headers in the Mbox
        """
        headers = FreqDist()
        for msg in self:
            headers['X-Tribe-Message-Count'] += 1
            for key in msg.keys():
                headers[key] += 1

        return headers

    def count(self):
        """
        Returns the number of emails in the MBox
        """
        return sum(1 for msg in self)

    def extract(self):
        """
        Extracts the meta data from the MBox
        """
        for msg in self:

            source = msg.get('From', '')
            if not source: continue

            tos = msg.get_all('To', []) + msg.get_all('Resent-To', [])
            ccs = msg.get_all('Cc', []) + msg.get_all('Resent-Cc', [])

            # construct data output
            email = EmailMeta(
                EmailAddress(source),
                [EmailAddress(to) for to in getaddresses(tos)],
                [EmailAddress(cc) for cc in getaddresses(ccs)],
                msg.get('Subject', '').strip() or None,
                parse_date(msg.get('Date', '').strip() or None),
            )

            yield email

    def extract_graph(self):
        """
        Extracts a Graph where the nodes are EmailAddress
        """
        links = FreqDist()
        for email in self.extract():
            people = [email.sender,]
            people.extend(email.recipients)
            people.extend(email.copied)

            people = filter(lambda p: p is not None, people)            # Filter out any None addresses
            people = set(addr.email for addr in people if addr.email)   # Obtain only unique people
            people = sorted(people)                                     # Sort lexicographically for combinations

            for combo in combinations(people, 2):
                links[combo] += 1

        G = nx.Graph(name="Email Network", mbox=self.path, extracted=strfnow())
        for link in links.keys():
            G.add_edge(*link, weight=links.freq(link))

        return G

if __name__ == '__main__':
    # Dump extracted email meta data to a pickle file for testing
    import pickle

    reader = MBoxReader("fixtures/benjamin@bengfort.com.mbox")
    emails = list(reader.extract())
    with open('fixtures/emails.pickle', 'w') as f:
        pickle.dump(emails, f, pickle.HIGHEST_PROTOCOL)


