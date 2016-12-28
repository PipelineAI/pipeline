///////////////////////////////////////////////////////////////////////
//
// Recurrent neural network based statistical language modeling toolkit
// Version 0.2b
// (c) 2010 Tomas Mikolov (tmikolov@gmail.com)
//
///////////////////////////////////////////////////////////////////////

#ifndef _RNNLMLIB_H_
#define _RNNLMLIB_H_

#define MAX_STRING 100

typedef double real;

struct neuron {
    real ac;		//actual value stored in neuron
    real er;		//error value in neuron, used by learning algorithm
};
                
struct synapse {
    real weight;	//weight of synapse
};

struct vocab_word {
    int cn;
    char word[MAX_STRING];

    real prob;
    int class_index;
};



class CRnnLM{
protected:
    char train_file[MAX_STRING];
    char valid_file[MAX_STRING];
    char test_file[MAX_STRING];
    char rnnlm_file[MAX_STRING];
    char lmprob_file[MAX_STRING];
    
    int rand_seed;
    
    int debug_mode;
    
    int version;
    
    int use_lmprob;
    real lambda;
    
    real dynamic;
    
    real alpha;
    real starting_alpha;
    int alpha_divide;
    double logp, llogp;
    float min_improvement;
    int iter;
    int vocab_max_size;
    int vocab_size;
    int train_words;
    int train_cur_pos;
    int counter;
    
    int one_iter;
    int anti_k;
    
    real beta;
    
    int class_size;
    int **class_words;
    int *class_cn;
    int *class_max_cn;
    
    struct vocab_word *vocab;
    void sortVocab();
    int *vocab_hash;
    int vocab_hash_size;
    
    int layer0_size;
    int layer1_size;
    int layer2_size;
    
    int direct_size;
    
    int bptt;
    int bptt_block;
    int *bptt_history;
    neuron *bptt_hidden;
    struct synapse *bptt_syn0;
    
    int gen;
    
    struct neuron *neu0;		//neurons in input layer
    struct neuron *neu1;		//neurons in hidden layer
    struct neuron *neu2;		//neurons in output layer

    struct synapse *syn0;		//weights between input and hidden layer
    struct synapse *syn1;		//weights between hidden and output layer
    struct synapse *syn_d;		//direct parameters between input and output layer (similar to Maximum Entropy model parameters)
    struct synapse *syn_dc;		//direct parameters between input and class layer
    
    //backup used in training:
    struct neuron *neu0b;
    struct neuron *neu1b;
    struct neuron *neu2b;

    struct synapse *syn0b;
    struct synapse *syn1b;
    struct synapse *syn_db;
    struct synapse *syn_dcb;
    
    //backup used in n-bset rescoring:
    struct neuron *neu1b2;
    
    
public:

    int alpha_set, train_file_set;

    CRnnLM()		//constructor initializes variables
    {
	version=6;
	
	use_lmprob=0;
	lambda=0.75;
	dynamic=0;
    
	train_file[0]=0;
	valid_file[0]=0;
	test_file[0]=0;
	rnnlm_file[0]=0;
	
	alpha_set=0;
	train_file_set=0;
	
	alpha=0.1;
	beta=0.0000001;
	//beta=0.00000;
	alpha_divide=0;
	logp=0;
	llogp=-100000000;
	iter=0;
	
	min_improvement=1.003;
	
	train_words=0;
	train_cur_pos=0;
	vocab_max_size=100;
	vocab_size=0;
	vocab=(struct vocab_word *)malloc(vocab_max_size * sizeof(struct vocab_word));
	
	layer1_size=30;
	
	direct_size=0;
	
	bptt=0;
	bptt_block=10;
	bptt_history=NULL;
	bptt_hidden=NULL;
	bptt_syn0=NULL;
	
	gen=0;
	
	neu0=NULL;
	neu1=NULL;
	neu2=NULL;
	
	syn0=NULL;
	syn1=NULL;
	syn_d=NULL;
	syn_dc=NULL;
	//backup
	neu0b=NULL;
	neu1b=NULL;
	neu2b=NULL;
	
	neu1b2=NULL;
	
	syn0b=NULL;
	syn1b=NULL;
	//
	
	rand_seed=1;
	
	class_size=100;
	
	one_iter=0;
	
	debug_mode=1;
	srand(rand_seed);
	
	vocab_hash_size=1000000;
	vocab_hash=(int *)malloc(sizeof(int)*vocab_hash_size);
    }
    
    ~CRnnLM()		//destructor, deallocates memory
    {
	int i;
	
	if (neu0!=NULL) {
	    free(neu0);
	    free(neu1);
	    free(neu2);
	    
	    free(syn0);
	    free(syn1);
	    
	    if (syn_d!=NULL) free(syn_d);
	    if (syn_d!=NULL) free(syn_dc);
		
	    if (syn_d!=NULL) free(syn_db);
	    if (syn_d!=NULL) free(syn_dcb);

	    //
	    free(neu0b);
	    free(neu1b);
	    free(neu2b);
	    
	    free(neu1b2);
	    
	    free(syn0b);
	    free(syn1b);
	    //
	    
	    
	    for (i=0; i<class_size; i++) free(class_words[i]);
	    free(class_max_cn);
	    free(class_cn);
	    free(class_words);
	
	    free(vocab);
	    free(vocab_hash);
	    
	    //todo: free bptt variables too
	}
    }
    
    real random(real min, real max);

    void setTrainFile(char *str);
    void setValidFile(char *str);
    void setTestFile(char *str);
    void setRnnLMFile(char *str);
    void setLMProbFile(char *str) {strcpy(lmprob_file, str);}
    
    void setClassSize(int newSize) {class_size=newSize;}
    void setLambda(real newLambda) {lambda=newLambda;}
    void setDynamic(real newD) {dynamic=newD;}
    void setGen(real newGen) {gen=newGen;}
    
    void setLearningRate(real newAlpha) {alpha=newAlpha;}
    void setRegularization(real newBeta) {beta=newBeta;}
    void setMinImprovement(real newMinImprovement) {min_improvement=newMinImprovement;}
    void setHiddenLayerSize(int newsize) {layer1_size=newsize;}
    void setDirectSize(int newsize) {direct_size=newsize;}
    void setBPTT(int newval) {bptt=newval;}
    void setBPTTBlock(int newval) {bptt_block=newval;}
    void setRandSeed(int newSeed) {rand_seed=newSeed; srand(rand_seed);}
    void setDebugMode(int newDebug) {debug_mode=newDebug;}
    void setAntiKasparek(int newAnti) {anti_k=newAnti;}
    void setOneIter(int newOneIter) {one_iter=newOneIter;}
    
    int getWordHash(char *word);
    void readWord(char *word, FILE *fin);
    int searchVocab(char *word);
    int readWordIndex(FILE *fin);
    int addWordToVocab(char *word);
    void learnVocabFromTrainFile();		//train_file will be used to construct vocabulary
    
    void saveWeights();			//saves current weights and unit activations
    void restoreWeights();		//restores current weights and unit activations from backup copy
    //void saveWeights2();		//allows 2. copy to be stored, useful for dynamic rescoring of nbest lists
    //void restoreWeights2();		
    void saveContext();
    void restoreContext();
    void saveContext2();
    void restoreContext2();
    void initNet();
    void saveNet();
    void goToDelimiter(int delim, FILE *fi);
    void restoreNet();
    void netFlush();
    
    void computeNet(int last_word, int word);
    void learnNet(int last_word, int word);
    void copyHiddenLayerToInput();
    void trainNet();
    void useLMProb(int use) {use_lmprob=use;}
    void testNet();
    void testNbest();
    void testGen();
    
    void matrixXvector(struct neuron *dest, struct neuron *srcvec, struct synapse *srcmatrix, int matrix_width, int from, int to, int from2, int to2, int type);
};

#endif
