package io.pipeline.prediction.tensorflow;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

public class LabelImage {
//  private static void printUsage(PrintStream s) {
//    final String url =
//        "https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip";
//    s.println(
//        "Java program that uses a pre-trained Inception model (http://arxiv.org/abs/1512.00567)");
//    s.println("to label JPEG images.");
//    s.println("TensorFlow version: " + TensorFlow.version());
//    s.println();
//    s.println("Usage: label_image <model dir> <image file>");
//    s.println();
//    s.println("Where:");
//    s.println("<model dir> is a directory containing the unzipped contents of the inception model");
//    s.println("            (from " + url + ")");
//    s.println("<image file> is the path to a JPEG image file");
//  }

//  public static void main(String[] args) {
//    if (args.length != 2) {
//      printUsage(System.err);
//      System.exit(1);
//    }
//    String modelDir = args[0];
//    String imageFile = args[1];
//
//    byte[] graphDef = readAllBytesOrExit(Paths.get(modelDir, "tensorflow_inception_graph.pb"));
//    List<String> labels =
//        readAllLinesOrExit(Paths.get(modelDir, "imagenet_comp_graph_label_strings.txt"));
//    byte[] imageBytes = readAllBytesOrExit(Paths.get(imageFile));
//
////    Graph g = new Graph()
////    GraphBuilder b = new GraphBuilder(g)
//
//    try (Tensor image = constructAndExecuteGraphToNormalizeImage(imageBytes)) {
//      float[] labelProbabilities = executeInceptionGraph(graphDef, image);
//      int[] bestLabelIdxs = maxKIndex(labelProbabilities, 10);
//      for (int i = 0; i < 10; i++) {
//        System.out.println(
//          String.format(
//              "BEST MATCH: %s (%.2f%% likely)",
//              labels.get(bestLabelIdxs[i]), labelProbabilities[bestLabelIdxs[i]] * 100f));
//      }
//    }
//  }

  public static Tensor constructAndExecuteGraphToNormalizeImage(byte[] imageBytes) {
    try (Graph g = new Graph()) {
      GraphBuilder b = new GraphBuilder(g);
      // Some constants specific to the pre-trained model at:
      // https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
      //
      // - The model was trained with images scaled to 224x224 pixels.
      // - The colors, represented as R, G, B in 1-byte each were converted to
      //   float using (value - Mean)/Scale.
      final int H = 224;
      final int W = 224;
      final float mean = 117f;
      final float scale = 1f;

      // Since the graph is being constructed once per execution here, we can use a constant for the
      // input image. If the graph were to be re-used for multiple input images, a placeholder would
      // have been more appropriate.
      final Output input = b.constant("input", imageBytes);
      final Output output =
          b.div(
              b.sub(
                  b.resizeBilinear(
                      b.expandDims(
                          b.cast(b.decodeJpeg(input, 3), DataType.FLOAT),
                          b.constant("make_batch", 0)),
                      b.constant("size", new int[] {H, W})),
                  b.constant("mean", mean)),
              b.constant("scale", scale));

      try (Session s = new Session(g)) {
        return s.runner().fetch(output.op().name()).run().get(0);
      }
    }
  }

  public static float[] executeInceptionGraph(byte[] graphDef, Tensor image) {
    try (Graph g = new Graph()) {
      g.importGraphDef(graphDef);
      try (Session s = new Session(g);
          Tensor result = s.runner().feed("input", image).fetch("output").run().get(0)) {
        final long[] rshape = result.shape();
        if (result.numDimensions() != 2 || rshape[0] != 1) {
          throw new RuntimeException(
              String.format(
                  "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                  Arrays.toString(rshape)));
        }
        int nlabels = (int) rshape[1];
        return result.copyTo(new float[1][nlabels])[0];
      }
    }
  }

  /**
    * Return the indexes correspond to the top-k largest in an array.
    */
  public static int[] maxKIndex(float[] probabilities, int k) {
    float[] max = new float[k];
    int[] maxIndex = new int[k];
    Arrays.fill(max, Float.NEGATIVE_INFINITY);
    Arrays.fill(maxIndex, -1);

    top: for(int i = 0; i < probabilities.length; i++) {
        for(int j = 0; j < k; j++) {
            if(probabilities[i] > max[j]) {
                for(int x = k - 1; x > j; x--) {
                    maxIndex[x] = maxIndex[x-1]; max[x] = max[x-1];
                }
                maxIndex[j] = i; max[j] = probabilities[i];
                continue top;
            }
        }
    }
    return maxIndex;
  }

  public static int maxIndex(float[] probabilities) {
    int best = 0;
    for (int i = 1; i < probabilities.length; ++i) {
      if (probabilities[i] > probabilities[best]) {
        best = i;
      }
    }
    return best;
  }

  public static byte[] readAllBytesOrExit(Path path) {
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      System.err.println("Failed to read [" + path + "]: " + e.getMessage());
      System.exit(1);
    }
    return null;
  }

  public static List<String> readAllLinesOrExit(Path path) {
    try {
      return Files.readAllLines(path, Charset.forName("UTF-8"));
    } catch (IOException e) {
      System.err.println("Failed to read [" + path + "]: " + e.getMessage());
      System.exit(0);
    }
    return null;
  }

  // In the fullness of time, equivalents of the methods of this class should be auto-generated from
  // the OpDefs linked into libtensorflow_jni.so. That would match what is done in other languages
  // like Python, C++ and Go.
  public static class GraphBuilder {
    GraphBuilder(Graph g) {
      this.g = g;
    }

    Output div(Output x, Output y) {
      return binaryOp("Div", x, y);
    }

    Output sub(Output x, Output y) {
      return binaryOp("Sub", x, y);
    }

    Output resizeBilinear(Output images, Output size) {
      return binaryOp("ResizeBilinear", images, size);
    }

    Output expandDims(Output input, Output dim) {
      return binaryOp("ExpandDims", input, dim);
    }

    Output cast(Output value, DataType dtype) {
      return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().output(0);
    }

    Output decodeJpeg(Output contents, long channels) {
      return g.opBuilder("DecodeJpeg", "DecodeJpeg")
          .addInput(contents)
          .setAttr("channels", channels)
          .build()
          .output(0);
    }

    Output constant(String name, Object value) {
      try (Tensor t = Tensor.create(value)) {
        return g.opBuilder("Const", name)
            .setAttr("dtype", t.dataType())
            .setAttr("value", t)
            .build()
            .output(0);
      }
    }

    private Output binaryOp(String type, Output in1, Output in2) {
      return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0);
    }

    private Graph g;
  }
}
