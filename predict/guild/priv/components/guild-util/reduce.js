/* Copyright 2016-2017 TensorHub, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var Guild = Guild || {};

Guild.Reduce = new function() {

    var last_5_average = function(data) {
        return mapSeries(function(series) {
            return seriesAverage(series.slice(-5));
        }, data);
    };

    var average = function(data) {
        return mapSeries(function(series) {
            return seriesAverage(series);
        }, data);
    };

    var last = function(data) {
        return mapSeries(function(series) {
            return seriesLast(series);
        }, data);
    };

    var steps = function(data) {
        return mapSeries(function(series) {
            return seriesSteps(series);
        }, data);
    };

    var duration = function(data) {
        return mapSeries(function(series) {
            return seriesDuration(series);
        }, data);
    };

    var mapSeries = function(f, data) {
        var result = {};
        for (var name in data) {
            var series = data[name];
            result[name] = series ? f(series) : undefined;
        }
        return result;
    };

    var seriesAverage = function(series) {
        if (!series) {
            return undefined;
        }
        var total = 0;
        for(var i in series) {
            total += series[i][2];
        }
        return total / series.length;
    };

    var seriesLast = function(series) {
        if (!series) {
            return undefined;
        }
        return series[series.length - 1][2];
    };

    var seriesSteps = function(series) {
        if (!series) {
            return 0;
        }
        return series[series.length - 1][1];
    };

    var seriesSteps0 = function(series) {
        if (!series || series.length < 2) {
            return 0;
        }
        return series[series.length - 1][1] + 1;
    };

    var seriesDuration = function(series) {
        if (!series || series.length < 2) {
            return null;
        }
        return (series[series.length - 1][0] - series[0][0]) / 1000;
    };

    this.last_5_average = last_5_average;
    this.average = average;
    this.last = last;
    this.steps = steps;
    this.duration = duration;
};
