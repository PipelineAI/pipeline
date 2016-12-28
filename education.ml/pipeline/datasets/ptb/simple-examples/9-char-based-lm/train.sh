#!/bin/bash

rnnpath=../rnnlm-0.2b
trainfile=../data/ptb.char.train.txt
validfile=../data/ptb.char.valid.txt
testfile=../data/ptb.char.test.txt
rnnmodel=../models/ptb.char.model.hidden200.txt
temp=../temp

hidden_size=200
class_size=1		#classes have no sense when characters are used instead of words
bptt_steps=10		#we need to go back deeper in time

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

if [ -e $rnnmodel ]; then
    rm $rnnmodel
fi

if [ -e $rnnmodel.output.txt ]; then
    rm $rnnmodel.output.txt
fi

################################
# TRAINING OF RNNLM HAPPENS HERE
################################

time $rnnpath/rnnlm -train $trainfile -valid $validfile -rnnlm $rnnmodel -hidden $hidden_size -rand-seed 1 -debug 2 -class $class_size -bptt $bptt_steps -bptt-block 20

#################################################
# N-GRAM MODEL IS TRAINED HERE, USING SRILM TOOLS
#################################################

#we use -unk switch for srilm tools, as the data contain <unk> tokens for unknown words
#as we have rewritten all out of vocabulary words in the validation and test data to <unk> tokens, we do not need any special parameters to rnnlm tool
#you can check that the results are correct by replacing <unk> toknes by other (like <unknown>) and training closed vocabulary models

ngram-count -text $trainfile -order 9 -lm $temp/templm -ndiscount -gt3min 1 -gt4min 1 -gt5min 1 -gt6min 2 -gt7min 3 -gt8min 6 -gt9min 8
ngram -lm $temp/templm -order 9 -ppl $testfile -debug 2 > $temp/temp.ppl

$rnnpath/convert <$temp/temp.ppl >$temp/ngram.txt

##################################################
# MODELS ARE COMBINED HERE, PERPLEXITY IS REPORTED
##################################################

time $rnnpath/rnnlm -rnnlm $rnnmodel -test $testfile -lm-prob $temp/ngram.txt -lambda 0.2
