package org.apache.beam.examples.tutorial.game.utils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.util.IOChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Note: this class shouldn't be used for production code. It is used for
 * debugging and working locally on Beam pipelines.
 */
public class UnboundedWriteIO extends DoFn<String, Void> {
  private static final Logger LOG = LoggerFactory.getLogger(UnboundedWriteIO.class);

  /** Static map of streams keyed by prefix.
   *
   * This allows us to ensure writes to any given prefix are serialized. Note that
   * we never close streams once opened, since we don't have any guarantees on DoFn
   * lifetimes, and thus can't guarantee that we won't get more DoFn instances wanting
   * access to the same file in the future.
   */
  private static Map<String, OutputStream> streams = Maps.newHashMap();

  private static synchronized void writeToStream(String prefix, List<String> outputs) throws IOException {

    // Check if stream for this prefix has been created yet. Initialize if not.
    OutputStream stream = streams.get(prefix);
    if (stream == null) {
      stream = Channels.newOutputStream(IOChannelUtils.create(prefix, "text/plain"));
      streams.put(prefix, stream);
    }

    // Write out all outputs
    for (String output : outputs) {
      stream.write(output.getBytes(StandardCharsets.UTF_8));
      stream.write("\n".getBytes(StandardCharsets.UTF_8));
    }

    // Flush to be sure our writes are observed, since we never close the stream.
    stream.flush();
  }

  /** List of output data to write */
  private transient List<String> outputs;

  /** Prefix of the file to write */
  protected final String prefix;

  /**
   * Constructor
   *
   * @param prefix
   *          Prefix of the file to write
   */
  public UnboundedWriteIO(String prefix) {
    this.prefix = prefix;
  }

  @StartBundle
  public void startBundle(Context c) {
    outputs = Lists.newArrayList();
  }

  @ProcessElement
  public void processElement(ProcessContext c, BoundedWindow w) throws Exception {
    outputs.add(c.element());
  }

  @FinishBundle
  public void finishBundle(Context c) throws IOException {
    writeToStream(prefix, outputs);

    // Clear outputs as they've been written
    outputs.clear();
  }
}
