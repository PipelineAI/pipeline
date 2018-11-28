java -Djava.security.egd=file:/dev/./urandom \
      -jar ./lib/sbt-launch-1.0.2.jar \
      "runMain ai.pipeline.predict.jvm.PredictionServiceMain"
