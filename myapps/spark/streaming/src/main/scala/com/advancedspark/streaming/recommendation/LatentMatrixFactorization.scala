package com.advancedspark.streaming.recommendation

//////////////////////////////////////////////////////////////////////
// This code has been adapted from the following source:
//   https://github.com/brkyvz/streaming-matrix-factorization
// Thanks, Burak!
//////////////////////////////////////////////////////////////////////

import java.io.PrintWriter
import org.apache.spark.{SparkContext, Logging}
import org.apache.spark.api.java.JavaRDD
import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.api.java.JavaDStream
import org.apache.spark.streaming.dstream.DStream

import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD._

import scala.reflect.ClassTag

import com.advancedspark.streaming.recommendation.optimization.MFGradientDescent

/**
 * Trains a Matrix Factorization Model for Recommendation Systems. The model consists of
 * user factors (User Matrix, `U`), product factors (Product Matrix, `P^T^`),
 * user biases (user bias vector, `bu`), product biases (product bias vector, `bp`) and
 * the global bias (global average, `mu`).
 *
 * Trained on a static RDD, but can make predictions on a DStream or RDD.
 *
 * @param params Parameters for training
 */
class LatentMatrixFactorization (params: LatentMatrixFactorizationParams) extends Logging {

  def this() = this(new LatentMatrixFactorizationParams)

  val optimizer = new MFGradientDescent(params)

  var model: Option[LatentMatrixFactorizationModel] = None

  def trainOn(ratings: RDD[Rating[Long]]): LatentMatrixFactorizationModel = {
    if (!model.isEmpty) {
      val (initialModel, numExamples) =
        LatentMatrixFactorizationModel.initialize(ratings, params, model, isStreaming = false)
      model = Some(optimizer.train(ratings, initialModel, numExamples))
    }
    model.get
  }

  /** Java-friendly version of `trainOn`. */
  def trainOn(ratings: JavaRDD[Rating[Long]]): Unit = trainOn(ratings.rdd)

  /**
   * Use the model to make predictions on batches of data from a DStream
   *
   * @param data DStream containing (user, product) tuples
   * @return DStream containing rating predictions
   */
  def predictOn(data: DStream[(Long, Long)]): DStream[Rating[Long]] = {
    if (model.isEmpty) {
      throw new IllegalStateException("Model must be trained before starting prediction.")
    }
    data.transform((rdd, time) => model.get.predict(rdd))
  }

  /** Java-friendly version of `predictOn`. */
  def predictOn(data: JavaDStream[(Long, Long)]): JavaDStream[Rating[java.lang.Long]] = {
    JavaDStream.fromDStream(predictOn(data.dstream).asInstanceOf[DStream[Rating[java.lang.Long]]])
  }

  /**
   * Use the model to make predictions on the values of a DStream and carry over its keys.
   * @param data DStream containing (user, product) tuples
   * @tparam K key type
   * @return DStream containing the input keys and the rating predictions as values
   */
  def predictOnValues[K: ClassTag](data: DStream[(K, (Long, Long))]): DStream[(K, Rating[Long])] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model must be initialized before starting prediction")
    }
    data.transform((rdd, time) => rdd.keys.zip(model.get.predict(rdd.values)))
  }

  /**
    * @param model
    * @param path
    */
  def saveModel(model: LatentMatrixFactorizationModel, path: String) = {
    require(model != null)
    //val pt = new Path(path)
    //pt.deleteIfExists()
    model.productFeatures.saveAsObjectFile(path + "/" + "itemFeature")
    model.userFeatures.saveAsObjectFile(path + "/" + "userFeature")
    val pw = new PrintWriter(path + "/" + "other")
    pw.println(model.globalBias)
    pw.println(model.maxRating)
    pw.println(model.minRating)
    pw.print(model.rank)
    pw.close()
  }

  /**
    * @param sc
    * @param path
    * @return
    */
  def loadModel(sc: SparkContext, path: String): LatentMatrixFactorizationModel = {
    //val pt = new Path(path)
    //require(pt.canRead)
    val vf = IndexedRDD(sc.objectFile[(Long, LatentFactor)](path + "/" + "itemFeature"))
    val uf = IndexedRDD(sc.objectFile[(Long, LatentFactor)](path + "/" + "userFeature"))
    val otherParams = sc.textFile(path + "/" + "other").collect()
    require(otherParams.length == 4)
    new LatentMatrixFactorizationModel(otherParams(3).toInt, uf, vf, otherParams(0).toFloat, otherParams(2).toFloat, otherParams(1).toFloat)
  }

  /**
    * @param initialModel
    */
  def setModel(initialModel: LatentMatrixFactorizationModel) = {
    model = Some(initialModel)
  }
}

/**
 * Trains a Matrix Factorization Model for Recommendation Systems. The model consists of
 * user factors (User Matrix, `U`), product factors (Product Matrix, `P^T^`),
 * user biases (user bias vector, `bu`), product biases (product bias vector, `bp`) and
 * the global bias (global average, `mu`).
 *
 * Trained on a DStream, but can make predictions on a DStream or RDD.
 *
 * @param params Parameters for training
 */
class StreamingLatentMatrixFactorization(params: LatentMatrixFactorizationParams)
  extends LatentMatrixFactorization(params) {

  def this() = this(new LatentMatrixFactorizationParams)

  /** Return the latest model. */
  def latestModel() = {
    model.get.asInstanceOf[StreamingLatentMatrixFactorizationModel]
  }

  /**
   * Update the model by training on batches of data from a DStream.
   * This operation registers a DStream for training the model,
   * and updates the model based on every subsequent
   * batch of data from the stream.
   *
   * @param data DStream containing Ratings
   */
  def trainOn(data: DStream[Rating[Long]]): Unit = {
    data.foreachRDD { (rdd, time) =>
      if (!model.isEmpty) {
        val (initialModel, numExamples) =
          LatentMatrixFactorizationModel.initialize(rdd, params, model, isStreaming = true)
        model = Some(optimizer.train(rdd, initialModel, numExamples).
          asInstanceOf[StreamingLatentMatrixFactorizationModel])
        logInfo(s"Model updated - time $time")
      } else {
        logInfo(s"Model not updated since it's empty - time $time")
      }
    }
  }
}

/**
 * Parameters for training a Matrix Factorization Model
 */
class LatentMatrixFactorizationParams() {
  private var rank: Int = 20
  private var minRating: Float = 1f
  private var maxRating: Float = 5f
  private var stepSize: Double = 1.0
  private var biasStepSize: Double = 1.0
  private var stepDecay: Double = 0.9
  private var lambda: Double = 10.0
  private var iter: Int = 10
  private var intermediateStorageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_SER
  private var seed: Long = System.currentTimeMillis()

  def getRank: Int = rank
  def getMinRating: Float = minRating
  def getMaxRating: Float = maxRating
  def getStepSize: Double = stepSize
  def getBiasStepSize: Double = biasStepSize
  def getStepDecay: Double = stepDecay
  def getLambda: Double = lambda
  def getIter: Int = iter
  def getIntermediateStorageLevel: StorageLevel = intermediateStorageLevel
  def getSeed: Long = seed

  /** The rank of the matrices. Default = 20 */
  def setRank(x: Int): this.type = {
    rank = x
    this
  }
  /** The minimum allowed rating. Default = 1.0 */
  def setMinRating(x: Float): this.type = {
    minRating = x
    this
  }
  /** The maximum allowed rating. Default = 5.0 */
  def setMaxRating(x: Float): this.type = {
    maxRating = x
    this
  }
  /** The step size to use during Gradient Descent. Default = 0.001 */
  def setStepSize(x: Double): this.type = {
    stepSize = x
    this
  }
  /** The step size to use for bias vectors during Gradient Descent. Default = 0.0001 */
  def setBiasStepSize(x: Double): this.type = {
    biasStepSize = x
    this
  }
  /** The value to decay the step size after each iteration. Default = 0.95 */
  def setStepDecay(x: Double): this.type = {
    stepDecay = x
    this
  }
  /** The regularization parameter. Default = 0.1 */
  def setLambda(x: Double): this.type = {
    lambda = x
    this
  }
  /** The number of iterations for Gradient Descent. Default = 5 */
  def setIter(x: Int): this.type = {
    iter = x
    this
  }
  /** The persistence level for intermediate RDDs. Default = MEMORY_AND_DISK_SER */
  def setIntermediateStorageLevel(x: StorageLevel): this.type = {
    intermediateStorageLevel = x
    this
  }

  /** The number of iterations for Gradient Descent. Default = 5 */
  def setSeed(x: Long): this.type = {
    seed = x
    this
  }
}
