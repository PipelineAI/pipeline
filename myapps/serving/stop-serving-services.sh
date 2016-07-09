echo '...Starting Config Service...'
ps -aef | grep "config-service" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop Discovery Service...'
ps -aef | grep "discovery-service" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop Prediction Service...'
ps -aef | grep "sbt-launch" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop TensorFlow Serving Inception Service (Sidecar for NetflixOSS)...'
ps -aef | grep "tensorflow-serving" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop TensorFlow Serving Inception Service...'
ps -aef | grep "inception_inference" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop TensorFlow Serving Inception Service Proxy...'
ps -aef | grep "service-proxy" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop Hystrix Dashboard...'
ps -aef | grep "hystrix-dashboard" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop Turbine...'
ps -aef | grep "turbine" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stop Atlas...'
ps -aef | grep "atlas" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

