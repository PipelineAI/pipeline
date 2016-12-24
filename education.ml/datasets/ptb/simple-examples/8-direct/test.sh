#!/bin/bash

rnnpath=../rnnlm-0.2b
trainfile=../data/ptb.train.txt
validfile=../data/ptb.valid.txt
testfile=../data/ptb.test.txt
rnnmodel1=../models/ptb.model.hidden30.class100.txt
rnnmodel2=../models/ptb.model.hidden30.class100.direct5000.txt
temp=../temp

hidden_size=30
class_size=100
bptt_steps=4


if [[ ! -e $rnnmodel1 || ! -e $rnnmodel2 ]]; then
    echo "model files not found... run first train.sh"
    exit
fi

#################################################
# N-GRAM MODEL IS TRAINED HERE, USING SRILM TOOLS
#################################################

#we use -unk switch for srilm tools, as the data contain <unk> tokens for unknown words
#as we have rewritten all out of vocabulary words in the validation and test data to <unk> tokens, we do not need any special parameters to rnnlm tool
#you can check that the results are correct by replacing <unk> toknes by other (like <unknown>) and training closed vocabulary models

ngram-count -text $trainfile -order 5 -lm $temp/templm -gt3min 1 -gt4min 1 -kndiscount -interpolate -unk
ngram -lm $temp/templm -order 5 -ppl $testfile -debug 2 > $temp/temp.ppl -unk

$rnnpath/convert <$temp/temp.ppl >$temp/ngram.txt

##################################################
# MODELS ARE COMBINED HERE, PERPLEXITY IS REPORTED
##################################################

echo "Probability from rnn model with no direct connections:"
time $rnnpath/rnnlm -rnnlm $rnnmodel1 -test $testfile -lm-prob $temp/ngram.txt -lambda 0.4
echo "Probability from rnn model with direct connections between top 5000 words:"
time $rnnpath/rnnlm -rnnlm $rnnmodel2 -test $testfile -lm-prob $temp/ngram.txt -lambda 0.5
