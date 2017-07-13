/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
var TF;
(function (TF) {
    var Backend;
    (function (Backend_1) {
        Backend_1.TYPES = [
            'scalar', 'histogram', 'compressedHistogram', 'graph', 'image', 'audio',
            'runMetadata'
        ];
        /**
         * The Backend class provides a convenient and typed interface to the backend.
         *
         * It provides methods corresponding to the different data sources on the
         * TensorBoard backend. These methods return a promise containing the data
         * from the backend. This class does some post-processing on the data; for
         * example, converting data elements tuples into js objects so that they can
         * be accessed in a more convenient and clearly-documented fashion.
         */
        var Backend = (function () {
            /**
             * Construct a Backend instance.
             * @param router the Router with info on what urls to get data from
             * @param requestManager The RequestManager, overwritable so you may
             * manually clear request queue, etc. Defaults to a new RequestManager.
             */
            function Backend(router, requestManager) {
                this.router = router;
                this.requestManager = requestManager || new Backend_1.RequestManager();
            }
            /**
             * Returns a promise for requesting the logdir string.
             */
            Backend.prototype.logdir = function () {
                return this.requestManager.request(this.router.logdir());
            };
            /**
             * Returns a listing of all the available data in the TensorBoard backend.
             */
            Backend.prototype.runs = function () {
                return this.requestManager.request(this.router.runs());
            };
            /**
             * Return a promise showing the Run-to-Tag mapping for scalar data.
             */
            Backend.prototype.scalarRuns = function () {
                return this.runs().then(function (x) { return _.mapValues(x, 'scalars'); });
            };
            /**
             * Return a promise showing the Run-to-Tag mapping for histogram data.
             */
            Backend.prototype.histogramRuns = function () {
                return this.runs().then(function (x) { return _.mapValues(x, 'histograms'); });
            };
            /**
             * Return a promise showing the Run-to-Tag mapping for image data.
             */
            Backend.prototype.imageRuns = function () {
                return this.runs().then(function (x) { return _.mapValues(x, 'images'); });
            };
            /**
             * Return a promise showing the Run-to-Tag mapping for audio data.
             */
            Backend.prototype.audioRuns = function () {
                return this.runs().then(function (x) { return _.mapValues(x, 'audio'); });
            };
            /**
             * Return a promise showing the Run-to-Tag mapping for compressedHistogram
             * data.
             */
            Backend.prototype.compressedHistogramRuns = function () {
                return this.runs().then(function (x) { return _.mapValues(x, 'compressedHistograms'); });
            };
            /**
             * Return a promise showing list of runs that contain graphs.
             */
            Backend.prototype.graphRuns = function () {
                return this.runs().then(function (x) { return _.keys(x).filter(function (k) { return x[k].graph; }); });
            };
            /**
             * Return a promise showing the Run-to-Tag mapping for run_metadata objects.
             */
            Backend.prototype.runMetadataRuns = function () {
                return this.runs().then(function (x) { return _.mapValues(x, 'run_metadata'); });
            };
            /**
             * Return a promise of a graph string from the backend.
             */
            Backend.prototype.graph = function (tag, limit_attr_size, large_attrs_key) {
                var url = this.router.graph(tag, limit_attr_size, large_attrs_key);
                return this.requestManager.request(url);
            };
            /**
             * Return a promise containing ScalarDatums for given run and tag.
             */
            Backend.prototype.scalar = function (tag, run) {
                var p;
                var url = this.router.scalars(tag, run);
                p = this.requestManager.request(url);
                return p.then(map(detupler(createScalar)));
            };
            /**
             * Return a promise containing HistogramDatums for given run and tag.
             */
            Backend.prototype.histogram = function (tag, run) {
                var p;
                var url = this.router.histograms(tag, run);
                p = this.requestManager.request(url);
                return p.then(map(detupler(createHistogram))).then(function (histos) {
                    // Get the minimum and maximum values across all histograms so that the
                    // visualization is aligned for all timesteps.
                    var min = d3.min(histos, function (d) { return d.min; });
                    var max = d3.max(histos, function (d) { return d.max; });
                    return histos.map(function (histo, i) {
                        return {
                            wall_time: histo.wall_time,
                            step: histo.step,
                            bins: convertBins(histo, min, max)
                        };
                    });
                });
            };
            /**
             * Return a promise containing ImageDatums for given run and tag.
             */
            Backend.prototype.image = function (tag, run) {
                var url = this.router.images(tag, run);
                var p;
                p = this.requestManager.request(url);
                return p.then(map(this.createImage.bind(this)));
            };
            /**
             * Return a promise containing AudioDatums for given run and tag.
             */
            Backend.prototype.audio = function (tag, run) {
                var url = this.router.audio(tag, run);
                var p;
                p = this.requestManager.request(url);
                return p.then(map(this.createAudio.bind(this)));
            };
            /**
             * Returns a promise to load the string RunMetadata for given run/tag.
             */
            Backend.prototype.runMetadata = function (tag, run) {
                var url = this.router.runMetadata(tag, run);
                return this.requestManager.request(url);
            };
            /**
             * Get compressedHistogram data.
             * Unlike other methods, don't bother reprocessing this data into a nicer
             * format. This is because we will deprecate this route.
             */
            Backend.prototype.compressedHistogram = function (tag, run) {
                var url = this.router.compressedHistograms(tag, run);
                var p;
                p = this.requestManager.request(url);
                return p.then(map(detupler(function (x) { return x; })));
            };
            Backend.prototype.createImage = function (x) {
                return {
                    width: x.width,
                    height: x.height,
                    wall_time: timeToDate(x.wall_time),
                    step: x.step,
                    url: this.router.individualImage(x.query, x.wall_time),
                };
            };
            Backend.prototype.createAudio = function (x) {
                return {
                    content_type: x.content_type,
                    wall_time: timeToDate(x.wall_time),
                    step: x.step,
                    url: this.router.individualAudio(x.query),
                };
            };
            return Backend;
        }());
        Backend_1.Backend = Backend;
        /** Given a RunToTag, return sorted array of all runs */
        function getRuns(r) {
            return _.keys(r).sort(VZ.Sorting.compareTagNames);
        }
        Backend_1.getRuns = getRuns;
        /** Given a RunToTag, return array of all tags (sorted + dedup'd) */
        function getTags(r) {
            return _.union.apply(null, _.values(r)).sort(VZ.Sorting.compareTagNames);
        }
        Backend_1.getTags = getTags;
        /**
         * Given a RunToTag and an array of runs, return every tag that appears for
         * at least one run.
         * Sorted, deduplicated.
         */
        function filterTags(r, runs) {
            var result = [];
            runs.forEach(function (x) { return result = result.concat(r[x]); });
            return _.uniq(result).sort(VZ.Sorting.compareTagNames);
        }
        Backend_1.filterTags = filterTags;
        function timeToDate(x) { return new Date(x * 1000); }
        ;
        /**  Just a curryable map to make things cute and tidy. */
        function map(f) {
            return function (arr) { return arr.map(f); };
        }
        ;
        /**
         * This is a higher order function that takes a function that transforms a
         * T into a G, and returns a function that takes TupleData<T>s and converts
         * them into the intersection of a G and a Datum.
         */
        function detupler(xform) {
            return function (x) {
                // Create a G, assert it has type <G & Datum>
                var obj = xform(x[2]);
                // ... patch in the properties of datum
                obj.wall_time = timeToDate(x[0]);
                obj.step = x[1];
                return obj;
            };
        }
        ;
        function createScalar(x) { return { scalar: x }; }
        ;
        function createHistogram(x) {
            return {
                min: x[0],
                max: x[1],
                nItems: x[2],
                sum: x[3],
                sumSquares: x[4],
                bucketRightEdges: x[5],
                bucketCounts: x[6],
            };
        }
        ;
        /**
         * Takes histogram data as stored by tensorboard backend and converts it to
         * the standard d3 histogram data format to make it more compatible and easier
         * to visualize. When visualizing histograms, having the left edge and width
         * makes things quite a bit easier. The bins are also converted to have an
         * uniform width, what makes the visualization easier to understand.
         *
         * @param histogram A histogram from tensorboard backend.
         * @param min The leftmost edge. The binning will start on it.
         * @param max The rightmost edge. The binning will end on it.
         * @param numBins The number of bins of the converted data. The default of 30
         * is a sensible default, using more starts to get artifacts because the event
         * data is stored in buckets, and you start being able to see the aliased
         * borders between each bucket.
         * @return A histogram bin. Each bin has an x (left edge), a dx (width),
         *     and a y (count).
         *
         * If given rightedges are inclusive, then these left edges (x) are exclusive.
         */
        function convertBins(histogram, min, max, numBins) {
            if (numBins === void 0) { numBins = 30; }
            if (histogram.bucketRightEdges.length !== histogram.bucketCounts.length) {
                throw (new Error('Edges and counts are of different lengths.'));
            }
            var binWidth = (max - min) / numBins;
            var bucketLeft = min; // Use the min as the starting point for the bins.
            var bucketPos = 0;
            return d3.range(min, max, binWidth).map(function (binLeft) {
                var binRight = binLeft + binWidth;
                // Take the count of each existing bucket, multiply it by the proportion
                // of overlap with the new bin, then sum and store as the count for the
                // new bin. If no overlap, will add to zero, if 100% overlap, will include
                // the full count into new bin.
                var binY = 0;
                while (bucketPos < histogram.bucketRightEdges.length) {
                    // Clip the right edge because right-most edge can be infinite-sized.
                    var bucketRight = Math.min(max, histogram.bucketRightEdges[bucketPos]);
                    var intersect = Math.min(bucketRight, binRight) - Math.max(bucketLeft, binLeft);
                    var count = (intersect / (bucketRight - bucketLeft)) *
                        histogram.bucketCounts[bucketPos];
                    binY += intersect > 0 ? count : 0;
                    // If bucketRight is bigger than binRight, than this bin is finished and
                    // there is data for the next bin, so don't increment bucketPos.
                    if (bucketRight > binRight) {
                        break;
                    }
                    bucketLeft = Math.max(min, bucketRight);
                    bucketPos++;
                }
                ;
                return { x: binLeft, dx: binWidth, y: binY };
            });
        }
        Backend_1.convertBins = convertBins;
    })(Backend = TF.Backend || (TF.Backend = {}));
})(TF || (TF = {}));
