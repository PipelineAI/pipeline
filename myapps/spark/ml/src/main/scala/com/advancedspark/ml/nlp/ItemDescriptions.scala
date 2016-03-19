package com.advancedspark.ml.nlp

import java.util.Properties
import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.hcoref.data._
import edu.stanford.nlp.dcoref._
import edu.stanford.nlp.dcoref.CorefCoreAnnotations
import edu.stanford.nlp.dcoref.CorefCoreAnnotations._
import edu.stanford.nlp.util._
import edu.stanford.nlp.semgraph.SemanticGraph
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.ling.CoreAnnotations._;
import edu.stanford.nlp.trees.TreeCoreAnnotations
import edu.stanford.nlp.trees.TreeCoreAnnotations._;
import org.apache.spark.sql.types._
import org.apache.spark.sql.Row
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._

object ItemDescriptions {
  def main(args: Array[String]) {
    val conf = new SparkConf()

    val sc = SparkContext.getOrCreate(conf)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
    val props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
    val pipeline = new StanfordCoreNLP(props);
    
    // read some text in the text variable
    val text = "Stanford University is located in California. It is a great university." // Add your text here!
    
    // create an empty Annotation just with the given text
    val document = new Annotation(text);
    
    // run all Annotators on this text
    pipeline.annotate(document);
    
    // these are all the sentences in this document
    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
    val sentences = document.get(new SentencesAnnotation().getClass);
    
    for(sentence <- sentences) {
      // traversing the words in the current sentence
      // a CoreLabel is a CoreMap with additional token-specific methods
      for (token <- sentence.get(new TokensAnnotation().getClass)) {
        // this is the text of the token
        val word = token.get(new TextAnnotation().getClass);
        // this is the POS tag of the token
        val pos = token.get(new PartOfSpeechAnnotation().getClass);
        // this is the NER label of the token
        val ne = token.get(new NamedEntityTagAnnotation().getClass);       
      }

      // this is the parse tree of the current sentence
      val tree = sentence.get(new TreeAnnotation().getClass);

      // this is the Stanford dependency graph of the current sentence
      val dependencies = sentence.get(new CollapsedCCProcessedDependenciesAnnotation().getClass);
    }

    // This is the coreference link graph
    // Each chain stores a set of mentions that link to each other,
    // along with a method for getting the most representative mention
    // Both sentence and token offsets start at 1!
    val graph = document.get(new CorefChainAnnotation().getClass);
    System.out.println(graph)
  }
}
