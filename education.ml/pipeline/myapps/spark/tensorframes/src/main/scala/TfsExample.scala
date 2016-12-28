import org.tensorframes.{dsl => tf}
import org.tensorframes.dsl.Implicits._

import scala.collection.JavaConverters._

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

object fsExample {
  def main(args: Array[String]) = {
    val sparkConf: SparkConf = new SparkConf()
    val sc: SparkContext = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    import sqlContext.implicits._

    val df = sqlContext.createDataFrame(Seq(1.0->1.1, 2.0->2.2)).toDF("a", "b")

    // As in Python, scoping is recommended to prevent name collisions.
    val df2 = tf.withGraph {
      val a = df.block("a")
      // Unlike python, the scala syntax is more flexible:
      val out = a + 3.0 named "out"
      // The 'mapBlocks' method is added using implicits to dataframes.
      df.mapBlocks(out).select("a", "out")
    }

    // The transform is all lazy at this point, let's execute it with collect:
    // res0: Array[org.apache.spark.sql.Row] = Array([1.0,1.1,2.1], [2.0,2.2,4.2])
    df2.collect()
  }
}
