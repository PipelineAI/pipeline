package org.apache.beam.examples.tutorial.game.utils;

import org.apache.beam.examples.tutorial.game.Exercise1;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.Validation;

/**
 * Options supported by {@link Exercise1}.
 */
public interface ExerciseOptions extends PipelineOptions {
  @Description("BigQuery Dataset to write tables to. Must already exist.")
  // @Validation.Required
  String getDataset();

  void setDataset(String value);

  @Description("Prefix for output files, either local path or cloud storage location.")
  @Default.String("output/")
  String getOutputPrefix();
  void setOutputPrefix(String value);
}
