#!/bin/bash

# From this AWS article:
#   https://aws.amazon.com/articles/0155828273219400

sudo find / -name "authorized_keys" -exec rm -f {} \;
sudo find / -name "authorized_keys" -print -exec cat {} \;
sudo find /root/ /home/*/ -name .cvspass -exec rm –f {} \;
sudo find /root/ /home/*/ -name .git-credentials -exec rm –f {} \;
sudo find /root/.subversion/auth/svn.simple/ /home/*/.subversion/auth/svn.simple/ -exec rm –rf {} \;
sudo cat /etc/passwd /etc/shadow | grep -E '^[^:]*:[^:]{3,}' | cut -d: -f1
sudo rm /etc/syslog.conf
sudo service rsyslog restart
sudo find /root/.*history /home/*/.*history -exec rm -f {} \;
