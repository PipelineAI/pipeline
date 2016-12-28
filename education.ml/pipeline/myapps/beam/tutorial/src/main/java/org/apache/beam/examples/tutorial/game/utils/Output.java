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

package org.apache.beam.examples.tutorial.game.utils;

import java.util.TimeZone;

import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollection.IsBounded;
import org.apache.beam.sdk.values.PDone;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Helpers for writing output
 */
public class Output {

  private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")
      .withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST")));

  private static class Base<InputT> extends PTransform<PCollection<InputT>, PDone> {

    private final String fileName;
    protected MapContextElements<InputT, String> objToString;

    public Base(String fileName) {
      this.fileName = fileName;
    }

    @Override
    public PDone apply(PCollection<InputT> input) {
      String outputFilename =
          input.getPipeline().getOptions().as(ExerciseOptions.class).getOutputPrefix() + fileName;

      PCollection<String> formatted = input.apply(objToString);

      if (input.isBounded().equals(IsBounded.BOUNDED)) {
        formatted.apply(TextIO.Write.to(outputFilename));
      } else {
        formatted.apply(ParDo.of(new UnboundedWriteIO(outputFilename)));
      }

      return PDone.in(input.getPipeline());
    }
  }

  /**
   * Writes to the {@code user_score} table the following columns: -
   * {@code user} from the string key - {@code total_score} from the integer
   * value
   */
  public static class WriteUserScoreSums extends Base<KV<String, Integer>> {
    public WriteUserScoreSums() {
      this("user_score");
    }

    protected WriteUserScoreSums(String fileName) {
      super(fileName);

      objToString = MapContextElements
          .<KV<String, Integer>, String>via((KV<DoFn<KV<String, Integer>, String>.ProcessContext, BoundedWindow> c) -> {
            String output = "user: " + c.getKey().element().getKey() + " total_score:"
                + c.getKey().element().getValue();
            c.getKey().output(output);

            return null;
          }).withOutputType(TypeDescriptors.strings());
    }
  }

  /**
   * Writes to the {@code hourly_team_score} table the following columns: -
   * {@code team} from the string key - {@code total_score} from the integer
   * value - {@code window_start} from the start time of the window
   */
  public static class WriteHourlyTeamScore extends Base<KV<String, Integer>> {
    public WriteHourlyTeamScore() {
      this("hourly_team_score");
    }

    protected WriteHourlyTeamScore(String tableName) {
      super(tableName);

      objToString = MapContextElements
          .<KV<String, Integer>, String>via((KV<DoFn<KV<String, Integer>, String>.ProcessContext, BoundedWindow> c) -> {
            IntervalWindow w = (IntervalWindow) c.getValue();

            String output = w.toString() + " : team: " + c.getKey().element().getKey() + " total_score:"
                + c.getKey().element().getValue() + " window_start:" + DATE_TIME_FMT.print(w.start());

            c.getKey().output(output);

            return null;
          }).withOutputType(TypeDescriptors.strings());
    }
  }

  /**
   * Writes to the {@code triggered_user_score} table the following columns: -
   * {@code user} from the string key - {@code total_score} from the integer
   * value - {@code processing_time} the time at which the row was written
   */
  public static class WriteTriggeredUserScoreSums extends Base<KV<String, Integer>> {
    public WriteTriggeredUserScoreSums() {
      super("triggered_user_score");

      objToString = MapContextElements
          .<KV<String, Integer>, String>via((KV<DoFn<KV<String, Integer>, String>.ProcessContext, BoundedWindow> c) -> {
            String output =
                "user: " + c.getKey().element().getKey()
                + " processing_time: " + DATE_TIME_FMT.print(Instant.now())
                + " total_score:" + c.getKey().element().getValue();

            c.getKey().output(output);

            return null;
          }).withOutputType(TypeDescriptors.strings());
    }
  }

  /**
   * Writes to the {@code triggered_team_score} table the following columns: -
   * {@code team} from the string key - {@code total_score} from the integer
   * value - {@code window_start} from the start time of the window -
   * {@code processing_time} the time at which the row was written -
   * {@code timing} a string describing whether the row is early, on-time, or
   * late
   */
  public static class WriteTriggeredTeamScore extends Base<KV<String, Integer>> {
    public WriteTriggeredTeamScore() {
      super("triggered_team_score");

      objToString = MapContextElements
          .<KV<String, Integer>, String>via((KV<DoFn<KV<String, Integer>, String>.ProcessContext, BoundedWindow> c) -> {
            IntervalWindow w = (IntervalWindow) c.getValue();

            String output = c.getValue().toString() + " : team: " + c.getKey().element().getKey()
                + " processing_time: " + DATE_TIME_FMT.print(Instant.now())
                + " timing: " + c.getKey().pane().getTiming().toString()
                + " total_score: " + c.getKey().element().getValue()
                + " window_start: " + DATE_TIME_FMT.print(w.start());

            c.getKey().output(output);

            return null;
          }).withOutputType(TypeDescriptors.strings());
    }
  }
}
