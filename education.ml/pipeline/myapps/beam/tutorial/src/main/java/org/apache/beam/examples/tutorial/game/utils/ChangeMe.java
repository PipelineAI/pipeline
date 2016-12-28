package org.apache.beam.examples.tutorial.game.utils;

import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;

public class ChangeMe<InputT, OutputT> extends PTransform<PCollection<InputT>, PCollection<OutputT>> {

  @Override
  public PCollection<OutputT> apply(PCollection<InputT> input) {
    throw new RuntimeException("Not implemented");
  }

}
