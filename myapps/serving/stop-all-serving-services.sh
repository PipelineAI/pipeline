#echo '...Stopping Redis...'
#redis-cli shutdown

echo '...Stopping Config Service...'
ps -aef | grep "config-service" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stopping Discovery Service...'
ps -aef | grep "discovery-service" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stopping Hystrix Dashboard...'
ps -aef | grep "hystrix-dashboard" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stopping Turbine...'
ps -aef | grep "turbine" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stopping Atlas...'
ps -aef | grep "atlas" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stopping TensorFlow Serving Inception Service (Sidecar for NetflixOSS)...'
ps -aef | grep "tensorflow-serving" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stopping TensorFlow Serving Inception Service...'
ps -aef | grep "inception_inference" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stopping TensorFlow Serving Inception Service Proxy...'
ps -aef | grep "service-proxy" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL
