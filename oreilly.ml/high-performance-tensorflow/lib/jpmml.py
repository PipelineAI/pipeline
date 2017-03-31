from pyspark.ml.common import _py2java

def toPMMLBytes(sc, data, pipelineModel):
	javaData = _py2java(sc, data)
	javaSchema = javaData.schema.__call__()
	javaPipelineModel = pipelineModel._to_java()
	return sc._jvm.org.jpmml.sparkml.ConverterUtil.toPMMLByteArray(javaSchema, javaPipelineModel)
