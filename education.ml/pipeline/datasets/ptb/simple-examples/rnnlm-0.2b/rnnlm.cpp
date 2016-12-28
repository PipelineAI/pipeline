///////////////////////////////////////////////////////////////////////
//
// Recurrent neural network based statistical language modeling toolkit
// Version 0.2b
// (c) 2010 Tomas Mikolov (tmikolov@gmail.com)
//
///////////////////////////////////////////////////////////////////////


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <fstream>
#include <iostream>
#include "rnnlmlib.h"

using namespace std;

#define MAX_STRING 100

int argPos(char *str, int argc, char **argv)
{
    int a;
    
    for (a=1; a<argc; a++) if (!strcmp(str, argv[a])) return a;
    
    return -1;
}

int main(int argc, char **argv)
{
    int i;
    
    int debug_mode=1;
    
    int train_mode=0;
    int valid_data_set=0;
    int test_data_set=0;
    int rnnlm_file_set=0;
    
    int alpha_set=0, train_file_set=0;
    
    int class_size=100;
    float lambda=0.75;
    float dynamic=0;
    float starting_alpha=0.1;
    float regularization=0.0000001;
    float min_improvement=1.003;
    int hidden_size=30;
    int direct=0;
    int bptt=0;
    int bptt_block=10;
    int gen=0;
    int rnnlm_exist=0;
    int use_lmprob=0;
    int rand_seed=1;
    int nbest=0;
    int one_iter=0;
    int anti_k=0;
    
    char train_file[MAX_STRING];
    char valid_file[MAX_STRING];
    char test_file[MAX_STRING];
    char rnnlm_file[MAX_STRING];
    char lmprob_file[MAX_STRING];
    
    FILE *f;
    
    if (argc==1) {
    	//printf("Help\n");

    	printf("Recurrent neural network based language modeling toolkit v 0.2b\n\n");

    	printf("Options:\n");

    	//
    	printf("Parameters for training phase:\n");
    	
    	printf("\t-train <file>\n");
        printf("\t\tUse text data from <file> to train rnnlm model\n");
        
        printf("\t-class <int>\n");
        printf("\t\tWill use specified amount of classes to decompose vocabulary; default is 100\n");

    	printf("\t-rnnlm <file>\n");
        printf("\t\tUse <file> to store rnnlm model\n");

    	printf("\t-valid <file>\n");
    	printf("\t\tUse <file> as validation data\n");

    	printf("\t-alpha <float>\n");
    	printf("\t\tSet starting learning rate; default is 0.1\n");
    	
    	printf("\t-beta <float>\n");
    	printf("\t\tSet L2 regularization parameter; default is 1e-7\n");

    	printf("\t-hidden <int>\n");
    	printf("\t\tSet size of hidden layer; default is 30\n");
    	
    	printf("\t-direct <int>\n");
    	printf("\t\tWill use direct connections between input and output layers for most frequent <int> words; default is 0\n");
    	
    	printf("\t-bptt <int>\n");
    	printf("\t\tSet amount of steps to propagate error back in time; default is 0 (equal to simple RNN)\n");
    	
    	printf("\t-bptt-block <int>\n");
    	printf("\t\tSpecifies amount of time steps after which the error is backpropagated through time in block mode (default 10, update at each time step = 1)\n");
    	
    	printf("\t-one-iter\n");
    	printf("\t\tWill cause training to perform exactly one iteration over training data (useful for adapting final models on different data etc.)\n");
    	
    	printf("\t-anti-kasparek <int>\n");
    	printf("\t\tModel will be saved during training after processing specified amount of words\n");
    	
    	printf("\t-min-improvement <float>\n");
    	printf("\t\tSet minimal relative entropy improvement for training convergence; default is 1.003\n");

    	//

    	printf("Parameters for testing phase:\n");

    	printf("\t-rnnlm <file>\n");
    	printf("\t\tRead rnnlm model from <file>\n");

    	printf("\t-test <file>\n");
    	printf("\t\tUse <file> as test data to report perplexity\n");

    	printf("\t-lm-prob\n");
    	printf("\t\tUse other LM probabilities for linear interpolation with rnnlm model; see examples/*** \n");	//***

    	printf("\t-lambda <float>\n");
    	printf("\t\tSet parameter for linear interpolation of rnnlm and other lm; default weight of rnnlm is 0.75\n");
    	
    	printf("\t-dynamic <float>\n");
    	printf("\t\tSet learning rate for dynamic model updates during testing phase; default is 0 (static model)\n");
    	
    	//

    	printf("Parameters for data generation:\n");
    	
    	printf("\t-gen <int>\n");
    	printf("\t\tGenerate specified amount of words given distribution from current model\n");

    	printf("\nExamples:\n");
    	printf("rnnlm -train train -rnnlm model -valid valid -hidden 50\n");
    	printf("rnnlm -rnnlm model -test test\n");
    	printf("\n");

    	return 0;	//***
    }

    
    //set debug mode
    i=argPos((char *)"-debug", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: debug mode not specified!\n");
            return 0;
        }

        debug_mode=atoi(argv[i+1]);

	if (debug_mode>0)
        printf("debug mode: %d\n", debug_mode);
    }

    
    //search for train file
    i=argPos((char *)"-train", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: training data file not specified!\n");
            return 0;
        }

        strcpy(train_file, argv[i+1]);

	if (debug_mode>0)
        printf("train file: %s\n", train_file);

        f=fopen(train_file, "rb");
        if (f==NULL) {
            printf("ERROR: training data file not found!\n");
            return 0;
        }

        train_mode=1;
        
        train_file_set=1;
    }
    
    
    //set one-iter
    i=argPos((char *)"-one-iter", argc, argv);
    if (i>0) {
        one_iter=1;

        if (debug_mode>0)
        printf("Training for one iteration\n");
    }
    
    
    //search for validation file
    i=argPos((char *)"-valid", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: validation data file not specified!\n");
            return 0;
        }

        strcpy(valid_file, argv[i+1]);

        if (debug_mode>0)
        printf("valid file: %s\n", valid_file);

        f=fopen(valid_file, "rb");
        if (f==NULL) {
            printf("ERROR: validation data file not found!\n");
            return 0;
        }

        valid_data_set=1;
    }
    
    if (train_mode && !valid_data_set) {
	if (one_iter==0) {
	    printf("ERROR: validation data file must be specified for training!\n");
    	    return 0;
    	}
    }
    
    
    //search for test file
    i=argPos((char *)"-test", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: test data file not specified!\n");
            return 0;
        }

        strcpy(test_file, argv[i+1]);

        if (debug_mode>0)
        printf("test file: %s\n", test_file);

        f=fopen(test_file, "rb");
        if (f==NULL) {
            printf("ERROR: test data file not found!\n");
            return 0;
        }

        test_data_set=1;
    }
    
    
    //set class size parameter
    i=argPos((char *)"-class", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: amount of classes not specified!\n");
            return 0;
        }

        class_size=atoi(argv[i+1]);

	if (debug_mode>0)
        printf("class size: %d\n", class_size);
    }
    
    
    //set lambda
    i=argPos((char *)"-lambda", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: lambda not specified!\n");
            return 0;
        }

        lambda=atof(argv[i+1]);

        if (debug_mode>0)
        printf("Lambda (interpolation coefficient between rnnlm and other lm): %f\n", lambda);
    }
    
    
    //set dynamic
    i=argPos((char *)"-dynamic", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: dynamic learning rate not specified!\n");
            return 0;
        }

        dynamic=atof(argv[i+1]);

        if (debug_mode>0)
        printf("Dynamic learning rate: %f\n", dynamic);
    }
    
    
    //set gen
    i=argPos((char *)"-gen", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: gen parameter not specified!\n");
            return 0;
        }

        gen=atoi(argv[i+1]);

        if (debug_mode>0)
        printf("Generating # words: %d\n", gen);
    }
    
    
    //set learning rate
    i=argPos((char *)"-alpha", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: alpha not specified!\n");
            return 0;
        }

        starting_alpha=atof(argv[i+1]);

        if (debug_mode>0)
        printf("Starting learning rate: %f\n", starting_alpha);
        alpha_set=1;
    }
    
    
    //set regularization
    i=argPos((char *)"-beta", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: beta not specified!\n");
            return 0;
        }

        regularization=atof(argv[i+1]);

        if (debug_mode>0)
        printf("Regularization: %f\n", regularization);
    }
    
    
    //set min improvement
    i=argPos((char *)"-min-improvement", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: minimal improvement value not specified!\n");
            return 0;
        }

        min_improvement=atof(argv[i+1]);

        if (debug_mode>0)
        printf("Min improvement: %f\n", min_improvement);
    }
    
    
    //set anti kasparek
    i=argPos((char *)"-anti-kasparek", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: anti-kasparek parameter not set!\n");
            return 0;
        }

        anti_k=atoi(argv[i+1]);
        
        if ((anti_k!=0) && (anti_k<10000)) anti_k=10000;

        if (debug_mode>0)
        printf("Model will be saved after each # words: %d\n", anti_k);
    }
    

    //set hidden layer size
    i=argPos((char *)"-hidden", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: hidden layer size not specified!\n");
            return 0;
        }

        hidden_size=atoi(argv[i+1]);

        if (debug_mode>0)
        printf("Hidden layer size: %d\n", hidden_size);
    }
    
    
    //set direct connections
    i=argPos((char *)"-direct", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: direct connections not specified!\n");
            return 0;
        }

        direct=atoi(argv[i+1]);

        if (debug_mode>0)
        printf("Direct connections: %d\n", direct);
    }
    
    
    //set bptt
    i=argPos((char *)"-bptt", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: bptt value not specified!\n");
            return 0;
        }

        bptt=atoi(argv[i+1]);
        bptt++;
        if (bptt<1) bptt=0;

        if (debug_mode>0)
        printf("BPTT: %d\n", bptt-1);
    }
    
    
    //set bptt block
    i=argPos((char *)"-bptt-block", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: bptt block value not specified!\n");
            return 0;
        }

        bptt_block=atoi(argv[i+1]);
        if (bptt_block<1) bptt_block=1;

        if (debug_mode>0)
        printf("BPTT block: %d\n", bptt_block);
    }
    
        
    //set random seed
    i=argPos((char *)"-rand-seed", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: Random seed variable not specified!\n");
            return 0;
        }

        rand_seed=atoi(argv[i+1]);

        if (debug_mode>0)
        printf("Rand seed: %d\n", rand_seed);
    }
    
    
    //set nbest rescoring mode
    i=argPos((char *)"-nbest", argc, argv);
    if (i>0) {
	nbest=1;
        if (debug_mode>0)
        printf("Processing test data as list of nbests\n");
    }
    
    
    //use other lm
    i=argPos((char *)"-lm-prob", argc, argv);
    if (i>0) {
	if (i+1==argc) {
            printf("ERROR: other lm file not specified!\n");
            return 0;
        }

        strcpy(lmprob_file, argv[i+1]);

        if (debug_mode>0)
        printf("other lm probabilities specified in: %s\n", lmprob_file);

        f=fopen(lmprob_file, "rb");
        if (f==NULL) {
            printf("ERROR: other lm file not found!\n");
            return 0;
        }
    
        use_lmprob=1;
    }
    
    
    //search for rnnlm file
    i=argPos((char *)"-rnnlm", argc, argv);
    if (i>0) {
        if (i+1==argc) {
            printf("ERROR: model file not specified!\n");
            return 0;
        }

        strcpy(rnnlm_file, argv[i+1]);

        if (debug_mode>0)
        printf("rnnlm file: %s\n", rnnlm_file);

        f=fopen(rnnlm_file, "rb");
        if (f!=NULL) {
            rnnlm_exist=1;
        }

        rnnlm_file_set=1;
    }
    if (train_mode && !rnnlm_file_set) {
    	printf("ERROR: rnnlm file must be specified for training!\n");
    	return 0;
    }
    if (test_data_set && !rnnlm_file_set) {
    	printf("ERROR: rnnlm file must be specified for testing!\n");
    	return 0;
    }
    if (!test_data_set && !train_mode && gen==0) {
    	printf("ERROR: training or testing must be specified!\n");
    	return 0;
    }
    if ((gen>0) && !rnnlm_file_set) {
	printf("ERROR: rnnlm file must be specified to generate words!\n");
    	return 0;
    }
    
    
    srand(1);

    if (train_mode) {
    	CRnnLM model1;

    	model1.setTrainFile(train_file);
    	model1.setRnnLMFile(rnnlm_file);
    	
    	model1.setOneIter(one_iter);
    	if (one_iter==0) model1.setValidFile(valid_file);

	model1.setClassSize(class_size);
    	model1.setLearningRate(starting_alpha);
    	model1.setRegularization(regularization);
    	model1.setMinImprovement(min_improvement);
    	model1.setHiddenLayerSize(hidden_size);
    	model1.setDirectSize(direct);
    	model1.setBPTT(bptt);
    	model1.setBPTTBlock(bptt_block);
    	model1.setRandSeed(rand_seed);
    	model1.setDebugMode(debug_mode);
    	model1.setAntiKasparek(anti_k);
    	
    	model1.alpha_set=alpha_set;
    	model1.train_file_set=train_file_set;

    	model1.trainNet();
    }
    
    if (test_data_set && rnnlm_file_set) {
        CRnnLM model1;

        model1.setLambda(lambda);
        model1.setRegularization(regularization);
        model1.setDynamic(dynamic);
        model1.setTestFile(test_file);
        model1.setRnnLMFile(rnnlm_file);
        model1.setRandSeed(rand_seed);
        model1.useLMProb(use_lmprob);
        if (use_lmprob) model1.setLMProbFile(lmprob_file);
        model1.setDebugMode(debug_mode);

	if (nbest==0) model1.testNet();
	else model1.testNbest();
    }
    
    if (gen>0) {
	CRnnLM model1;
	
	model1.setRnnLMFile(rnnlm_file);
	model1.setDebugMode(debug_mode);
	model1.setRandSeed(rand_seed);
	model1.setGen(gen);
    
	model1.testGen();
    }
    
    
    return 0;
}
