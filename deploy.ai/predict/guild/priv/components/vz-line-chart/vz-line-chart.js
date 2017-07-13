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
    var LineChart = (function () {
        function LineChart(xType, yScaleType, colorScale, tooltip) {
            this.seriesNames = [];
            this.name2datasets = {};
            this.colorScale = colorScale;
            this.tooltip = tooltip;
            this.datasets = [];
            // lastPointDataset is a dataset that contains just the last point of
            // every dataset we're currently drawing.
            this.lastPointsDataset = new Plottable.Dataset();
            this.nanDataset = new Plottable.Dataset();
            // need to do a single bind, so we can deregister the callback from
            // old Plottable.Datasets. (Deregistration is done by identity checks.)
            this.onDatasetChanged = this._onDatasetChanged.bind(this);
            this.buildChart(xType, yScaleType);
        }
        LineChart.prototype.buildChart = function (xType, yScaleType) {
            if (this.outer) {
                this.outer.destroy();
            }
            var xComponents = VZ.ChartHelpers.getXComponents(xType);
            this.xAccessor = xComponents.accessor;
            this.xScale = xComponents.scale;
            this.xAxis = xComponents.axis;
            this.xAxis.margin(0).tickLabelPadding(3);
            this.yScale = LineChart.getYScaleFromType(yScaleType);
            this.yAxis = new Plottable.Axes.Numeric(this.yScale, 'left');
            var yFormatter = VZ.ChartHelpers.multiscaleFormatter(VZ.ChartHelpers.Y_AXIS_FORMATTER_PRECISION);
            this.yAxis.margin(0).tickLabelPadding(5).formatter(yFormatter);
            this.yAxis.usesTextWidthApproximation(true);
            this.dzl = new Plottable.DragZoomLayer(this.xScale, this.yScale);
            var center = this.buildPlot(this.xAccessor, this.xScale, this.yScale);
            this.gridlines =
                new Plottable.Components.Gridlines(this.xScale, this.yScale);
            this.center =
                new Plottable.Components.Group([this.gridlines, center, this.dzl]);
            this.outer = new Plottable.Components.Table([
                [this.yAxis, this.center],
                [null, this.xAxis]
            ]);
        };
        LineChart.prototype.buildPlot = function (xAccessor, xScale, yScale) {
            var _this = this;
            this.scalarAccessor = function (d) { return d.scalar; };
            this.smoothedAccessor = function (d) { return d.smoothed; };
            var linePlot = new Plottable.Plots.Line();
            linePlot.x(xAccessor, xScale);
            linePlot.y(this.scalarAccessor, yScale);
            linePlot.attr('stroke', function (d, i, dataset) {
                return _this.colorScale.scale(dataset.metadata().name);
            });
            this.linePlot = linePlot;
            var group = this.setupTooltips(linePlot);
            var smoothLinePlot = new Plottable.Plots.Line();
            smoothLinePlot.x(xAccessor, xScale);
            smoothLinePlot.y(this.smoothedAccessor, yScale);
            smoothLinePlot.attr('stroke', function (d, i, dataset) {
                return _this.colorScale.scale(dataset.metadata().name);
            });
            this.smoothLinePlot = smoothLinePlot;
            // The scatterPlot will display the last point for each dataset.
            // This way, if there is only one datum for the series, it is still
            // visible. We hide it when tooltips are active to keep things clean.
            var scatterPlot = new Plottable.Plots.Scatter();
            scatterPlot.x(xAccessor, xScale);
            scatterPlot.y(this.scalarAccessor, yScale);
            scatterPlot.attr('fill', function (d) { return _this.colorScale.scale(d.name); });
            scatterPlot.attr('opacity', 1);
            scatterPlot.size(VZ.ChartHelpers.TOOLTIP_CIRCLE_SIZE * 2);
            scatterPlot.datasets([this.lastPointsDataset]);
            this.scatterPlot = scatterPlot;
            var nanDisplay = new Plottable.Plots.Scatter();
            nanDisplay.x(xAccessor, xScale);
            nanDisplay.y(function (x) { return x.displayY; }, yScale);
            nanDisplay.attr('fill', function (d) { return _this.colorScale.scale(d.name); });
            nanDisplay.attr('opacity', 1);
            nanDisplay.size(VZ.ChartHelpers.NAN_SYMBOL_SIZE * 2);
            nanDisplay.datasets([this.nanDataset]);
            nanDisplay.symbol(Plottable.SymbolFactories.triangleUp);
            this.nanDisplay = nanDisplay;
            return new Plottable.Components.Group([nanDisplay, scatterPlot, smoothLinePlot, group]);
        };
        /** Updates the chart when a dataset changes. Called every time the data of
         * a dataset changes to update the charts.
         */
        LineChart.prototype._onDatasetChanged = function (dataset) {
            if (this.smoothingEnabled) {
                this.resmoothDataset(dataset);
            }
            this.updateSpecialDatasets();
        };
        LineChart.prototype.updateSpecialDatasets = function () {
            if (this.smoothingEnabled) {
                this.updateSpecialDatasetsWithAccessor(this.smoothedAccessor);
            }
            else {
                this.updateSpecialDatasetsWithAccessor(this.scalarAccessor);
            }
        };
        /** Constructs special datasets. Each special dataset contains exceptional
         * values from all of the regular datasets, e.g. last points in series, or
         * NaN values. Those points will have a `name` and `relative` property added
         * (since usually those are context in the surrounding dataset).
         * The accessor will point to the correct data to access.
         */
        LineChart.prototype.updateSpecialDatasetsWithAccessor = function (accessor) {
            var lastPointsData = this.datasets
                .map(function (d) {
                var datum = null;
                // filter out NaNs to ensure last point is a clean one
                var nonNanData = d.data().filter(function (x) { return !isNaN(accessor(x, -1, d)); });
                if (nonNanData.length > 0) {
                    var idx = nonNanData.length - 1;
                    datum = nonNanData[idx];
                    datum.name = d.metadata().name;
                    datum.relative =
                        VZ.ChartHelpers.relativeAccessor(datum, -1, d);
                }
                return datum;
            })
                .filter(function (x) { return x != null; });
            this.lastPointsDataset.data(lastPointsData);
            // Take a dataset, return an array of NaN data points
            // the NaN points will have a "displayY" property which is the
            // y-value of a nearby point that was not NaN (0 if all points are NaN)
            var datasetToNaNData = function (d) {
                var displayY = null;
                var data = d.data();
                var i = 0;
                while (i < data.length && displayY == null) {
                    if (!isNaN(accessor(data[i], -1, d))) {
                        displayY = accessor(data[i], -1, d);
                    }
                    i++;
                }
                if (displayY == null) {
                    displayY = 0;
                }
                var nanData = [];
                for (i = 0; i < data.length; i++) {
                    if (!isNaN(accessor(data[i], -1, d))) {
                        displayY = accessor(data[i], -1, d);
                    }
                    else {
                        data[i].name = d.metadata().name;
                        data[i].displayY = displayY;
                        data[i].relative = VZ.ChartHelpers.relativeAccessor(data[i], -1, d);
                        nanData.push(data[i]);
                    }
                }
                return nanData;
            };
            var nanData = _.flatten(this.datasets.map(datasetToNaNData));
            this.nanDataset.data(nanData);
        };
        LineChart.prototype.setupTooltips = function (plot) {
            var _this = this;
            var pi = new Plottable.Interactions.Pointer();
            pi.attachTo(plot);
            // PointsComponent is a Plottable Component that will hold the little
            // circles we draw over the closest data points
            var pointsComponent = new Plottable.Component();
            var group = new Plottable.Components.Group([plot, pointsComponent]);
            var hideTooltips = function () {
                _this.tooltip.style('opacity', 0);
                _this.scatterPlot.attr('opacity', 1);
                pointsComponent.content().selectAll('.point').remove();
            };
            var enabled = true;
            var disableTooltips = function () {
                enabled = false;
                hideTooltips();
            };
            var enableTooltips = function () { enabled = true; };
            this.dzl.interactionStart(disableTooltips);
            this.dzl.interactionEnd(enableTooltips);
            pi.onPointerMove(function (p) {
                if (!enabled) {
                    return;
                }
                var target = {
                    x: p.x,
                    y: p.y,
                    datum: null,
                    dataset: null,
                };
                var bbox = _this.gridlines.content().node().getBBox();
                // pts is the closets point to the tooltip for each dataset
                var pts = plot.datasets()
                    .map(function (dataset) { return _this.findClosestPoint(target, dataset); })
                    .filter(function (x) { return x != null; });
                var intersectsBBox = Plottable.Utils.DOM.intersectsBBox;
                // We draw tooltips for points that are NaN, or are currently visible
                var ptsForTooltips = pts.filter(function (p) { return intersectsBBox(p.x, p.y, bbox) || isNaN(p.datum.scalar); });
                // Only draw little indicator circles for the non-NaN points
                var ptsToCircle = ptsForTooltips.filter(function (p) { return !isNaN(p.datum.scalar); });
                var ptsSelection = pointsComponent.content().selectAll('.point').data(ptsToCircle, function (p) { return p.dataset.metadata().name; });
                if (pts.length !== 0) {
                    ptsSelection.enter().append('circle').classed('point', true);
                    ptsSelection.attr('r', VZ.ChartHelpers.TOOLTIP_CIRCLE_SIZE)
                        .attr('cx', function (p) { return p.x; })
                        .attr('cy', function (p) { return p.y; })
                        .style('stroke', 'none')
                        .attr('fill', function (p) { return _this.colorScale.scale(p.dataset.metadata().name); });
                    ptsSelection.exit().remove();
                    _this.drawTooltips(ptsForTooltips, target);
                }
                else {
                    hideTooltips();
                }
            });
            pi.onPointerExit(hideTooltips);
            return group;
        };
        LineChart.prototype.drawTooltips = function (points, target) {
            var _this = this;
            // Formatters for value, step, and wall_time
            this.scatterPlot.attr('opacity', 0);
            var valueFormatter = VZ.ChartHelpers.multiscaleFormatter(VZ.ChartHelpers.Y_TOOLTIP_FORMATTER_PRECISION);
            var dist = function (p) {
                return Math.pow(p.x - target.x, 2) + Math.pow(p.y - target.y, 2);
            };
            var closestDist = _.min(points.map(dist));
            var valueSortMethod = this.scalarAccessor;
            if (this.smoothingEnabled) {
                valueSortMethod = this.smoothedAccessor;
            }
            if (this.tooltipSortingMethod === 'ascending') {
                points =
                    _.sortBy(points, function (d) { return valueSortMethod(d.datum, -1, d.dataset); });
            }
            else if (this.tooltipSortingMethod === 'descending') {
                points =
                    _.sortBy(points, function (d) { return valueSortMethod(d.datum, -1, d.dataset); })
                        .reverse();
            }
            else if (this.tooltipSortingMethod === 'nearest') {
                points = _.sortBy(points, dist);
            }
            else {
                // The 'default' sorting method maintains the order of names passed to
                // setVisibleSeries(). However we reverse that order when defining the
                // datasets. So we must call reverse again to restore the order.
                points = points.slice(0).reverse();
            }
            var rows = this.tooltip.select('tbody')
                .html('')
                .selectAll('tr')
                .data(points)
                .enter()
                .append('tr');
            // Grey out the point if any of the following are true:
            // - The cursor is outside of the x-extent of the dataset
            // - The point's y value is NaN
            rows.classed('distant', function (d) {
                var firstPoint = d.dataset.data()[0];
                var lastPoint = _.last(d.dataset.data());
                var firstX = _this.xScale.scale(_this.xAccessor(firstPoint, 0, d.dataset));
                var lastX = _this.xScale.scale(_this.xAccessor(lastPoint, 0, d.dataset));
                var s = _this.smoothingEnabled ? d.datum.smoothed : d.datum.scalar;
                return target.x < firstX || target.x > lastX || isNaN(s);
            });
            rows.classed('closest', function (p) { return dist(p) === closestDist; });
            // It is a bit hacky that we are manually applying the width to the swatch
            // and the nowrap property to the text here. The reason is as follows:
            // the style gets updated asynchronously by Polymer scopeSubtree observer.
            // Which means we would get incorrect sizing information since the text
            // would wrap by default. However, we need correct measurements so that
            // we can stop the text from falling off the edge of the screen.
            // therefore, we apply the size-critical styles directly.
            rows.style('white-space', 'nowrap');
            rows.append('td')
                .append('span')
                .classed('swatch', true)
                .style('background-color', function (d) { return _this.colorScale.scale(d.dataset.metadata().name); });
            rows.append('td').text(function (d) { return d.dataset.metadata().name; });
            if (this.smoothingEnabled) {
                rows.append('td').text(function (d) { return isNaN(d.datum.smoothed) ? 'NaN' :
                    valueFormatter(d.datum.smoothed); });
            }
            rows.append('td').text(function (d) {
                return isNaN(d.datum.scalar) ? 'NaN' : valueFormatter(d.datum.scalar);
            });
            rows.append('td').text(function (d) { return VZ.ChartHelpers.stepFormatter(d.datum.step); });
            rows.append('td').text(function (d) { return VZ.ChartHelpers.timeFormatter(d.datum.wall_time); });
            rows.append('td').text(function (d) { return VZ.ChartHelpers.relativeFormatter(VZ.ChartHelpers.relativeAccessor(d.datum, -1, d.dataset)); });
            // compute left position
            var documentWidth = document.body.clientWidth;
            var node = this.tooltip.node();
            var parentRect = node.parentElement.getBoundingClientRect();
            var nodeRect = node.getBoundingClientRect();
            // prevent it from falling off the right side of the screen
            var left = documentWidth - parentRect.left - nodeRect.width - 60, top = 0;
            if (this.tooltipPosition === 'right') {
                left = Math.min(parentRect.width, left);
            }
            else {
                left = Math.min(0, left);
                top = parentRect.height + VZ.ChartHelpers.TOOLTIP_Y_PIXEL_OFFSET;
            }
            this.tooltip.style('transform', 'translate(' + left + 'px,' + top + 'px)');
            this.tooltip.style('opacity', 1);
        };
        LineChart.prototype.findClosestPoint = function (target, dataset) {
            var _this = this;
            var points = dataset.data().map(function (d, i) {
                var x = _this.xAccessor(d, i, dataset);
                var y = _this.smoothingEnabled ? _this.smoothedAccessor(d, i, dataset) :
                    _this.scalarAccessor(d, i, dataset);
                return {
                    x: _this.xScale.scale(x),
                    y: _this.yScale.scale(y),
                    datum: d,
                    dataset: dataset,
                };
            });
            var idx = _.sortedIndex(points, target, function (p) { return p.x; });
            if (idx === points.length) {
                return points[points.length - 1];
            }
            else if (idx === 0) {
                return points[0];
            }
            else {
                var prev = points[idx - 1];
                var next = points[idx];
                var prevDist = Math.abs(prev.x - target.x);
                var nextDist = Math.abs(next.x - target.x);
                return prevDist < nextDist ? prev : next;
            }
        };
        LineChart.prototype.resmoothDataset = function (dataset) {
            // When increasing the smoothing window, it smoothes a lot with the first
            // few points and then starts to gradually smooth slower, so using an
            // exponential function makes the slider more consistent. 1000^x has a
            // range of [1, 1000], so subtracting 1 and dividing by 999 results in a
            // range of [0, 1], which can be used as the percentage of the data, so
            // that the kernel size can be specified as a percentage instead of a
            // hardcoded number, what would be bad with multiple series.
            var factor = (Math.pow(1000, this.smoothingWeight) - 1) / 999;
            var data = dataset.data();
            var kernelRadius = Math.floor(data.length * factor / 2);
            data.forEach(function (d, i) {
                var actualKernelRadius = Math.min(kernelRadius, i, data.length - i - 1);
                var start = i - actualKernelRadius;
                var end = i + actualKernelRadius + 1;
                // Only smooth finite numbers.
                if (!_.isFinite(d.scalar)) {
                    d.smoothed = d.scalar;
                }
                else {
                    d.smoothed = d3.mean(data.slice(start, end).filter(function (d) { return _.isFinite(d.scalar); }), function (d) { return d.scalar; });
                }
            });
        };
        LineChart.prototype.getDataset = function (name) {
            if (this.name2datasets[name] === undefined) {
                this.name2datasets[name] = new Plottable.Dataset([], { name: name });
            }
            return this.name2datasets[name];
        };
        LineChart.getYScaleFromType = function (yScaleType) {
            if (yScaleType === 'log') {
                return new Plottable.Scales.ModifiedLog();
            }
            else if (yScaleType === 'linear') {
                return new Plottable.Scales.Linear();
            }
            else {
                throw new Error('Unrecognized yScale type ' + yScaleType);
            }
        };
        /**
         * Update the selected series on the chart.
         */
        LineChart.prototype.setVisibleSeries = function (names) {
            var _this = this;
            this.seriesNames = names;
            names.reverse(); // draw first series on top
            this.datasets.forEach(function (d) { return d.offUpdate(_this.onDatasetChanged); });
            this.datasets = names.map(function (r) { return _this.getDataset(r); });
            this.datasets.forEach(function (d) { return d.onUpdate(_this.onDatasetChanged); });
            this.linePlot.datasets(this.datasets);
            if (this.smoothingEnabled) {
                this.smoothLinePlot.datasets(this.datasets);
            }
            this.updateSpecialDatasets();
        };
        /**
         * Set the data of a series on the chart.
         */
        LineChart.prototype.setSeriesData = function (name, data) {
            this.getDataset(name).data(data);
        };
        LineChart.prototype.smoothingUpdate = function (weight) {
            var _this = this;
            this.smoothingWeight = weight;
            this.datasets.forEach(function (d) { return _this.resmoothDataset(d); });
            if (!this.smoothingEnabled) {
                this.linePlot.addClass('ghost');
                this.scatterPlot.y(this.smoothedAccessor, this.yScale);
                this.smoothingEnabled = true;
                this.smoothLinePlot.datasets(this.datasets);
            }
            this.updateSpecialDatasetsWithAccessor(this.smoothedAccessor);
        };
        LineChart.prototype.smoothingDisable = function () {
            if (this.smoothingEnabled) {
                this.linePlot.removeClass('ghost');
                this.scatterPlot.y(this.scalarAccessor, this.yScale);
                this.smoothLinePlot.datasets([]);
                this.smoothingEnabled = false;
                this.updateSpecialDatasetsWithAccessor(this.scalarAccessor);
            }
        };
        LineChart.prototype.setTooltipSortingMethod = function (method) {
            this.tooltipSortingMethod = method;
        };
        LineChart.prototype.setTooltipPosition = function (position) {
            this.tooltipPosition = position;
        };
        LineChart.prototype.renderTo = function (targetSVG) {
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
        LineChart.prototype.setViewBox = function () {
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
        LineChart.prototype.redraw = function () {
            this.outer.redraw();
            this.setViewBox();
        };
        LineChart.prototype.destroy = function () { this.outer.destroy(); };
        return LineChart;
    }());
    VZ.LineChart = LineChart;
})(VZ || (VZ = {}));
