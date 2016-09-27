package org.apache.beam.examples.tutorial.game.utils;

import org.apache.beam.sdk.transforms.DoFn;

public class ChangeMeFN<InputT, OutputT> extends DoFn<InputT, OutputT> {

  @ProcessElement
  public void processElement(ProcessContext context) throws Exception {
    throw new RuntimeException("Not implemented");
  }

}
