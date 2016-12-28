from pyspark.mllib.common import _py2java

def toPMMLBytes(sc, data, pipelineModel):
	javaData = _py2java(sc, data)
	javaSchema = javaData.schema.__call__()

	javaStages = sc._jvm.java.util.ArrayList()
	for stage in pipelineModel.stages:
		javaStages.add(stage._java_obj)
	javaPipelineModel = sc._jvm.org.apache.spark.ml.PipelineModel(pipelineModel.uid, javaStages)

	return sc._jvm.org.jpmml.sparkml.ConverterUtil.toPMMLByteArray(javaSchema, javaPipelineModel)
