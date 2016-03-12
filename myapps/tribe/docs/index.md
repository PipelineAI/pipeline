# Tribe Documentation

Social networks are not new, even though websites like Facebook and Twitter might make you want to believe they are; and trust me- I’m not talking about Myspace! Social networks are extremely interesting models for human behavior, whose study dates back to the early twentieth century. However, because of those websites, data scientists have access to much more data than the anthropologists who studied the networks of tribes!

Because networks take a relationship-centered view of the world, the data structures that we will analyze model real world behaviors and community. Through a suite of algorithms derived from mathematical Graph theory we are able to compute and predict behavior of individuals and communities through these types of analyses. Clearly this has a number of practical applications from recommendation to law enforcement to election prediction, and more.

Tribe is a utility that will allow you to extract a network (a graph) from a communication network that we all use often - our email. Tribe is designed to read an email mbox (a native format for email in Python)and write the resulting graph to a GraphML file on disk. This utility is generally used for District Data Labs' Graph Analytics with Python and NetworkX course, but can be used for anyone interested in studying networks.

## Quick Start

1. Download your data. See [Extracting an MBox from Email](emails.md) for more information on how to accomplish this.

2. Install the tribe utility with `pip`:

        $ pip install tribe

3. If you would like to develop for tribe, please see the instructions in the README.

4. Extract a graph from your email MBox as follows:

        $ tribe-admin.py extract -w myemails.grpahml myemails.mbox

5. Be patient, this could take some time, on my Macbook Pro it took 12 minutes to perform the complete extraction on an MBox that was 7.5 GB.

You're now ready to get started analyzing your email network!
