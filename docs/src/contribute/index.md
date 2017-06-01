# Contribute to PipelineIO
Want to join the PipelineIO Community?  

We will gladly accept pull requests for implementations of popular ML/AI models, fast prediction runtimes, and flexible experimentation frameworks.

The active list of outstanding PipelineIO issues and feature requests are [here](https://github.com/fluxcapacitor/pipeline/issues).

## Create a Pull Request
### Create a Branch for Your Changes
```
git branch <branch-name>
```

### Verify New Branch is Created
```
git branch

### EXPECTED OUTPUT ###
* master   <-- this is current branch
  <branch-name>
```

### Switch to Your New Branch
```
git checkout <branch-name>

### EXPECTED OUTPUT ###
Switched to branch '<branch-name>'
```

### Verify That You're on the Branch
```
git branch
  master
* <branch-name> <-- you are now on the new branch
```

### Make Your Branch Changes
```
...
```

### Verify Branch Changes
```
git status
```

### Prepare (Add) Your Branch Changes for Commit
```
git add ...
```

### Commit Your Branch Changes Locally
```
git commit -m "<descriptive-message>"
```

### Push Your Branch Changes Remotely
```
git push -u origin <branch-name>

### EXPECTED OUTPUT ###
# Counting objects: 8, done.
# Delta compression using up to 8 threads.
# Compressing objects: 100% (6/6), done.
# Writing objects: 100% (8/8), 684 bytes | 0 bytes/s, done.
# Total 8 (delta 2), reused 0 (delta 0)
# remote: Resolving deltas: 100% (2/2), completed with 1 local objects.
# To github.com:fluxcapacitor/pipeline.git
# * [new branch]      <branch-name> -> <branch-name>
#Branch <branch-name> set up to track remote branch <branch-name> from origin.
```
Note:  If you see an error related to `Permission denied (publickey)` or `Please make sure you have the correct access rights`, follow the steps detailed [here](Github-Notes#start-your-ssh-authentication-agent).

### Submit a Pull Request with Github Server UI
* Compare your `<branch-name>` to the `master` branch on Github Server

### Fix the Pull Request if Needed based on Comments

### Wait for Committer to Merge Pull Request (within Github UI)

### Delete the Branch (from both Local and Remote Github)
Go Back to `master` Branch
```
git checkout master
```

Delete Remote Branch
```
git push origin --delete <branch-name>
```

Delete Local Branch
```
git branch -d <branch-name>
```
 
## License and Datasets 
* Your work should be open source under a liberal license (ie. Apache2, MIT)
* Please use **public** training and validation datasets so that others can easily replicate your work.
