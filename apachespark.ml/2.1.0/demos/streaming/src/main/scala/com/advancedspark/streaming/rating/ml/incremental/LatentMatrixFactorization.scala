package com.advancedspark.streaming.rating.ml.incremental

import java.io.PrintWriter

import scala.reflect.ClassTag

import org.apache.hadoop.fs.Path
import org.apache.spark.SparkContext
import org.apache.spark.api.java.JavaRDD
import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.api.java.JavaDStream
import org.apache.spark.streaming.dstream.DStream
import org.json4s.JsonDSL._

import com.advancedspark.streaming.rating.ml.incremental.model.LatentFactor
import com.advancedspark.streaming.rating.ml.incremental.model.LatentMatrixFactorizationModel
import com.advancedspark.streaming.rating.ml.incremental.model.LatentMatrixFactorizationModelOps
import com.advancedspark.streaming.rating.ml.incremental.optimization.MFGradientDescent

import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD._

/**
 * This library contains methods to train a Matrix Factorization Recommendation System on Spark. 
*  For user u and item i, the rating is calculated as:
 *     r = U(u) * P^T^(i) + bu(u) + bi(i) + mu
 * where r is the rating, 
 *       U is the User Matrix, 
 *       P^T^ is the transpose of the item matrix, 
 *       U(u) corresponds to the uth row of U, 
 *       bu(u) is the bias of the uth user, 
 *       bi(i) is the bias of the ith item,
 *       mu is the average global rating.
 *
 * Trained on a static RDD, but can make predictions on a DStream or RDD.
 *
 * @param params Parameters for training
 */
class LatentMatrixFactorization (params: LatentMatrixFactorizationParams) {

  def this() = this(new LatentMatrixFactorizationParams)

  // TODO:  Hide optimizer so it's not publicly available
  val optimizer = new MFGradientDescent(params)

  // TODO:  Also, figure out if this model state is being properly managed.    
  //        And why we need LatentMatrixFactorizationModelOps.  
  private var model: Option[LatentMatrixFactorizationModel] = None

  /**
    * @param initialModel
    */
  def setModel(initialModel: LatentMatrixFactorizationModel) = {
    model = Some(initialModel)
  }

  def trainOn(ratings: RDD[Rating[Long]]): LatentMatrixFactorizationModel = {
    if (!model.isEmpty) {
      val (initialModel, numExamples) =
        LatentMatrixFactorizationModelOps.train(ratings, params, model, isStreaming = false)
      model = Some(optimizer.train(ratings, initialModel, numExamples))
    }
    model.get
  }

  /** Java-friendly version of `trainOn`. */
  def trainOn(ratings: JavaRDD[Rating[Long]]): Unit = trainOn(ratings.rdd)

  /**
   * Use the model to make predictions on batches of data from a DStream
   *
   * @param data DStream containing (user, item) tuples
   * @return DStream containing rating predictions
   */
  def predictOn(data: DStream[(Long, Long)]): DStream[Rating[Long]] = {
    if (model.isEmpty) {
      throw new IllegalStateException("Model cannot be empty.")
    }
    data.transform((rdd, time) => model.get.predict(rdd))
  }

  /** Java-friendly version of `predictOn`. */
  def predictOn(data: JavaDStream[(Long, Long)]): JavaDStream[Rating[java.lang.Long]] = {
    JavaDStream.fromDStream(predictOn(data.dstream).asInstanceOf[DStream[Rating[java.lang.Long]]])
  }

  /**
   * Use the model to make predictions on the values of a DStream and carry over its keys.
   * @param data DStream containing (user, item) tuples
   * @tparam K key type
   * @return DStream containing the input keys and the rating predictions as values
   */
  def predictOnValues[K: ClassTag](data: DStream[(K, (Long, Long))]): DStream[(K, Rating[Long])] = {
    if (model.isEmpty) {
      throw new IllegalArgumentException("Model cannot be empty.")
    }
    data.transform((rdd, time) => rdd.keys.zip(model.get.predict(rdd.values)))
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  // TODO:  Update this save/load code to match the latest Spark model saving using Parquet
  //////////////////////////////////////////////////////////////////////////////////////////
  /**
    * @param path
    */
  // TODO:  Figure out why we need to pass the model in here versus using the model attr
  def saveObject(aModel: LatentMatrixFactorizationModel, path: String) = {
    aModel.userFactors.saveAsObjectFile(path + "/" + "userFactors")
    aModel.itemFactors.saveAsObjectFile(path + "/" + "itemFactors")
    val pw = new PrintWriter(path + "/" + "metadata")
    pw.println(aModel.globalBias)
    pw.println(aModel.maxRating)
    pw.println(aModel.minRating)
    pw.print(aModel.rank)
    pw.close()
  }

  /**
    * @param sc
    * @param path
    * @return
    */
  def loadModel(sc: SparkContext, path: String): LatentMatrixFactorizationModel = {
    val vf = IndexedRDD(sc.objectFile[(Long, LatentFactor)](path + "/" + "itemFactors"))
    val uf = IndexedRDD(sc.objectFile[(Long, LatentFactor)](path + "/" + "userFactors"))
    val metadata = sc.textFile(path + "/" + "metadata").collect()
    require(metadata.length == 4)
    new LatentMatrixFactorizationModel(metadata(3).toInt, uf, vf, metadata(0).toFloat, metadata(2).toFloat, metadata(1).toFloat)
  }


  /**********************/
  /* DEBUGGING PURPOSES */
  /**********************/
  /**
    * @param path
    */
  def saveText(aModel: LatentMatrixFactorizationModel, path: String) = {
    aModel.userFactors.saveAsTextFile(path + "/" + "userFactors")
    aModel.itemFactors.saveAsTextFile(path + "/" + "itemFactors")
    val pw = new PrintWriter(path + "/" + "metadata")
    pw.println(aModel.globalBias)
    pw.println(aModel.maxRating)
    pw.println(aModel.minRating)
    pw.print(aModel.rank)
    // TODO:  numObservations/Examples?
    pw.close()
  }

  def saveParquet(aModel: LatentMatrixFactorizationModel, path: String, sc: SparkContext) = {
    val extraMetadata = "rank" -> aModel.rank
    // TODO:  This requies that LatentMatrixFactorizationModel extends org.apache.spark.ml.param.Params
//    DefaultParamsWriter.saveMetadata(model.get, path, sc, Some(extraMetadata))
    val userPath = new Path(path, "userFactors").toString

    // TODO:  figure out how to save these factors (IndexedRDD) as parquet
    aModel.userFactors.collect()
      //.write.format("parquet").save(userPath)
    val itemPath = new Path(path, "itemFactors").toString

    // TODO:  figure out how to save these factors (IndexedRDD) as parquet 
    aModel.itemFactors.collect()
      //.write.format("parquet").save(itemPath)
  }
}
