import glob
import os
import sys

# Setup SPARK_HOME
spark_home = os.getenv('SPARK_HOME', None)
if not spark_home:
    raise ValueError('SPARK_HOME environment variable is not set')

# System sys.path
sys.path.insert(0, os.path.join(spark_home, 'python'))
for lib in glob.glob(os.path.join(spark_home, 'python/lib/py4j-*-src.zip')):
    sys.path.insert(0, lib)

os.environ['PYSPARK_SUBMIT_ARGS']='--master %s %s pyspark-shell' % (os.getenv('SPARK_MASTER'), os.getenv('SPARK_SUBMIT_ARGS'))

execfile(os.path.join(spark_home, 'python/pyspark/shell.py'))
