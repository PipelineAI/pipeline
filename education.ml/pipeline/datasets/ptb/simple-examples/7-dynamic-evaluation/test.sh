#!/bin/bash

rnnpath=../rnnlm-0.2b
trainfile=../data/ptb.train.txt
validfile=../data/ptb.valid.txt
testfile=../data/ptb.test.txt
rnnmodel=../models/ptb.model.hidden100.class100.txt
temp=../temp

if [ ! -e $rnnmodel ]; then
    echo "model file not found... run first train.sh"
    exit
fi

#######################
# CHECK FOR 'prob' TOOL
#######################

if [ ! -e $rnnpath/prob ]; then
    gcc $rnnpath/prob.c -O2 -lm -o $rnnpath/prob
fi

#################################################
# N-GRAM MODEL IS TRAINED HERE, USING SRILM TOOLS
#################################################

ngram-count -text $trainfile -order 5 -lm $temp/templm -gt3min 1 -gt4min 1 -kndiscount -interpolate -unk
ngram -lm $temp/templm -order 5 -ppl $testfile -debug 2 > $temp/temp.ppl -unk

$rnnpath/convert <$temp/temp.ppl >$temp/ngram.txt

###############################
#COMPUTE PER-WORD PROBABILITIES
###############################

$rnnpath/rnnlm -rnnlm $rnnmodel -test $testfile -debug 2 > $temp/model.static.output.txt
awk '{if ($2 ~ /^[0-9.]+$/) print $2;}' < $temp/model.static.output.txt > $temp/model.static.probs.txt

$rnnpath/rnnlm -rnnlm $rnnmodel -test $testfile -debug 2 > $temp/model.dynamic.output.txt -dynamic 0.1
awk '{if ($2 ~ /^[0-9.]+$/) print $2;}' < $temp/model.dynamic.output.txt > $temp/model.dynamic.probs.txt

echo "Static model:"
time $rnnpath/rnnlm -rnnlm $rnnmodel -test $testfile -lm-prob $temp/ngram.txt -lambda 0.5
echo "Dynamic model:"
time $rnnpath/rnnlm -rnnlm $rnnmodel -test $testfile -lm-prob $temp/ngram.txt -lambda 0.5 -dynamic 0.1

##################################################
# MODELS ARE COMBINED HERE, PERPLEXITY IS REPORTED
##################################################

#the 'prob' tool takes 3 arguments - two files with probabilities, and the weight (lambda) of the first model - the output is then linear combination of both
#in this example, both rnn models have the same weight
$rnnpath/prob $temp/model.static.probs.txt $temp/model.dynamic.probs.txt 0.5 > $temp/model.static+dynamic.probs.txt

$rnnpath/prob $temp/model.static+dynamic.probs.txt $temp/ngram.txt 0.7 > $temp/model.static+dynamic+ngram.probs.txt

#with no arguemnts, 'prob' reads from stdin probabilities and probss perplexity and log likelihood
echo "Probability from static model"
$rnnpath/prob < $temp/model.static.probs.txt
echo "Probability from dynamic model"
$rnnpath/prob < $temp/model.dynamic.probs.txt
echo "Probability from combination of static and dynamic model"
$rnnpath/prob < $temp/model.static+dynamic.probs.txt
echo "Probability from static, dynamic and ngram models"
$rnnpath/prob < $temp/model.static+dynamic+ngram.probs.txt
