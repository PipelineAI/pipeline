/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.beam.examples.tutorial.game;

import org.apache.beam.examples.tutorial.game.GameActionInfo.KeyField;
import org.apache.beam.examples.tutorial.game.utils.ChangeMe;
import org.apache.beam.examples.tutorial.game.utils.ExerciseOptions;
import org.apache.beam.examples.tutorial.game.utils.Input;
import org.apache.beam.examples.tutorial.game.utils.Output;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

/**
 * This is the first in a series of exercises that walk through writing some
 * basic Dataflow pipelines using randomly generated data from the gaming
 * scenario.
 *
 * <p>
 * In this gaming scenario, many users play, as members of different teams, over
 * the course of a day, and their actions are logged for processing. Some of the
 * logged game events may be late- arriving, if users play on mobile devices and
 * go transiently offline for a period
 *
 * <p>
 * This exercise introduces the basics of a batch pipeline that extracts some
 * data and computes per-user sums.
 *
 * <p>
 * This pipeline does batch processing of data collected from gaming events. It
 * calculates the sum of scores per user, over an entire batch of gaming data
 * (collected, say, for each day). The batch processing will not include any
 * late data that arrives after the day's cutoff point.
 *
 * <p>
 * To run this, you will need to set the following options in the "Arguments"
 * tab of the run configuration (you'll need this in future configurations as
 * well):
 * 
 * <pre>
 * {@code
 *   --dataset=YOUR-DATASET
 * }
 * </pre>
 */
public class Exercise1 {

  /**
   * A transform to extract key/score information from GameActionInfo, and sum
   * the scores. The constructor arg determines whether 'team' or 'user' info is
   * extracted.
   */
  public static class ExtractAndSumScore
      extends PTransform<PCollection<GameActionInfo>, PCollection<KV<String, Integer>>> {

    private final KeyField field;

    ExtractAndSumScore(KeyField field) {
      this.field = field;
    }

    @Override
    public PCollection<KV<String, Integer>> apply(PCollection<GameActionInfo> gameInfo) {

      // [START EXERCISE 1]:
      // JavaDoc: https://cloud.google.com/dataflow/java-sdk/JavaDoc
      // Developer Docs: https://cloud.google.com/dataflow/model/par-do
      //
      // Fill in the code to:
      //   1. Extract a KV<String, Integer> from each GameActionInfo corresponding to the given
      //      KeyField and the score.
      //   2. Compute the sum of the scores for each key.
      //   3. Run your pipeline using the DirectPipelineRunner.
      return gameInfo
        // MapElements is a PTransform for mapping a function over the elements of a
        // PCollection. MapElements.via() takes a lambda expression defining the function
        // to apply.
	      // Write the expression that creates key-value pairs, using the KeyField as the
	      // key and the score as the value. KV.of(key, value) creates a key-value pair.
        // Java erasure means we can't determine the output type of our MapElements.
        // We declare the output type explicitly using withOutputType.
        // Use the following code to add the output type:
        //.withOutputType(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.integers()))
        .apply(new ChangeMe<>() /* TODO: YOUR CODE GOES HERE */)
        // Sum is a family of PTransforms for computing the sum of elements in a PCollection.
        // Select the appropriate method to compute the sum over each key.
        .apply(new ChangeMe<>() /* TODO: YOUR CODE GOES HERE */);
      // [END EXERCISE 1]:
    }
  }

  /**
   * Run a batch pipeline.
   */
  public static void main(String[] args) throws Exception {
    // Begin constructing a pipeline configured by commandline flags.
    ExerciseOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(ExerciseOptions.class);
    Pipeline pipeline = Pipeline.create(options);

    pipeline
        // Generate a bounded set of data.
        .apply(new Input.BoundedGenerator())
        // Extract and sum username/score pairs from the event data.
        .apply("ExtractUserScore", new ExtractAndSumScore(KeyField.USER))
        // Write the user and score to the "user_score" BigQuery table.
        .apply(new Output.WriteUserScoreSums());

    // Run the batch pipeline.
    pipeline.run();
  }
}
