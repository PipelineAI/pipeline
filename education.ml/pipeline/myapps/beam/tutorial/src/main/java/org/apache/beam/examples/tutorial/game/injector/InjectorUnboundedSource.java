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
package org.apache.beam.examples.tutorial.game.injector;

import org.apache.beam.examples.tutorial.game.GameActionInfo;
import org.apache.beam.examples.tutorial.game.injector.InjectorIterator.SourceConfig;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.UnboundedSource;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.util.SerializableUtils;
import org.joda.time.Duration;
import org.joda.time.Instant;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.annotation.Nullable;

/**
 * An {@link UnboundedSource} which generates {@link GameActionInfo} events.
 */
public class InjectorUnboundedSource extends UnboundedSource<GameActionInfo, InjectorUnboundedSource.Checkpoint> {

  /**
   * Checkpointing an {@link InjectorUnboundedSource} requires remembering our
   * random number generator, among other things.
   */
  public static class Checkpoint implements UnboundedSource.CheckpointMark, Serializable {

    private final Random random;

    public Checkpoint(Random random) {
      this.random = SerializableUtils.clone(random);
    }

    @Override
    public void finalizeCheckpoint() throws IOException {
      // Nothing is necessary for checkpoints.
    }

    public Random getRandom() {
      return random;
    }
  }

  private final SourceConfig config;

  public InjectorUnboundedSource() {
    this(new SourceConfig(null /* no limit */, 15, 5000, 8000));
  }

  private InjectorUnboundedSource(SourceConfig config) {
    this.config = config;
  }

  @Override
  public List<InjectorUnboundedSource> generateInitialSplits(int desiredNumSplits, PipelineOptions options)
      throws Exception {
    int numSplits = Math.min(desiredNumSplits, config.numTeams);
    ArrayList<InjectorUnboundedSource> splits = new ArrayList<>(numSplits);
    for (SourceConfig config : config.split(numSplits)) {
      splits.add(new InjectorUnboundedSource(config));
    }
    return splits;
  }

  @Override
  public InjectorUnboundedReader createReader(PipelineOptions options, @Nullable Checkpoint checkpoint) {
    return new InjectorUnboundedReader(this, checkpoint);
  }

  @Override
  public Coder<Checkpoint> getCheckpointMarkCoder() {
    return SerializableCoder.of(Checkpoint.class);
  }

  @Override
  public void validate() {
    // Nothing to validate
  }

  @Override
  public Coder<GameActionInfo> getDefaultOutputCoder() {
    return AvroCoder.of(GameActionInfo.class);
  }

  private static class InjectorUnboundedReader extends UnboundedReader<GameActionInfo> {

    private final InjectorUnboundedSource source;
    private final Random random;
    private final InjectorIterator items;

    private GameActionInfo currentEvent = null;
    private GameActionInfo nextEvent;
    private Instant nextEventTimestamp;

    // The "watermark" is simulated to be this far behind the current processing
    // time. All arriving
    // data is generated within this delay from now, with a high probability
    // (80%)
    private static final Duration WATERMARK_DELAY = Duration.standardSeconds(5);

    // 15% of the data will be between WATERMARK_DELAY and WATERMARK_DELAY +
    // LATE_DELAY of now.
    // 5% of the data will be between WATERMARK_DELAY + LATE_DELAY and
    // WATERMARK_DELAY + 2 * LATE_DELAY of now.
    private static final Duration LATE_DELAY = Duration.standardSeconds(25);

    private static final int PERCENT_ONE_UNITS_LATE = 7;
    private static final int PERCENT_TWO_UNITS_LATE = 3;

    public InjectorUnboundedReader(InjectorUnboundedSource source, @Nullable Checkpoint initialCheckpoint) {
      this.source = source;
      this.items = new InjectorIterator(source.config);
      random = initialCheckpoint == null ? new Random() : initialCheckpoint.getRandom();

      // TODO: Should probably put nextArrivalTime and currentEvent into the
      // checkpoint?
      nextEvent = items.next();
      nextEventTimestamp = new Instant(nextEvent.getTimestamp());
    }

    @Override
    public boolean start() throws IOException {
      return advance();
    }

    private Instant arrivalTimeToEventTime(Instant arrivalTime) {
      int bucket = random.nextInt(100);
      Instant eventTime = arrivalTime;
      if (bucket <= PERCENT_ONE_UNITS_LATE) {
        eventTime = eventTime.minus(LATE_DELAY);
      }
      if (bucket <= PERCENT_ONE_UNITS_LATE + PERCENT_TWO_UNITS_LATE) {
        eventTime = eventTime.minus(WATERMARK_DELAY);
        eventTime = eventTime.minus(randomDuration(LATE_DELAY));
      } else {
        eventTime = eventTime.minus(randomDuration(WATERMARK_DELAY));
      }
      return eventTime;
    }

    private Duration randomDuration(Duration max) {
      return Duration.millis(random.nextInt((int) max.getMillis()));
    }

    @Override
    public boolean advance() throws IOException {
      if (nextEventTimestamp.isAfterNow()) {
        // Not yet ready to emit the next event
        currentEvent = null;
        return false;
      } else {
        // The next event is available now. Figure out what its actual event
        // time was:
        currentEvent = new GameActionInfo(nextEvent.getUser(), nextEvent.getTeam(), nextEvent.getScore(),
            arrivalTimeToEventTime(nextEvent.getTimestamp()));

        // And we should peek to see when the next event will be ready.
        nextEvent = items.next();
        nextEventTimestamp = currentEvent.getTimestamp();
        return true;
      }
    }

    @Override
    public Instant getWatermark() {
      return Instant.now().minus(WATERMARK_DELAY);
    }

    @Override
    public Checkpoint getCheckpointMark() {
      return new Checkpoint(random);
    }

    @Override
    public UnboundedSource<GameActionInfo, ?> getCurrentSource() {
      return source;
    }

    @Override
    public GameActionInfo getCurrent() throws NoSuchElementException {
      if (currentEvent == null) {
        throw new NoSuchElementException("No current element");
      }
      return currentEvent;
    }

    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
      return getCurrent().getTimestamp();
    }

    @Override
    public void close() throws IOException {
      // Nothing is necessary to close.
    }
  }
}
