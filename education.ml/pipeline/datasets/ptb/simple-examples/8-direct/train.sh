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

#################################
# CHECK FOR 'rnnlm' AND 'convert'
#################################

if [ ! -e $rnnpath/rnnlm ]; then
    make clean -C $rnnpath
    make -C $rnnpath
fi

if [ ! -e $rnnpath/rnnlm ]; then
    echo "Cannot compile rnnlm tool";
    exit
fi

if [ ! -e $rnnpath/convert ]; then
    gcc $rnnpath/convert.c -O2 -o $rnnpath/convert
fi

#################################################
# IF MODEL FILE ALREADY EXISTS, IT WILL BE ERASED
#################################################

if [ -e $rnnmodel1 ]; then
    rm $rnnmodel1
fi

if [ -e $rnnmodel1.output.txt ]; then
    rm $rnnmodel1.output.txt
fi

if [ -e $rnnmodel2 ]; then
    rm $rnnmodel2
fi

if [ -e $rnnmodel2.output.txt ]; then
    rm $rnnmodel2.output.txt
fi

################################
# TRAINING OF RNNLM HAPPENS HERE
################################

time $rnnpath/rnnlm -train $trainfile -valid $validfile -rnnlm $rnnmodel1 -hidden $hidden_size -rand-seed 1 -debug 2 -class $class_size -bptt $bptt_steps -bptt-block 10
time $rnnpath/rnnlm -train $trainfile -valid $validfile -rnnlm $rnnmodel2 -hidden $hidden_size -rand-seed 1 -debug 2 -class $class_size -bptt $bptt_steps -bptt-block 10 -direct 5000

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
