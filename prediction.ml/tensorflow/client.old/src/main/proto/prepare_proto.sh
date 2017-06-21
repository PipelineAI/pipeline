#!/bin/bash

set -x
set -e

# Clone TensorFlow project in proto directory

ls ./tensorflow/**/*[a-n] | xargs rm
ls ./tensorflow/**/*[p-z] | xargs rm
ls ./tensorflow/**/*[A-Z] | xargs rm
ls ./tensorflow/**/*[0-9] | xargs rm
ls ./tensorflow/**/*.go | xargs rm

find . -type d -empty -print | xargs rmdir
find . -type d -empty -print | xargs rmdir
find . -type d -empty -print | xargs rmdir
find . -type d -empty -print | xargs rmdir
find . -type d -empty -print | xargs rmdir

