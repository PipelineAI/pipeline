echo '...Starting Config Service...'
ps -aef | grep "config-service" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop Discovery Service...'
ps -aef | grep "discovery-service" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop Prediction Service...'
ps -aef | grep "sbt-launch" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop Hystrix Dashboard...'
ps -aef | grep "hystrix-dashboard" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop Atlas...'
ps -aef | grep "atlas" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

