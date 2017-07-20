/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the 'License');
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an 'AS IS' BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
suite('graph', function () {
    var assert = chai.assert;
    test('graphlib exists', function () { assert.isTrue(graphlib != null); });
    test('simple graph contruction', function (done) {
        var pbtxt = tf.graph.test.util.stringToArrayBuffer("\n      node {\n        name: \"Q\"\n        op: \"Input\"\n      }\n      node {\n        name: \"W\"\n        op: \"Input\"\n      }\n      node {\n        name: \"X\"\n        op: \"MatMul\"\n        input: \"Q:2\"\n        input: \"W\"\n      }");
        var statsPbtxt = tf.graph.test.util.stringToArrayBuffer("step_stats {\n      dev_stats {\n        device: \"cpu\"\n        node_stats {\n          node_name: \"Q\"\n          all_start_micros: 10\n          all_end_rel_micros: 4\n        }\n        node_stats {\n          node_name: \"Q\"\n          all_start_micros: 12\n          all_end_rel_micros: 4\n        }\n      }\n    }");
        var buildParams = {
            enableEmbedding: true,
            inEmbeddingTypes: ['Const'],
            outEmbeddingTypes: ['^[a-zA-Z]+Summary$'],
            refEdges: {}
        };
        var dummyTracker = tf.graph.util.getTracker({ set: function () { return; }, progress: 0 });
        tf.graph.parser.parseGraphPbTxt(pbtxt).then(function (nodes) {
            tf.graph.build(nodes, buildParams, dummyTracker)
                .then(function (slimGraph) {
                assert.isTrue(slimGraph.nodes['X'] != null);
                assert.isTrue(slimGraph.nodes['W'] != null);
                assert.isTrue(slimGraph.nodes['Q'] != null);
                var firstInputOfX = slimGraph.nodes['X'].inputs[0];
                assert.equal(firstInputOfX.name, 'Q');
                assert.equal(firstInputOfX.outputTensorIndex, 2);
                var secondInputOfX = slimGraph.nodes['X'].inputs[1];
                assert.equal(secondInputOfX.name, 'W');
                assert.equal(secondInputOfX.outputTensorIndex, 0);
                tf.graph.parser.parseStatsPbTxt(statsPbtxt).then(function (stepStats) {
                    tf.graph.joinStatsInfoWithGraph(slimGraph, stepStats);
                    assert.equal(slimGraph.nodes['Q'].stats.totalMicros, 6);
                    done();
                });
            });
        });
    });
    // TODO(bp): write tests.
});
