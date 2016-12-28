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

import org.apache.beam.examples.tutorial.game.GameActionInfo;
import org.apache.beam.examples.tutorial.game.injector.InjectorBoundedSource;
import org.apache.beam.examples.tutorial.game.injector.InjectorUnboundedSource;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;

/**
 * Helpers for generating the input
 */
public class Input {

  /**
   * Generate a bounded {link PCollection} of data.
   */
  public static class BoundedGenerator extends PTransform<PBegin, PCollection<GameActionInfo>> {
    @Override
    public PCollection<GameActionInfo> apply(PBegin input) {
      return input.apply(Read.from(new InjectorBoundedSource(100, 180, 200)));
    }
  }

  /**
   * Generate an unbounded {@link PCollection} of data.
   */
  public static class UnboundedGenerator extends PTransform<PBegin, PCollection<GameActionInfo>> {
    @Override
    public PCollection<GameActionInfo> apply(PBegin input) {
      return input.apply(Read.from(new InjectorUnboundedSource()));
    }
  }
}
