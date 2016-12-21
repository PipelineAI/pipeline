#!/bin/bash

rnnpath=../rnnlm-0.2b
trainfile=../data/ptb.char.train.txt
validfile=../data/ptb.char.valid.txt
testfile=../data/ptb.char.test.txt
rnnmodel=../models/ptb.char.model.hidden200.txt
temp=../temp

if [ ! -e $rnnmodel ]; then
    echo "model file not found... run first train.sh"
    exit
fi

#################################################
# N-GRAM MODEL IS TRAINED HERE, USING SRILM TOOLS
#################################################

ngram-count -text $trainfile -order 9 -lm $temp/templm -ndiscount -gt3min 1 -gt4min 1 -gt5min 1 -gt6min 2 -gt7min 3 -gt8min 6 -gt9min 8
ngram -lm $temp/templm -order 9 -ppl $testfile -debug 2 > $temp/temp.ppl

$rnnpath/convert <$temp/temp.ppl >$temp/ngram.txt

##################################################
# MODELS ARE COMBINED HERE, PERPLEXITY IS REPORTED
##################################################

time $rnnpath/rnnlm -rnnlm $rnnmodel -test $testfile -lm-prob $temp/ngram.txt -lambda 0.2
