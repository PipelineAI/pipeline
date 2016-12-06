import tensorflow as tf
import tensorframes as tfs
from pyspark.sql import Row

# Build a DataFrame of vectors
data = [Row(y=[float(y), float(-y)]) for y in range(10)]
df = sqlContext.createDataFrame(data)
# Because the dataframe contains vectors, we need to analyze it first to find the
# dimensions of the vectors.
df2 = tfs.analyze(df)

# The information gathered by TF can be printed to check the content:
tfs.print_schema(df2)
# TF has inferred that y contains vectors of size 2
# root
#  |-- y: array (nullable = false) DoubleType[?,2]

# Let's use the analyzed dataframe to compute the sum and the elementwise minimum 
# of all the vectors:
# First, let's make a copy of the 'y' column. This will be very cheap in Spark 2.0+
df3 = df2.select(df2.y, df2.y.alias("z"))
with tf.Graph().as_default() as g:
    # The placeholders. Note the special name that end with '_input':
    y_input = tfs.block(df3, 'y', tf_name="y_input")
    z_input = tfs.block(df3, 'z', tf_name="z_input")
    y = tf.reduce_sum(y_input, [0], name='y')
    z = tf.reduce_min(z_input, [0], name='z')
    # The resulting dataframe
    (data_sum, data_min) = tfs.reduce_blocks([y, z], df3)

# The final results are numpy arrays:
print data_sum
# [45.0, -45.0]
print data_min
# [0.0, -9.0]
