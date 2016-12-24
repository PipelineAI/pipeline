#!/bin/bash

rnnpath=../rnnlm-0.2b
trainfile=../data/ptb.train.txt
validfile=../data/ptb.valid.txt
testfile=../data/ptb.test.txt
rnnmodel1=../models/ptb.model-1.hidden100.class100.txt
rnnmodel2=../models/ptb.model-2.hidden100.class100.txt
rnnmodel3=../models/ptb.model-3.hidden100.class100.txt
rnnmodel4=../models/ptb.model-4.hidden100.class100.txt
rnnmodel5=../models/ptb.model-5.hidden100.class100.txt
temp=../temp

if [[ ! -e $rnnmodel1 || ! -e $rnnmodel2 || ! -e $rnnmodel3 || ! -e $rnnmodel4 || ! -e $rnnmodel5 ]]; then
    echo "model files not found... run first train.sh"
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

##################################################
#COMPUTE PER-WORD PROBABILITIES GIVEN ALL 5 MODELS
##################################################

$rnnpath/rnnlm -rnnlm $rnnmodel1 -test $testfile -debug 2 > $temp/model1.output.txt
awk '{if ($2 ~ /^[0-9.]+$/) print $2;}' < $temp/model1.output.txt > $temp/model1.probs.txt

$rnnpath/rnnlm -rnnlm $rnnmodel2 -test $testfile -debug 2 > $temp/model2.output.txt
awk '{if ($2 ~ /^[0-9.]+$/) print $2;}' < $temp/model2.output.txt > $temp/model2.probs.txt

$rnnpath/rnnlm -rnnlm $rnnmodel3 -test $testfile -debug 2 > $temp/model3.output.txt
awk '{if ($2 ~ /^[0-9.]+$/) print $2;}' < $temp/model3.output.txt > $temp/model3.probs.txt

$rnnpath/rnnlm -rnnlm $rnnmodel4 -test $testfile -debug 2 > $temp/model4.output.txt
awk '{if ($2 ~ /^[0-9.]+$/) print $2;}' < $temp/model4.output.txt > $temp/model4.probs.txt

$rnnpath/rnnlm -rnnlm $rnnmodel5 -test $testfile -debug 2 > $temp/model5.output.txt
awk '{if ($2 ~ /^[0-9.]+$/) print $2;}' < $temp/model5.output.txt > $temp/model5.probs.txt

##################################################
# MODELS ARE COMBINED HERE, PERPLEXITY IS REPORTED
##################################################

#the 'prob' tool takes 3 arguments - two files with probabilities, and the weight (lambda) of the first model - the output is then linear combination of both
#in this example, all rnn models have the same weight
$rnnpath/prob $temp/model1.probs.txt $temp/model2.probs.txt 0.5 > $temp/model12.probs.txt
$rnnpath/prob $temp/model12.probs.txt $temp/model3.probs.txt 0.6666 > $temp/model123.probs.txt
$rnnpath/prob $temp/model123.probs.txt $temp/model4.probs.txt 0.75 > $temp/model1234.probs.txt
$rnnpath/prob $temp/model1234.probs.txt $temp/model5.probs.txt 0.8 > $temp/model12345.probs.txt

$rnnpath/prob $temp/model12345.probs.txt $temp/ngram.txt 0.6 > $temp/model12345+ngram.probs.txt

#with no arguemnts, 'prob' reads from stdin probabilities and probss perplexity and log likelihood
echo "Probability from model 1"
$rnnpath/prob < $temp/model1.probs.txt
echo "Probability from model 12"
$rnnpath/prob < $temp/model12.probs.txt
echo "Probability from model 123"
$rnnpath/prob < $temp/model123.probs.txt
echo "Probability from model 1234"
$rnnpath/prob < $temp/model1234.probs.txt
echo "Probability from model 12345"
$rnnpath/prob < $temp/model12345.probs.txt

echo "Probability from model 12345 + ngram"
$rnnpath/prob < $temp/model12345+ngram.probs.txt
