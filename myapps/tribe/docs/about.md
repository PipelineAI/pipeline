# About

Tribe is a utility that will allow you to extract a network (a graph) from a communication network that we all use often - our email. Tribe is designed to read an email mbox (a native format for email in Python)and write the resulting graph to a GraphML file on disk. This utility is generally used for District Data Labs' Graph Analytics with Python and NetworkX course, but can be used for anyone interested in studying networks.

## Contributing

Tribe is open source, and I'd love your help. If you would like to contribute, you can do so in the following ways:

1. Add issues or bugs to the bug tracker: [https://github.com/DistrictDataLabs/tribe/issues](https://github.com/DistrictDataLabs/tribe/issues)
2. Work on a card on the dev board: [https://waffle.io/DistrictDataLabs/tribe](https://waffle.io/DistrictDataLabs/tribe)
3. Create a pull request in Github: [https://github.com/DistrictDataLabs/tribe/pulls](https://github.com/DistrictDataLabs/tribe/pulls)

Note that labels in the Github issues are defined in the blog post: [How we use labels on GitHub Issues at Mediocre Laboratories](https://mediocre.com/forum/topics/how-we-use-labels-on-github-issues-at-mediocre-laboratories).

If you are a member of the District Data Labs Faculty group, you have direct access to the repository, which is set up in a typical production/release/development cycle as described in _[A Successful Git Branching Model](http://nvie.com/posts/a-successful-git-branching-model/)_. A typical workflow is as follows:

1. Select a card from the [dev board](https://waffle.io/DistrictDataLabs/tribe) - preferably one that is "ready" then move it to "in-progress".

2. Create a branch off of develop called "feature-[feature name]", work and commit into that branch.

        ~$ git checkout -b feature-myfeature develop

3. Once you are done working (and everything is tested) merge your feature into develop.

        ~$ git checkout develop
        ~$ git merge --no-ff feature-myfeature
        ~$ git branch -d feature-myfeature
        ~$ git push origin develop

4. Repeat. Releases will be routinely pushed into master via release branches, then deployed to the server.

## Contributors

Thank you for all your help contributing to make Tribe a great project!

### Maintainers

- Benjamin Bengfort: [@bbengfort](https://github.com/bbengfort/)

### Contributors

- Your name welcome here!

## Changelog

The release versions that are sent to the Python package index (PyPI) are also tagged in Github. You can see the tags through the Github web application and download the tarball of the version you'd like.

The versioning uses a three part version system, "a.b.c" - "a" represents a major release that may not be backwards compatible. "b" is incremented on minor releases that may contain extra features, but are backwards compatible. "c" releases are bug fixes or other micro changes that developers should feel free to immediately update to.

### Version 1.1.2

* **tag**: [v1.1.2](https://github.com/DistrictDataLabs/tribe/releases/tag/v1.1.2)
* **release**:  Thursday, November 20, 2014
* **deployment**: Friday, March 11, 2016
* **commit**: [69fe3c6](https://github.com/DistrictDataLabs/tribe/commit/69fe3c69130899479be2e33f73872d6cfedd4659)

This is the initial release of Tribe that has been used for teaching since the first SNA workshop in 2014. This version was cleaned up a bit, with extra dependency removal and better organization. This is also the first version that was deployed to PyPI.
