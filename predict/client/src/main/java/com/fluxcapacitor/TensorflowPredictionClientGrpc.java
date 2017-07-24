package com.fluxcapacitor;

// Derived from the following:
//   https://github.com/tobegit3hub/deep_recommend_system/blob/master/java_predict_client/src/main/java/com/tobe/PredictClient.java

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The general predit client for TensorFlow models.
 */
public class TensorflowPredictionClientGrpc {
    private static final Logger logger = Logger.getLogger(TensorflowPredictionClientGrpc.class.getName());
    private final ManagedChannel channel;
    private final PredictionServiceGrpc.PredictionServiceBlockingStub stub;
    //private final Model.ModelSpec modelSpec;
    
    // Initialize gRPC client
    public TensorflowPredictionClientGrpc(String host, int port)
    {
        channel = ManagedChannelBuilder.forAddress(host, port)
           // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
           // needing certificates.
          .usePlaintext(true)
          .build();

        stub = PredictionServiceGrpc.newBlockingStub(channel);    
    }

    public String predict(String namespace, String modelName, Integer version, String inputJson) {
        // TODO:  Use generic json -> tensor proto utility (ie. from TF Java API)
        // Generate features TensorProto
        float[][] featuresTensorData = new float[][] {
            {0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f}
        };

        TensorProto.Builder featuresTensorBuilder = TensorProto.newBuilder();
        featuresTensorBuilder.setDtype(org.tensorflow.framework.DataType.DT_FLOAT);

        for (int i = 0; i < featuresTensorData.length; ++i) {
            for (int j = 0; j < featuresTensorData[i].length; ++j) {
               featuresTensorBuilder.addFloatVal(featuresTensorData[i][j]);
            }
        }

        TensorShapeProto.Dim dim1 = TensorShapeProto.Dim.newBuilder().setSize(1).build();
        TensorShapeProto.Dim dim2 = TensorShapeProto.Dim.newBuilder().setSize(featuresTensorData[0].length).build();

        TensorShapeProto shape = TensorShapeProto.newBuilder().addDim(dim1).addDim(dim2).build();

        featuresTensorBuilder.setTensorShape(shape);

        TensorProto featuresTensorProto = featuresTensorBuilder.build();

        Model.ModelSpec modelSpec = Model.ModelSpec.newBuilder().setName(modelName)
           //.setVersion(version)
          .build();

        Predict.PredictRequest request = Predict.PredictRequest.newBuilder()
          .setModelSpec(modelSpec).putInputs("features", featuresTensorProto)
          .build();

        // Request gRPC server
        Predict.PredictResponse response;
        try {
            response = stub.predict(request);

            java.util.Map<java.lang.String, org.tensorflow.framework.TensorProto> outputs = response.getOutputsMap();
            //for (java.util.Map.Entry<java.lang.String, org.tensorflow.framework.TensorProto> entry : outputs.entrySet()) {
            //    System.out.println("Response with the key: " + entry.getKey() + ", value: " + entry.getValue());
            //}

            return outputs.get("prediction").toString();
        } catch (StatusRuntimeException e) {
            //logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        	System.out.println(e);
            throw e;
        }
    }
    
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
        
    public static void main(String[] args) {
        //System.out.println("Start the predict client");

        String host = "127.0.0.1";
        int port = 9000;
        String namespace = "my_namespace";
        String modelName = "tensorflow_minimal";
        Integer version = 1;
        String inputJson = "";

        // Parse command-line arguments
        if (args.length == 5) {
            host = args[0];
            port = Integer.parseInt(args[1]);
            modelName = args[2];
            inputJson = args[3];
        }

        // Run predict client to send request
        TensorflowPredictionClientGrpc client = new TensorflowPredictionClientGrpc(host, port);

        String response = null;
        try {
            response = client.predict(namespace, modelName, version, inputJson);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                client.shutdown();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        //System.out.println("Response: " + response);
        //System.out.println("End of predict client");
    }
 
}
