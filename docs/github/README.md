## Setup Github SSH Keys
### Start in the `<pipeline-root>` directory
```
cd <pipeline-root>
```

### Configure Git with your Email and Name
```
git config --global user.name "cfregly"

git config --global user.email "chris@fregly.com"
```

### Update the Remote Origin URL to use `ssh`
```
cd <pipeline-root>

git remote set-url origin git@github.com:PipelineAI/pipeline.git
```

### Setup Github SSH Keys 
* SSH Keys are provided by Github through your Github account
* Put your private `github_rsa` and public `github_rsa.pub` keys into `~/.ssh/`
* Modify permissions on this `github_rsa` file
```
chmod 600 ~/.ssh/github_rsa
```

### Start your SSH Authentication Agent
```
eval $(ssh-agent -s)
```

### Register SSH Keys Locally
* Run `ssh-add` and enter the passphrase used when creating the key pair
```
ssh-add ~/.ssh/github_rsa
Enter passphrase for ~/.ssh/github_rsa: <your-passphrase>
```

## Troubleshooting
* If you see the following error, start from [this](#start-your-ssh-authentication-agent) step.
```
Permission denied (publickey).
fatal: Could not read from remote repository.

Please make sure you have the correct access rights
and the repository exists.
```

* If you see the following error, make sure you ran modified the permissions on your `github_rsa` file above
```
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@         WARNING: UNPROTECTED PRIVATE KEY FILE!          @
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
Permissions 0644 for '/root/.ssh/github_rsa' are too open.
It is required that your private key files are NOT accessible by others.
This private key will be ignored.
```
