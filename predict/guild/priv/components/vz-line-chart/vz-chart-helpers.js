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
    var ChartHelpers;
    (function (ChartHelpers) {
        ChartHelpers.Y_TOOLTIP_FORMATTER_PRECISION = 4;
        ChartHelpers.STEP_FORMATTER_PRECISION = 4;
        ChartHelpers.Y_AXIS_FORMATTER_PRECISION = 3;
        ChartHelpers.TOOLTIP_Y_PIXEL_OFFSET = 20;
        ChartHelpers.TOOLTIP_CIRCLE_SIZE = 4;
        ChartHelpers.NAN_SYMBOL_SIZE = 6;
        /* Create a formatter function that will switch between exponential and
         * regular display depending on the scale of the number being formatted,
         * and show `digits` significant digits.
         */
        function multiscaleFormatter(digits) {
            return function (v) {
                var absv = Math.abs(v);
                if (absv < 1E-15) {
                    // Sometimes zero-like values get an annoying representation
                    absv = 0;
                }
                var f;
                if (absv >= 1E4) {
                    f = d3.format('.' + digits + 'e');
                }
                else if (absv > 0 && absv < 0.01) {
                    f = d3.format('.' + digits + 'e');
                }
                else {
                    f = d3.format('.' + digits + 'g');
                }
                return f(v);
            };
        }
        ChartHelpers.multiscaleFormatter = multiscaleFormatter;
        function accessorize(key) {
            return function (d, index, dataset) { return d[key]; };
        }
        ChartHelpers.accessorize = accessorize;
        ChartHelpers.stepFormatter = Plottable.Formatters.siSuffix(ChartHelpers.STEP_FORMATTER_PRECISION);
        function stepX() {
            var scale = new Plottable.Scales.Linear();
            var axis = new Plottable.Axes.Numeric(scale, 'bottom');
            axis.formatter(ChartHelpers.stepFormatter);
            return {
                scale: scale,
                axis: axis,
                accessor: function (d) { return d.step; },
            };
        }
        ChartHelpers.stepX = stepX;
        ChartHelpers.timeFormatter = Plottable.Formatters.time('%a %b %e, %H:%M:%S');
        function wallX() {
            var scale = new Plottable.Scales.Time();
            return {
                scale: scale,
                axis: new Plottable.Axes.Time(scale, 'bottom'),
                accessor: function (d) { return d.wall_time; },
            };
        }
        ChartHelpers.wallX = wallX;
        ChartHelpers.relativeAccessor = function (d, index, dataset) {
            // We may be rendering the final-point datum for scatterplot.
            // If so, we will have already provided the 'relative' property
            if (d.relative != null) {
                return d.relative;
            }
            var data = dataset.data();
            // I can't imagine how this function would be called when the data is
            // empty (after all, it iterates over the data), but lets guard just
            // to be safe.
            var first = data.length > 0 ? +data[0].wall_time : 0;
            return (+d.wall_time - first) / (60 * 60 * 1000); // ms to hours
        };
        ChartHelpers.relativeFormatter = function (n) {
            // we will always show 2 units of precision, e.g days and hours, or
            // minutes and seconds, but not hours and minutes and seconds
            var ret = '';
            var days = Math.floor(n / 24);
            n -= (days * 24);
            if (days) {
                ret += days + 'd ';
            }
            var hours = Math.floor(n);
            n -= hours;
            n *= 60;
            if (hours || days) {
                ret += hours + 'h ';
            }
            var minutes = Math.floor(n);
            n -= minutes;
            n *= 60;
            if (minutes || hours || days) {
                ret += minutes + 'm ';
            }
            var seconds = Math.floor(n);
            return ret + seconds + 's';
        };
        function relativeX() {
            var scale = new Plottable.Scales.Linear();
            return {
                scale: scale,
                axis: new Plottable.Axes.Numeric(scale, 'bottom'),
                accessor: ChartHelpers.relativeAccessor,
            };
        }
        ChartHelpers.relativeX = relativeX;
        // a very literal definition of NaN: true for NaN for a non-number type
        // or null, etc. False for Infinity or -Infinity
        ChartHelpers.isNaN = function (x) { return +x !== x; };
        function getXComponents(xType) {
            switch (xType) {
                case 'step':
                    return stepX();
                case 'wall_time':
                    return wallX();
                case 'relative':
                    return relativeX();
                default:
                    throw new Error('invalid xType: ' + xType);
            }
        }
        ChartHelpers.getXComponents = getXComponents;
    })(ChartHelpers = VZ.ChartHelpers || (VZ.ChartHelpers = {}));
})(VZ || (VZ = {}));
