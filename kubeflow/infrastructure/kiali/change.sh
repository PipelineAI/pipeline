#sed '/env:/!b;c      env:\n        - name: blah\n          value: blah' blah.yaml 
sed '/env:/!b;c      env:\n        - name: blah\n          value: blah' blah.yaml
