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
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.GlobalWindows;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;

/**
 * In this exercise we start processing unbounded streams of input. As in
 * {@link Exercise3} we use fixed windows and event times. In this example we
 * use triggers to generate early and speculative results, as well as
 * {@code .accumulatingFiredPanes()} to do cumulative processing of
 * late-arriving data.
 *
 * <p>
 * This pipeline processes an unbounded stream of 'game events'. The calculation
 * of the team scores uses fixed windowing based on event time (the time of the
 * game play event), not processing time (the time that an event is processed by
 * the pipeline). The pipeline calculates the sum of scores per team, for each
 * window. By default, the team scores are calculated using one-hour windows.
 *
 * <p>
 * To demonstrate other windowing options the user scores are calculated using a
 * global window, which periodically (every ten minutes) emits cumulative user
 * score sums.
 *
 * <p>
 * In contrast to the previous pipelines in the series, which used static,
 * finite input data, here we're using an unbounded data source, which lets us
 * provide speculative results, and allows handling of late data, at much lower
 * latency. We can use the early/speculative results to keep a 'leaderboard'
 * updated in near-realtime. Our handling of late data lets us generate correct
 * results, e.g. for 'team prizes'. We're now outputting window results as
 * they're calculated, giving us much lower latency than with the previous batch
 * examples.
 */
public class Exercise4 {

  // Extract user/score pairs from the event stream using processing time, via
  // global windowing.
  public static class UserLeaderBoard
      extends PTransform<PCollection<GameActionInfo>, PCollection<KV<String, Integer>>> {

    private Duration allowedLateness;
    private Duration updateFrequency;

    public UserLeaderBoard(Duration allowedLateness, Duration updateFrequency) {
      this.allowedLateness = allowedLateness;
      this.updateFrequency = updateFrequency;
    }

    @Override
    public PCollection<KV<String, Integer>> apply(PCollection<GameActionInfo> input) {
      // [START EXERCISE 4 Part 1 - User Leaderboard]
      // JavaDoc: https://cloud.google.com/dataflow/java-sdk/JavaDoc
      // Developer Docs: https://cloud.google.com/dataflow/model/triggering
      //
      // Compute the user scores since the beginning of time. To do this we will
      // need:
      //
      // 1. Use the GlobalWindows WindowFn so that all events occur in the same
      // window.
      // 2. Specify the accumulation mode so that total score is constantly
      // increasing.
      // 3. Trigger every updateFrequency duration
      //
      return input
          .apply(Window
              // Since we want a globally increasing sum, use the GlobalWindows
              // WindowFn
              .<GameActionInfo>into(new GlobalWindows())
              // We want periodic results every updateFrequency of processing
              // time. We will be triggering repeatedly and forever, starting
              // updateFrequency after the first element seen. Window.
              .triggering(Repeatedly.forever(
                  AfterProcessingTime
                    .pastFirstElementInPane()
                    .plusDelayOf(updateFrequency)))
              // Specify the accumulation mode to ensure that each firing of the
              // trigger produces monotonically increasing sums rather than just
              // deltas.
              .accumulatingFiredPanes())

          // Extract and sum username/score pairs from the event data.
          // You can use the ExtractAndSumScore transform again.
          // Name the step -- look at overloads of apply().
          .apply("ExtractUserScore", new ExtractAndSumScore(KeyField.USER));
      // [END EXERCISE 4 Part 1]
    }
  }

  // Extract user/score pairs from the event stream using processing time, via
  // global windowing.
  public static class TeamLeaderBoard
      extends PTransform<PCollection<GameActionInfo>, PCollection<KV<String, Integer>>> {

    private Duration allowedLateness;
    private Duration earlyUpdateFrequency;
    private Duration lateUpdateFrequency;
    private Duration windowSize;

    public TeamLeaderBoard(Duration allowedLateness, Duration earlyUpdateFrequency, Duration lateUpdateFrequency,
        Duration windowSize) {
      this.allowedLateness = allowedLateness;
      this.earlyUpdateFrequency = earlyUpdateFrequency;
      this.lateUpdateFrequency = lateUpdateFrequency;
      this.windowSize = windowSize;
    }

    @Override
    public PCollection<KV<String, Integer>> apply(PCollection<GameActionInfo> input) {
      // [START EXERCISE 4 Part 2 - Team Leaderboard]
      // JavaDoc: https://cloud.google.com/dataflow/java-sdk/JavaDoc
      // Developer Docs: https://cloud.google.com/dataflow/model/triggering
      //
      // We're going to produce windowed team score again, but this time we want
      // to get
      // early (speculative) results as well as occasional late updates.
      return input
          .apply("FixedWindows",
              Window.<GameActionInfo>into(FixedWindows.of(windowSize))
                  .triggering(AfterWatermark.pastEndOfWindow()
                      // Specify .withEarlyFirings to produce speculative
                      // results
                      // with a delay of earlyUpdateFrequency
                      .withEarlyFirings(AfterProcessingTime.pastFirstElementInPane().alignedTo(earlyUpdateFrequency))
                      // Specify .withLateFirings to produce late updates with a
                      // delay
                      // of lateUpdateFrequency
                      .withLateFirings(AfterProcessingTime.pastFirstElementInPane().alignedTo(lateUpdateFrequency)))
                  // Specify allowed lateness, and ensure that we get cumulative
                  // results
                  // across the window.
                  .withAllowedLateness(allowedLateness).accumulatingFiredPanes())
          // Extract and sum teamname/score pairs from the event data.
          // You can use the ExtractAndSumScore transform again.
          .apply("ExtractTeamScore", new ExtractAndSumScore(KeyField.TEAM));
      // [END EXERCISE 4 Part 2]
    }
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

  private static final Duration ALLOWED_LATENESS = Duration.standardMinutes(30);
  private static final Duration EARLY_UPDATE_FREQUENCY = Duration.standardSeconds(10);
  private static final Duration LATE_UPDATE_FREQUENCY = Duration.standardSeconds(20);
  private static final Duration WINDOW_SIZE = Duration.standardMinutes(1);

  public static void main(String[] args) throws Exception {
    ExerciseOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(ExerciseOptions.class);

    Pipeline pipeline = Pipeline.create(options);

    // Read game events from the unbounded injector.
    PCollection<GameActionInfo> gameEvents = pipeline.apply(new Input.UnboundedGenerator());

    // Extract user/score pairs from the event stream using processing time for
    // regular updates.
    gameEvents.apply(new UserLeaderBoard(ALLOWED_LATENESS, EARLY_UPDATE_FREQUENCY))
        // Write the results to BigQuery.
        .apply(new Output.WriteTriggeredUserScoreSums());

    // Extract team/score pairs from the event stream, using windows with early
    // and late updates.
    gameEvents.apply(new TeamLeaderBoard(ALLOWED_LATENESS, EARLY_UPDATE_FREQUENCY, LATE_UPDATE_FREQUENCY, WINDOW_SIZE))
        // Write the results to BigQuery.
        .apply(new Output.WriteTriggeredTeamScore());

    // Run the pipeline
    pipeline.run();
  }
}
