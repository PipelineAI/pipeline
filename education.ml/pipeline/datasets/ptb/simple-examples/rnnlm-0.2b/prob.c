#include <stdio.h>
#include <stdlib.h>
#include <math.h>

int main(int argc, char *argv[])
{
    FILE *f1, *f2, *f3;
    int w;
    double f, g, mix, logp;

    if (argc==4) {
    
	f1=fopen(argv[1], "rb");
	f2=fopen(argv[2], "rb");
	f3=stdout;

	mix=atof(argv[3]);

	while (1) {
	    fscanf(f1, "%lf", &f);

	    if (feof(f1)) break;
	    fscanf(f2, "%lf", &g);
	    
	    f=f*mix+g*(1-mix);
	
	    fprintf(f3, "%E\n", f);
	}
    
	fclose(f1);
	fclose(f2);
	fclose(f3);

    } else {
	w=0;
	logp=0;
	while (1) {
    	    fscanf(stdin, "%lf", &f);
    	    if (feof(stdin)) break;
    	    if (f>0) {
        	logp+=log10(f);
        	w++;
	    }
	}

	printf("Log P: %lf\n", logp);
	printf("PPL: %lf\n", 1/exp10(logp/w));
    }

    return 0;
}
