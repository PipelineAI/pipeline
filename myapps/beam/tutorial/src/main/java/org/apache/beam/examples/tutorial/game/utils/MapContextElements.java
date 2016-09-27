package org.apache.beam.examples.tutorial.game.utils;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;

/**
 * {@code PTransform}s for mapping a simple function over the elements of a
 * {@link PCollection}.
 */
public class MapContextElements<InputT, OutputT> extends PTransform<PCollection<InputT>, PCollection<OutputT>> {

  /**
   * For a {@code SerializableFunction<InputT, OutputT>} {@code fn} and output
   * type descriptor, returns a {@code PTransform} that takes an input
   * {@code PCollection<InputT>} and returns a {@code PCollection<OutputT>}
   * containing {@code fn.apply(v)} for every element {@code v} in the input.
   *
   * <p>
   * Example of use in Java 8:
   * 
   * <pre>
   * {@code
   * PCollection<Integer> wordLengths = words.apply(
   *     MapElements.via((String word) -> word.length())
   *         .withOutputType(new TypeDescriptor<Integer>() {});
   * }
   * </pre>
   *
   * <p>
   * In Java 7, the overload {@link #via(SimpleFunction)} is more concise as the
   * output type descriptor need not be provided.
   */
  public static <InputT, OutputT> MissingOutputTypeDescriptor<InputT, OutputT> via(
      SerializableFunction<KV<DoFn<InputT, OutputT>.ProcessContext, BoundedWindow>, OutputT> fn) {
    return new MissingOutputTypeDescriptor<>(fn);
  }

  /**
   * An intermediate builder for a {@link MapElements} transform. To complete
   * the transform, provide an output type descriptor to
   * {@link MissingOutputTypeDescriptor#withOutputType}. See
   * {@link #via(SerializableFunction)} for a full example of use.
   */
  public static final class MissingOutputTypeDescriptor<InputT, OutputT> {

    private final SerializableFunction<KV<DoFn<InputT, OutputT>.ProcessContext, BoundedWindow>, OutputT> fn;

    private MissingOutputTypeDescriptor(
        SerializableFunction<KV<DoFn<InputT, OutputT>.ProcessContext, BoundedWindow>, OutputT> fn) {
      this.fn = fn;
    }

    public MapContextElements<InputT, OutputT> withOutputType(TypeDescriptor<OutputT> outputType) {
      return new MapContextElements<>(fn, outputType);
    }
  }

  ///////////////////////////////////////////////////////////////////

  private final SerializableFunction<KV<DoFn<InputT, OutputT>.ProcessContext, BoundedWindow>, OutputT> fn;
  private final transient TypeDescriptor<OutputT> outputType;

  private MapContextElements(SerializableFunction<KV<DoFn<InputT, OutputT>.ProcessContext, BoundedWindow>, OutputT> fn,
      TypeDescriptor<OutputT> outputType) {
    this.fn = fn;
    this.outputType = outputType;
  }

  @Override
  public PCollection<OutputT> apply(PCollection<InputT> input) {
    return input.apply("Map", ParDo.of(new DoFn<InputT, OutputT>() {
      @ProcessElement
      public void processElement(ProcessContext c, BoundedWindow w) {
        fn.apply(KV.of(c, w));
      }

      @Override
      public void populateDisplayData(DisplayData.Builder builder) {
        MapContextElements.this.populateDisplayData(builder);
      }
    })).setTypeDescriptorInternal(outputType);
  }

  @Override
  public void populateDisplayData(DisplayData.Builder builder) {
    super.populateDisplayData(builder);
    builder.add(DisplayData.item("mapFn", fn.getClass()).withLabel("Map Function"));
  }
}
