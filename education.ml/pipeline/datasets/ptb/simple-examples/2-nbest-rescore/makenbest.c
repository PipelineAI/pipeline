#include <stdio.h>

int main()
{
    char str[1000];
    FILE *fi;
    int a=0, ch;

    while (1) {
	fscanf(stdin, "%s", str);
	if (feof(stdin)) break;
	
	fi=fopen(str, "rb");
	while (1) {
	    printf("%d ", a);
	    ch=0;
	    while (ch!='\n') {
		ch=fgetc(fi);
		if (feof(fi)) break;
		fputc(ch, stdout);
	    }
	    ch=fgetc(fi);
	    if (feof(fi)) break;
	    ungetc(ch, fi);
	}
	a++;
	fclose(fi);
    }

    return 0;
}
