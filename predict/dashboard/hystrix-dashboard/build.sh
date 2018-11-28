#!/bin/bash

../gradlew --no-daemon clean buildAllProducts assemble -x test
