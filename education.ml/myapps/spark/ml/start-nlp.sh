#!/bin/bash
#
echo '...Starting NLP App...'
#java -Xmx1g -jar $PIPELINE_HOME/myapps/ml/target/scala-2.10/ml_2.10-1.0.jar -cp $PIPELINE_HOME/myapps/ml/nlp/lib/stanford-corenlp-3.5.2-models.jar:$PIPELINE_HOME/myapps/ml/nlp/lib/stanford-corenlp-3.5.2.jar 2>&1 1>$PIPELINE_HOME/logs/ml/nlp-item-descriptions.log &

#java -Xmx13G -XX:+PreserveFramePointer -XX:-Inline -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=512m -cp $PIPELINE_HOME/myapps/ml/nlp/lib/stanford-corenlp-3.5.2-models.jar:$PIPELINE_HOME/myapps/ml/nlp/lib/stanford-corenlp-3.5.2.jar -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.ml.nlp.ProfileNLP"

echo '...Starting Spark CoreNLP App:  Analyze Item Descriptions with Spark and CoreNLP...'
#nohup
spark-submit --repositories $SPARK_REPOSITORIES --jars $SPARK_SUBMIT_JARS,/root/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0-models.jar,/root/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0.jar,/root/stanford-corenlp-full-2015-12-09/jollyday.jar,/root/pipeline/myapps/ml/lib/spark-corenlp_2.10-0.1.jar --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.ml.nlp.ItemDescriptionsDF $PIPELINE_HOME/myapps/ml/target/scala-2.10/ml_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/ml/nlp-item-descriptions-df.log
# &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/ml/nlp-item-descriptions-df.log"'
