package org.apache.beam.examples.tutorial.game.injector;

import com.google.api.client.repackaged.com.google.common.base.Objects;
import com.google.api.client.util.Preconditions;

import org.apache.beam.examples.tutorial.game.GameActionInfo;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

/**
 * An iterator over an infinite stream of {@link GameActionInfo} objects.
 */
public class InjectorIterator implements Iterator<GameActionInfo> {

  /** The parameters for the Injector. */
  public static class SourceConfig implements Serializable {
    public final @Nullable Integer numEntries;
    public final int numTeams;
    public final int minQps;
    public final int maxQps;

    public SourceConfig(@Nullable Integer numEntries, int numTeams, int minQps, int maxQps) {
      this.numEntries = numEntries;
      this.numTeams = numTeams;
      this.minQps = minQps;
      this.maxQps = maxQps;
    }

    /**
     * Divide a SourceConfig into some number of parts, with the following
     * constraints:
     *
     * 1. Each part should be responsible for an integer number of teams. 2.
     * Entries and QPS should be divided between splits proportionally to the
     * teams.
     */
    public SourceConfig[] split(int numParts) {
      SourceConfig[] parts = new SourceConfig[numParts];
      for (int i = 0; i < numParts; i++) {
        int newTeams = numTeams / numParts + (i < numTeams % numParts ? 1 : 0);
        parts[i] = new SourceConfig(numEntries == null ? null : newTeams * (numEntries / numTeams), newTeams,
            newTeams * (minQps / numTeams), newTeams * (maxQps / numTeams));
      }
      return parts;
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(InjectorIterator.class);

  // Lists used to generate random team names.
  private static final ArrayList<String> COLORS = new ArrayList<String>(Arrays.asList("Magenta", "AliceBlue", "Almond",
      "Amaranth", "Amber", "Amethyst", "AndroidGreen", "AntiqueBrass", "Fuchsia", "Ruby", "AppleGreen", "Apricot",
      "Aqua", "ArmyGreen", "Asparagus", "Auburn", "Azure", "Banana", "Beige", "Bisque", "BarnRed", "BattleshipGrey"));

  private static final ArrayList<String> ANIMALS = new ArrayList<String>(
      Arrays.asList("Echidna", "Koala", "Wombat", "Marmot", "Quokka", "Kangaroo", "Dingo", "Numbat", "Emu", "Wallaby",
          "CaneToad", "Bilby", "Possum", "Cassowary", "Kookaburra", "Platypus", "Bandicoot", "Cockatoo", "Antechinus"));

  // The total number of robots in the system.
  private static final int NUM_ROBOTS = 20;
  // Determines the chance that a team will have a robot team member.
  private static final int ROBOT_PROBABILITY = 3;
  private static final int BASE_MEMBERS_PER_TEAM = 5;
  private static final int MEMBERS_PER_TEAM = 15;
  private static final int MAX_SCORE = 20;

  // The minimum time a 'team' can live will be in the range
  // [BASE_TEAM_EXPIRATION, BASE_TEAM_EXPIRATION + RANDOM_TEAM_EXPIRATION)
  private static final Duration BASE_TEAM_EXPIRATION = Duration.standardMinutes(20);
  private static final Duration RANDOM_TEAM_EXPIRATION = Duration.standardMinutes(20);

  // The list of live teams.
  private ArrayList<TeamInfo> liveTeams = new ArrayList<TeamInfo>();

  private Random random = new Random();
  private @Nullable Integer bound;

  // QPS ranges from 800 to 1000
  private final Iterator<Instant> timestamps;

  public InjectorIterator(SourceConfig config) {
    Instant now = Instant.now();
    timestamps = new QpsIterator(random, now, config.minQps, config.maxQps);
    this.bound = config.numEntries;
    while (liveTeams.size() < config.numTeams) {
      liveTeams.add(createTeam(now));
    }
  }

  @Override
  public boolean hasNext() {
    return bound == null || bound > 0;
  }

  @Override
  public GameActionInfo next() {
    Instant nextTimestamp = timestamps.next();

    TeamInfo team = selectRandomTeam(nextTimestamp);
    GameActionInfo nextItem = new GameActionInfo(team.getRandomUser(random), team.teamName, random.nextInt(MAX_SCORE),
        nextTimestamp);

    if (bound != null) {
      bound--;
    }

    return nextItem;
  }

  /** Return a random item from the list of items. */
  private <T> T randomItem(List<T> items) {
    return items.get(random.nextInt(items.size()));
  }

  /**
   * Select a random team from the list of teams. If the selected team is too
   * old w.r.t its expiration remove it, replacing it with a new team.
   */
  private TeamInfo selectRandomTeam(Instant now) {
    TeamInfo team = randomItem(liveTeams);
    if (team.endTime.isAfter(now)) {
      return team;
    }

    // This team has ended.
    liveTeams.remove(team);

    // Create and return a new team
    TeamInfo newTeam = createTeam(now);
    LOG.warn("{}: Team {} was too old; replaced them with {}.", now, team, newTeam);
    liveTeams.add(newTeam);
    return newTeam;
  }

  private TeamInfo createTeam(Instant now) {
    String teamName = randomItem(COLORS) + randomItem(ANIMALS);
    String robot = random.nextInt(ROBOT_PROBABILITY) == 0 ? null : "Robot-" + random.nextInt(NUM_ROBOTS);

    Instant startTime = now;
    Instant endTime = startTime.plus(randomDuration(BASE_TEAM_EXPIRATION, RANDOM_TEAM_EXPIRATION));
    TeamInfo newTeam = new TeamInfo(teamName, startTime, endTime, robot,
        BASE_MEMBERS_PER_TEAM + random.nextInt(MEMBERS_PER_TEAM));
    return newTeam;
  }

  private Duration randomDuration(Duration base, Duration randomMax) {
    return base.plus(Duration.millis(random.nextInt((int) randomMax.getMillis())));
  }

  /**
   * A class for holding team info: the name of the team, when it started, and
   * the current team members. Teams may but need not include one robot team
   * member.
   */
  private static class TeamInfo {
    private final String teamName;
    private final Instant startTime;
    private final Instant endTime;

    // The team might but need not include 1 robot. Will be non-null if so.
    @Nullable
    private String robot;
    private int numMembers;

    private TeamInfo(String teamName, Instant startTime, Instant endTime, @Nullable String robot, int numMembers) {
      this.teamName = teamName;
      this.startTime = startTime;
      this.endTime = endTime;
      this.robot = robot;
      this.numMembers = numMembers;
    }

    String getRandomUser(Random random) {
      // If we have a robot, then the probability of generating the robot should
      // be
      // higher than any specific user.
      if (robot != null && random.nextInt(numMembers / 2) == 0) {
        return robot;
      } else {
        return "user" + random.nextInt(numMembers) + "_" + teamName;
      }
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(getClass()).add("teamName", teamName).add("startTime", startTime)
          .add("endTime", endTime).add("robot", robot).add("numMembers", numMembers).toString();
    }
  }

  private static class QpsIterator implements Iterator<Instant> {
    private final Random random;
    private final long minDurationNanos;
    private final int durationRangeNanos;

    private Instant lastTimestamp;

    public QpsIterator(Random random, Instant initialTimestamp, int minQps, int maxQps) {
      Preconditions.checkArgument(minQps <= maxQps, "minQps(%1) should be <= maxQps(%2)", minQps, maxQps);
      this.random = random;
      lastTimestamp = initialTimestamp;
      minDurationNanos = TimeUnit.SECONDS.toNanos(1) / maxQps;
      long maxDurationNanos = TimeUnit.SECONDS.toNanos(1) / minQps;
      durationRangeNanos = Math.max(1, (int) (maxDurationNanos - minDurationNanos));
      Preconditions.checkArgument(durationRangeNanos > 0, "durationRangeNanos(%1) should be > 0", durationRangeNanos);
    }

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public Instant next() {
      long nextNanos = minDurationNanos + (random.nextInt(durationRangeNanos));
      lastTimestamp = lastTimestamp.plus(TimeUnit.NANOSECONDS.toMillis(nextNanos));
      return lastTimestamp;
    }
  }
}
