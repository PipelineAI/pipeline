#!/bin/bash

sbt -mem 2000 package

java -Xshare:dump
