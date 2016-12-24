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

import java.util.Map;

import org.apache.beam.examples.tutorial.game.Exercise4;
import org.apache.beam.examples.tutorial.game.GameActionInfo;
import org.apache.beam.examples.tutorial.game.GameActionInfo.KeyField;
import org.apache.beam.examples.tutorial.game.utils.ExerciseOptions;
import org.apache.beam.examples.tutorial.game.utils.Input;
import org.apache.beam.examples.tutorial.game.utils.Output;
import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Aggregator;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Mean;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the final exercise in our series. We use side-inputs to build a more
 * complicated pipeline structure.
 *
 * <p>
 * This pipeline builds on the {@link Exercise4} functionality, and adds some
 * "business intelligence" analysis: abuse detection. The pipeline derives the
 * Mean user score sum for a window, and uses that information to identify
 * likely spammers/robots. (The robots have a higher click rate than the human
 * users). The 'robot' users are then filtered out when calculating the team
 * scores.
 */
public class Exercise5 {

  /**
   * Filter out all but those users with a high clickrate, which we will
   * consider as 'spammy' uesrs. We do this by finding the mean total score per
   * user, then using that information as a side input to filter out all but
   * those user scores that are > (mean * SCORE_WEIGHT)
   */
  public static class CalculateSpammyUsers
      extends PTransform<PCollection<KV<String, Integer>>, PCollection<KV<String, Integer>>> {
    private static final Logger LOG = LoggerFactory.getLogger(CalculateSpammyUsers.class);
    private static final double SCORE_WEIGHT = 2.5;

    @Override
    public PCollection<KV<String, Integer>> apply(PCollection<KV<String, Integer>> userScores) {

      // Get the sum of scores for each user.
      PCollection<KV<String, Integer>> sumScores = userScores.apply("UserSum", Sum.<String>integersPerKey());

      // Extract the score from each element, and use it to find the global
      // mean.
      final PCollectionView<Double> globalMeanScore = sumScores.apply(Values.<Integer>create())
          .apply(Mean.<Integer>globally().asSingletonView());

      // Filter the user sums using the global mean.
      return sumScores.apply("ProcessAndFilter",
          ParDo
              // use the derived mean total score as a side input
              .withSideInputs(globalMeanScore).of(new DoFn<KV<String, Integer>, KV<String, Integer>>() {
                private final Aggregator<Long, Long> numSpammerUsers = createAggregator("SpammerUsers",
                    new Sum.SumLongFn());

                @ProcessElement
                public void processElement(ProcessContext c) {
                  int score = c.element().getValue();
                  double globalMean = c.sideInput(globalMeanScore);
                  // [START EXERCISE 5 - Part 1]
                  // JavaDoc: https://cloud.google.com/dataflow/java-sdk/JavaDoc
                  // Developer Docs:
                  // https://cloud.google.com/dataflow/model/par-do#side-inputs
                  //
                  // If the score is 2.5x the global average in each window, log
                  // the username and its score. Hint: (Use LOG.info for logging
                  // the score)
                  if (score * SCORE_WEIGHT > globalMean) {
                    LOG.info("Spammer found username:" + c.element().getKey() + " score:" + score);

                    //
                    // Increment the numSpammerUsers aggregator to record the
                    // number of spammers identified.
                    numSpammerUsers.addValue(1L);

                    //
                    // Output only the spammy user entries.
                    c.output(c.element());
                  }
                  // [END EXERCISE 5 - Part 1]
                }
              }));
    }
  }

  public static class WindowedNonSpammerTeamScore
      extends PTransform<PCollection<GameActionInfo>, PCollection<KV<String, Integer>>> {

    private Duration windowSize;

    public WindowedNonSpammerTeamScore(Duration windowSize) {
      this.windowSize = windowSize;
    }

    @Override
    public PCollection<KV<String, Integer>> apply(PCollection<GameActionInfo> input) {
      // Create a side input view of the spammy users
      final PCollectionView<Map<String, Integer>> spammersView = createSpammersView(input);

      PCollection<GameActionInfo> teamWindows = input.apply("TeamWindows",
          Window.<GameActionInfo>into(FixedWindows.of(windowSize)));

      // [START EXERCISE 5 - Part 2]
      // JavaDoc: https://cloud.google.com/dataflow/java-sdk/JavaDoc
      // Developer Docs:
      // https://cloud.google.com/dataflow/model/par-do#side-inputs
      //
      // For this part, we'll use the previously computed spammy users as a
      // side-input to identify which entries should be filtered out. Compute
      // team scores over only those individuals who are not identified as
      // spammers.
      return teamWindows.apply("FilterOutSpammers",
          ParDo
              // Configure the ParDo to read the side input. Hint: use
              // ParDo.withSideInputs()
              .withSideInputs(spammersView).of(new DoFn<GameActionInfo, GameActionInfo>() {
                @ProcessElement
                public void processElement(ProcessContext c) {
                  Map<String, Integer> spammers = c.sideInput(spammersView);
                  
                  // Only output those user not appearing in the spammers map
                  if (!spammers.containsKey(c.element().getUser())) {
                    c.output(c.element());
                  }
                }
              }))
          // Use the ExtractAndSumScore to compute the team scores.
          .apply("ExtractTeamScore", new ExtractAndSumScore(KeyField.TEAM));
      // [END EXERCISE 5 - Part 2]
    }

    private PCollectionView<Map<String, Integer>> createSpammersView(PCollection<GameActionInfo> input) {
      return input
          .apply("ExtractUserScore",
              MapElements.via((GameActionInfo gInfo) -> KV.of(gInfo.getUser(), gInfo.getScore()))
                  .withOutputType(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.integers())))
          .apply("UserWindows", Window.<KV<String, Integer>>into(FixedWindows.of(windowSize)))
          // Filter out everyone but those with (SCORE_WEIGHT * avg) clickrate.
          // These might be robots/spammers.
          .apply("CalculateSpammyUsers", new CalculateSpammyUsers())
          // Derive a view from the collection of spammer users. It will be used
          // as a side input in calculating the team score sums, below.
          .apply("CreateSpammersView", View.<String, Integer>asMap());
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
      return gameInfo.apply(MapElements.via((GameActionInfo gInfo) -> KV.of(field.extract(gInfo), gInfo.getScore()))
          .withOutputType(new TypeDescriptor<KV<String, Integer>>() {
          })).apply(Sum.<String>integersPerKey());
    }
  }

  private static final Duration WINDOW_SIZE = Duration.standardMinutes(1);

  public static void main(String[] args) throws Exception {
    ExerciseOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(ExerciseOptions.class);
    // Allow the pipeline to be cancelled automatically.
    options.setRunner(DirectRunner.class);
    Pipeline pipeline = Pipeline.create(options);

    // Read Events from the custom unbounded source
    PCollection<GameActionInfo> rawEvents = pipeline.apply(new Input.UnboundedGenerator());

    // Calculate the total score per team over fixed windows,
    // and emit cumulative updates for late data. Uses the side input derived
    // above-- the set of
    // suspected robots-- to filter out scores from those users from the sum.
    rawEvents.apply(new WindowedNonSpammerTeamScore(WINDOW_SIZE))
        // Write the result to BigQuery
        .apply(new Output.WriteTriggeredTeamScore());

    pipeline.run();
  }
}
