#!/bin/sh                                                                               
                                                                                        
eval $(ssh-agent -s)                                                                    
                                                                                        
ssh-add ~/.ssh/github_rsa                                                               
                                                                                        
cd /root/prediction.ml                                                                  
                                                                                        
git remote set-url origin git@github.com:fluxcapacitor/pipeline.io/prediction.ml.git                
                                                                                        
git pull                                                                                
                                                                                        
git add pmml/data/census/census.pmml                                                    
git commit -m "updated pmml"                                                            
git push
