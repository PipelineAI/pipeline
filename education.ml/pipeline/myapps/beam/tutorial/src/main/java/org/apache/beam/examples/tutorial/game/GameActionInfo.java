package org.apache.beam.examples.tutorial.game;

import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.joda.time.Instant;

/**
 * Class to hold info about a game event.
 */
@DefaultCoder(AvroCoder.class)
public class GameActionInfo {
  @Nullable
  private String user;
  @Nullable
  private String team;
  @Nullable
  private Integer score;
  @Nullable
  private Instant timestamp;

  public GameActionInfo() {
  }

  public GameActionInfo(String user, String team, Integer score, Instant timestamp) {
    this.user = user;
    this.team = team;
    this.score = score;
    this.timestamp = timestamp;
  }

  public String getUser() {
    return this.user;
  }

  public String getTeam() {
    return this.team;
  }

  public Integer getScore() {
    return this.score;
  }

  public Instant getTimestamp() {
    return this.timestamp;
  }

  /**
   * The kinds of key fields that can be extracted from a
   * {@link GameActionInfo}.
   */
  public enum KeyField {
    TEAM {
      @Override
      public String extract(GameActionInfo g) {
        return g.team;
      }
    },
    USER {
      @Override
      public String extract(GameActionInfo g) {
        return g.user;
      }
    };

    public abstract String extract(GameActionInfo g);
  }
}