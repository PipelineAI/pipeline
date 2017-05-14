Similar to `.gitignore`, `.pioignore` is a file that lists which files should not be 
uploaded to the PipelineIO servers.

## Initialization
Create the `.pioignore` file with a list of files and directories to ignore when uploading code and models to PipelineIO. 
```
.git
.DS_Store
.gitignore
*.tar.gz
```
__Note:  You do not need to add a trailing slash for directories.__

## Options

There are different ways to specify files or directories that you want to be ignored. Below is 
a list with examples:

```
# Ignore all .dat files in the whole project:
*.dat

# Ignore all .dat files in some_folder:
/some_folder/*.dat

# Ignore all .dat files in some_folder and its subfolders:
/some_folder/**/*.dat

# Ignore all files (and folders) named .DS_Store
.DS_Store

# Ignore a specific file named .DS_Store located in some_folder
/some_folder/.DS_Store

# Ignore all files named .DS_Store in some_folder and its subfolders
/some_folder/**/.DS_Store

# Ignore all files in some_folder
/some_folder

# Ignore all files in some_folder (works the same as the rule above)
/some_folder/
```

Minimizing the number of files to upload saves upload time and disk space used 
by your experiments.

{!contributing.md!}
