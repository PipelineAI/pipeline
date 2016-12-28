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

package org.apache.beam.examples.tutorial.game.solution;

import org.apache.beam.examples.tutorial.game.GameActionInfo;
import org.apache.beam.examples.tutorial.game.GameActionInfo.KeyField;
import org.apache.beam.examples.tutorial.game.utils.ExerciseOptions;
import org.apache.beam.examples.tutorial.game.utils.Input;
import org.apache.beam.examples.tutorial.game.utils.Output;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;

/**
 * This pipeline extends {@link Exercise1} by windowing each of the input events
 * based on the event timestamp.
 *
 * <p>
 * This pipeline processes data collected from gaming events in batch, building
 * on {@link Exercise1} but using fixed windows. It calculates the sum of scores
 * per team, for each window, optionally allowing specification of two
 * timestamps before and after which data is filtered out. This allows a model
 * where late data collected after the intended analysis window can be included,
 * and any late-arriving data prior to the beginning of the analysis window can
 * be removed as well. By using windowing and adding element timestamps, we can
 * do finer-grained analysis than with the {@link Exercise1} pipeline. However,
 * our batch processing is high-latency, in that we don't get results from plays
 * at the beginning of the batch's time period until the batch is processed.
 */
public class Exercise3 {

  /**
   * A transform to compute the WindowedTeamScore.
   */
  public static class WindowedTeamScore
      extends PTransform<PCollection<GameActionInfo>, PCollection<KV<String, Integer>>> {

    private Duration duration;

    public WindowedTeamScore(Duration duration) {
      this.duration = duration;
    }

    @Override
    public PCollection<KV<String, Integer>> apply(PCollection<GameActionInfo> input) {
      // [START EXERCISE 3]:
      // JavaDoc: https://cloud.google.com/dataflow/java-sdk/JavaDoc
      // Developer Docs: https://cloud.google.com/dataflow/model/windowing
      //
      return input
          // Window.into() takes a WindowFn and returns a PTransform that
          // applies windowing
          // to the PCollection. FixedWindows.of() returns a WindowFn that
          // assigns elements
          // to windows of a fixed size. Use these methods to apply fixed
          // windows of size
          // this.duration to the PCollection.
          .apply(Window.into(FixedWindows.of(duration)))
          // Remember the ExtractAndSumScore PTransform from Exercise 1? We
          // parameterized
          // it over the KeyField. Use it here to compute the team scores.
          .apply("ExtractUserScore", new ExtractAndSumScore(KeyField.TEAM));
      // [END EXERCISE 3]
    }
  }

  /**
   * Run a batch pipeline to do windowed analysis of the data.
   */
  public static void main(String[] args) throws Exception {
    // Begin constructing a pipeline configured by commandline flags.
    ExerciseOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(ExerciseOptions.class);
    Pipeline pipeline = Pipeline.create(options);

    pipeline
        // Read a bounded set of generated data
        .apply(new Input.BoundedGenerator())
        // Extract and sum the windowed teamname/scores
        .apply(new WindowedTeamScore(Duration.standardMinutes(1)))
        // Write the hourly team scores to the "hourly_team_score" table
        .apply(new Output.WriteHourlyTeamScore());

    pipeline.run();
  }

  /**
   * A transform to extract key/score information from GameActionInfo, and sum
   * the scores. The constructor arg determines whether 'team' or 'user' info is
   * extracted.
   */
  private static class ExtractAndSumScore
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
}
