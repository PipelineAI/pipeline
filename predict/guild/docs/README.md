# Guild AI Documentation

## Setup on Debian based Linux systems

- Install ruby-bundler and ruby-dev
- Run `bundle install` in `www-guild-net` root
- Serve the project by running `make serve`

## To Do

### Problems installing psutil on Mac OSX

For some reason pip installed to /Library/Python/... - this may be
because it used the built-in Python by default rather than
Homebrew. I'm not sure how TensorFlow was installed properly though.

This command will install to the Homebrew site-packages location:

```
sudo pip install --upgrade --target /usr/local/lib/python2.7/site-packages psutil
```
