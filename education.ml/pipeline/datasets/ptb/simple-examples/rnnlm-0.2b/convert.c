// this simple program converts srilm output obtained in -debug 2 test mode to raw per-word probabilities

#include <stdio.h>

void goToDelimiter(int delim, FILE *fi)
{
    int ch=0;
    
    while (ch!=delim) {
        ch=fgetc(fi);
        if (feof(fi)) {
	    exit(1);
        }
    }
}

int main()
{
    int str[1000];
    float prob_other;
    
    goToDelimiter('\n', stdin);
    while (1) {
	fscanf(stdin, "%s", str);
	fscanf(stdin, "%s", str);
	fscanf(stdin, "%s", str);
	fscanf(stdin, "%s", str);
	fscanf(stdin, "%s", str);
	fscanf(stdin, "%s", str);
	if (strcmp(str, (char *)"=")) {
	    goToDelimiter('\n', stdin);
	    goToDelimiter('\n', stdin);
	    goToDelimiter('\n', stdin);
	    goToDelimiter('\n', stdin);
	}
	goToDelimiter(']', stdin);
	goToDelimiter(' ', stdin);
	fscanf(stdin, "%f", &prob_other);
	printf("%E\n", prob_other);
	goToDelimiter('\n', stdin);
    }
}
