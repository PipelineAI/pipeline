#include <stdio.h>
#include <string.h>

void main(int argc, char **argv)
{
    int a, b, i, cn, bestwcn, wcn, last_nbest, nbest;
    float bestscore, score, acscore, lmscore, rnnscore;
    float LM_SCALE, WI_PENALTY;
    char st[1000];
    char best[1000][1000];
    char curr[1000][1000];
    FILE *f1, *f2;
    
    if (argc<=2) {
	printf("Need 2 arguments - score file & nbest list\n");
	exit(1);
    }
    
    f1=fopen(argv[1], "rb");
    f2=fopen(argv[2], "rb");
    
    
    i=0;
    bestwcn=0;
    bestscore=-1000000;
    last_nbest=0;
    while (1) {
	fscanf (f1, "%f", &rnnscore);
	fscanf (f2, "%d", &nbest);
	
	if ((last_nbest!=nbest) || feof(f2)) {
	    for (a=0; a<bestwcn; a++) printf("%s ", best[a]);
	    printf("\n");
	
	    bestwcn=0;
	    bestscore=-1000000;
	}
	last_nbest=nbest;
	
	if (feof(f1)) break;
	
	fscanf (f2, "%f", &acscore);
	fscanf (f2, "%f", &lmscore);
	fscanf (f2, "%d", &wcn);
	
	wcn=0;
	fscanf (f2, "%s", st);
	fscanf (f2, "%s", st);
	
	while (1) {
	    fscanf (f2, "%s", curr[wcn]);
	    
	    if (!strcmp(curr[wcn], "</s>")) break;
	    
	    wcn++;
	}
	fscanf (f2, "%s", st);
	
	LM_SCALE=14;
	WI_PENALTY=0;
	score=acscore + rnnscore*LM_SCALE + wcn*WI_PENALTY/(2.718);
	
	//score=acscore + (rnnscore+lmscore)/2*LM_SCALE + wcn*WI_PENALTY/(2.718);	//optionally, the LM scores from lattices can be used like this
	
	if (score>bestscore) {
	    for (a=0; a<wcn; a++) strcpy(best[a], curr[a]);
	    bestwcn=wcn;
	    bestscore=score;
	}
    }
}
