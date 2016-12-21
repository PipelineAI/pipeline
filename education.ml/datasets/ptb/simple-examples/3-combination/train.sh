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

hidden_size=100
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

##############################################
# IF MODELS ALREADY EXIST, THEY WILL BE ERASED
##############################################

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

if [ -e $rnnmodel3 ]; then
    rm $rnnmodel3
fi
if [ -e $rnnmodel3.output.txt ]; then
    rm $rnnmodel3.output.txt
fi

if [ -e $rnnmodel4 ]; then
    rm $rnnmodel4
fi
if [ -e $rnnmodel4.output.txt ]; then
    rm $rnnmodel4.output.txt
fi

if [ -e $rnnmodel5 ]; then
    rm $rnnmodel5
fi
if [ -e $rnnmodel5.output.txt ]; then
    rm $rnnmodel5.output.txt
fi

#######################################################################################################
# TRAINING OF RNNLMS HAPPENS HERE - DIFFERENT INITIALIZATION IS OBTAINED BY USING -rand-seed <n> SWITCH
#######################################################################################################

time $rnnpath/rnnlm -train $trainfile -valid $validfile -rnnlm $rnnmodel1 -hidden $hidden_size -rand-seed 1 -debug 2 -class $class_size -bptt $bptt_steps -bptt-block 10
time $rnnpath/rnnlm -train $trainfile -valid $validfile -rnnlm $rnnmodel2 -hidden $hidden_size -rand-seed 2 -debug 2 -class $class_size -bptt $bptt_steps -bptt-block 10
time $rnnpath/rnnlm -train $trainfile -valid $validfile -rnnlm $rnnmodel3 -hidden $hidden_size -rand-seed 3 -debug 2 -class $class_size -bptt $bptt_steps -bptt-block 10
time $rnnpath/rnnlm -train $trainfile -valid $validfile -rnnlm $rnnmodel4 -hidden $hidden_size -rand-seed 4 -debug 2 -class $class_size -bptt $bptt_steps -bptt-block 10
time $rnnpath/rnnlm -train $trainfile -valid $validfile -rnnlm $rnnmodel5 -hidden $hidden_size -rand-seed 5 -debug 2 -class $class_size -bptt $bptt_steps -bptt-block 10
