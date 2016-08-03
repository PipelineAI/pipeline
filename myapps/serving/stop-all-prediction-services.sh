echo '...Stopping Prediction Service...'
ps -aef | grep "sbt-launch" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stopping TensorFlow Serving Inception Service (Sidecar for NetflixOSS)...'
ps -aef | grep "tensorflow-serving" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stopping TensorFlow Serving Inception Service...'
ps -aef | grep "inception_inference" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL

echo '...Stopping TensorFlow Serving Inception Service Proxy...'
ps -aef | grep "service-proxy" | tr -s ' ' | cut -d ' ' -f2 | xargs kill -KILL
