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
import org.apache.beam.examples.tutorial.game.utils.ExerciseOptions;
import org.apache.beam.examples.tutorial.game.utils.Input;
import org.apache.beam.examples.tutorial.game.utils.Output;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

/**
 * In this exercise we'll run the same pipeline using the
 * DataflowPipelineRunner. This runs in the cloud using multiple workers.
 *
 * <p>
 * In the "Pipeline Options" tab of the Dataflow run configuration use either
 * the DataflowPipelineRunner or the BlockingDataflowPipelineRunner. You will
 * need to specify your Google Cloud Project and a staging location. Make sure
 * to include the dataset option in the "Arguments" tab as well:
 * 
 * <pre>
 * {@code
 *   --dataset=YOUR-DATASET
 * }
 * </pre>
 */
public class Exercise2 {

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
      return gameInfo
          .apply(MapElements.via((GameActionInfo gInfo) -> KV.of(field.extract(gInfo), gInfo.getScore()))
              .withOutputType(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.integers())))
          .apply(Sum.<String>integersPerKey());
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
