#include "stdio.h"

int main(int argc, char* argv[])
{
  const int NUMBER=50;
  int fd;
  int i;
  int fds[NUMBER];
  const char base[] = "file";
  char file [10];
  for (i = 0; i < NUMBER ; i ++){
    sprintf(file, "%s%d", base, i);
    if (0 >= (fd = creat(file))) {
      printf("error: unable to creat\n");
      return 1;
    }
    printf("Created file %s\n",file);
    if (0 > close(fd)) {
      printf("error: unable to close\n");
      return 1;
    }
  }
  for (i=0; i<NUMBER; i++){
    sprintf(file, "%s%d", base, i);	
    fds[i] = open(file);
    if (fds[i]==-1) {
      printf("Unable to open %s\n", file);
      return 1;
    }
    printf("Opened file %s\n",file);
  }
  for (i=0; i<NUMBER; i++){	
    sprintf(file, "%s%d", base, i);
    if (0 > close(fds[i])) {
      printf("error: unable to close\n");
      return 1;
    }
    printf("Closed file %s\n",file);

  }
  for (i=0; i<NUMBER; i++){	
    sprintf(file, "%s%d", base, i);	
    if (unlink(file) != 0) {
      printf("Unable to remove %s\n", file);
      return 1;
    }
    printf("Unlinked file %s\n",file);	
  }
  return 0;
}
