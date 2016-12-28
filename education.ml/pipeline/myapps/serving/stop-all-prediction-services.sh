echo '...Stopping Prediction Service...'
ps -aef | grep "sbt-launch" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL
