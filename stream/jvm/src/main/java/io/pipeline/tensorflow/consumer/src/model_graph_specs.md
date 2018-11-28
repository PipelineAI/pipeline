# Custom TensorFlow model training

## Goals

* Ability to scan through input model to grab appropriate tensors and operations
* Automatically add summary ops to desired nodes
* Automatically add histogram summaries to trainable variables
* Automatically add 'global_step' variable, if it doesn't already exist
* Automatic model export every \<user-specified\> steps

## Requirements

User provides:

* list of string placeholder op names
* list of string train op names
* list of desired additional outputs
* list of desired scalar summaries
