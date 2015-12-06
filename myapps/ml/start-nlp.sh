#!/bin/bash
#
#echo '...Not Yet Implemented...'
echo '...Building NLP App...'
sbt package 
echo '...Starting NLP App...'
#java -Xmx1g -jar $PIPELINE_HOME/myapps/ml/target/scala-2.10/ml_2.10-1.0.jar -cp $PIPELINE_HOME/myapps/ml/nlp/lib/stanford-corenlp-3.5.2-models.jar:$PIPELINE_HOME/myapps/ml/nlp/lib/stanford-corenlp-3.5.2.jar 2>&1 1>$PIPELINE_HOME/logs/ml/nlp-out.log &

#java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -cp $PIPELINE_HOME/myapps/ml/nlp/lib/stanford-corenlp-3.5.2-models.jar:$PIPELINE_HOME/myapps/ml/nlp/lib/stanford-corenlp-3.5.2.jar -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.ml.nlp.ProfileNLP"

echo '...Starting Spark CoreNLP App:  Analyze Item Descriptions...'
nohup spark-submit --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.ml.nlp.ItemDescriptions $PIPELINE_HOME/myapps/ml/target/scala-2.10/ml_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/ml/nlp-item-descriptions.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/ml/nlp-item-descriptions.log"'
