import tensorflow as tf
import tensorframes as tfs
from pyspark.sql import Row

data = [Row(x=float(x)) for x in range(10)]
df = sqlContext.createDataFrame(data)
with tf.Graph().as_default() as g:
    # The TensorFlow placeholder that corresponds to column 'x'.
    # The shape of the placeholder is automatically inferred from the DataFrame.
    x = tfs.block(df, "x")
    # The output that adds 3 to x
    z = tf.add(x, 3, name='z')
    # The resulting dataframe
    df2 = tfs.map_blocks(z, df)

# The transform is lazy as for most DataFrame operations. This will trigger it:
df2.collect()
