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
/* tslint:disable:no-namespace variable-name */
var VZ;
(function (VZ) {
    var DistributionChart = (function () {
        function DistributionChart(xType, colorScale) {
            this.run2datasets = {};
            this.colorScale = colorScale;
            this.buildChart(xType);
        }
        DistributionChart.prototype.getDataset = function (run) {
            if (this.run2datasets[run] === undefined) {
                this.run2datasets[run] = new Plottable.Dataset([], { run: run });
            }
            return this.run2datasets[run];
        };
        DistributionChart.prototype.buildChart = function (xType) {
            if (this.outer) {
                this.outer.destroy();
            }
            var xComponents = VZ.ChartHelpers.getXComponents(xType);
            this.xAccessor = xComponents.accessor;
            this.xScale = xComponents.scale;
            this.xAxis = xComponents.axis;
            this.xAxis.margin(0).tickLabelPadding(3);
            this.yScale = new Plottable.Scales.Linear();
            this.yAxis = new Plottable.Axes.Numeric(this.yScale, 'left');
            var yFormatter = VZ.ChartHelpers.multiscaleFormatter(VZ.ChartHelpers.Y_AXIS_FORMATTER_PRECISION);
            this.yAxis.margin(0).tickLabelPadding(5).formatter(yFormatter);
            this.yAxis.usesTextWidthApproximation(true);
            var center = this.buildPlot(this.xAccessor, this.xScale, this.yScale);
            this.gridlines =
                new Plottable.Components.Gridlines(this.xScale, this.yScale);
            this.center = new Plottable.Components.Group([this.gridlines, center]);
            this.outer = new Plottable.Components.Table([[this.yAxis, this.center], [null, this.xAxis]]);
        };
        DistributionChart.prototype.buildPlot = function (xAccessor, xScale, yScale) {
            var _this = this;
            var percents = [0, 228, 1587, 3085, 5000, 6915, 8413, 9772, 10000];
            var opacities = _.range(percents.length - 1)
                .map(function (i) { return (percents[i + 1] - percents[i]) / 2500; });
            var accessors = percents.map(function (p, i) { return function (datum) { return datum[i][1]; }; });
            var median = 4;
            var medianAccessor = accessors[median];
            var plots = _.range(accessors.length - 1).map(function (i) {
                var p = new Plottable.Plots.Area();
                p.x(xAccessor, xScale);
                var y0 = i > median ? accessors[i] : accessors[i + 1];
                var y = i > median ? accessors[i + 1] : accessors[i];
                p.y(y, yScale);
                p.y0(y0);
                p.attr('fill', function (d, i, dataset) {
                    return _this.colorScale.scale(dataset.metadata().run);
                });
                p.attr('stroke', function (d, i, dataset) {
                    return _this.colorScale.scale(dataset.metadata().run);
                });
                p.attr('stroke-weight', function (d, i, m) { return '0.5px'; });
                p.attr('stroke-opacity', function () { return opacities[i]; });
                p.attr('fill-opacity', function () { return opacities[i]; });
                return p;
            });
            var medianPlot = new Plottable.Plots.Line();
            medianPlot.x(xAccessor, xScale);
            medianPlot.y(medianAccessor, yScale);
            medianPlot.attr('stroke', function (d, i, m) { return _this.colorScale.scale(m.run); });
            this.plots = plots;
            return new Plottable.Components.Group(plots);
        };
        DistributionChart.prototype.setVisibleSeries = function (runs) {
            var _this = this;
            this.runs = runs;
            var datasets = runs.map(function (r) { return _this.getDataset(r); });
            this.plots.forEach(function (p) { return p.datasets(datasets); });
        };
        /**
         * Set the data of a series on the chart.
         */
        DistributionChart.prototype.setSeriesData = function (name, data) {
            this.getDataset(name).data(data);
        };
        DistributionChart.prototype.renderTo = function (targetSVG) {
            this.targetSVG = targetSVG;
            this.setViewBox();
            this.outer.renderTo(targetSVG);
        };
        /** There's an issue in Chrome where the svg overflow is a bit
         * "flickery". There is a border on the gridlines on the extreme edge of the
         * chart, which behaves inconsistently and causes the screendiffing tests to
         * flake. We can solve this by creating 1px effective margin for the svg by
         * setting the viewBox on the containing svg.
         */
        DistributionChart.prototype.setViewBox = function () {
            // There's an issue in Firefox where if we measure with the old viewbox
            // set, we get horrible results.
            this.targetSVG.attr('viewBox', null);
            var parent = this.targetSVG.node().parentNode;
            var w = parent.clientWidth;
            var h = parent.clientHeight;
            this.targetSVG.attr({
                'height': h,
                'viewBox': "0 0 " + (w + 1) + " " + (h + 1),
            });
        };
        DistributionChart.prototype.redraw = function () {
            this.outer.redraw();
            this.setViewBox();
        };
        DistributionChart.prototype.destroy = function () { this.outer.destroy(); };
        return DistributionChart;
    }());
    VZ.DistributionChart = DistributionChart;
})(VZ || (VZ = {}));
