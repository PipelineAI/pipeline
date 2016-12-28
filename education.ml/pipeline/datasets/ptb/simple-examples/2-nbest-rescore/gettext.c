#include <stdio.h>

int main()
{
    char str[1000];
    int ch;

    while (1) {
	fscanf(stdin, "%s", str);
	if (feof(stdin)) break;
	
	printf("%s ", str);
	
	fscanf(stdin, "%s", str);
	fscanf(stdin, "%s", str);
	fscanf(stdin, "%s", str);
	fscanf(stdin, "%s", str);
	fscanf(stdin, "%s", str);
	
	while (1) {
	    fscanf(stdin, "%s", str);
	    if (!strcmp(str, "</s>")) break;
	    printf("%s ", str);
	}
	printf("\n");
	
	ch=0;
	while (ch!='\n') ch=fgetc(stdin);
	ch=fgetc(stdin);
	if (feof(stdin)) break;
	ungetc(ch, stdin);	
    }

    return 0;
}
