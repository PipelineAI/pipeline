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

Guild.Run = new function() {

    var runLabel = function(run) {
        var label = "";
        if (run.started) {
            var started = new Date(run.started);
            label += Guild.Util.formatShortDate(started);
        }
        if (run.model) {
            if (label) {
                label += " - ";
            }
            label += run.model;
        }
        return label;
    };

    var runStatusRules = [
        [["running"],
         {label: "Running",
          iconClass: "icon running",
          icon:  "fa:circle-o-notch",
          spin: true}],
        [["stopped", 0],
         {label: "Completed",
          iconClass: "icon completed",
          icon:  "check-circle"}],
        [["stopped"],
         {label: "Error",
          iconClass: "icon error",
          icon:  "error"}],
        [["crashed"],
         {label: "Terminated",
          iconClass: "icon crashed",
          icon:  "cancel"}],
        [[],
         {label: "--",
          iconClass: "icon unknown",
          icon:  "help"}]
    ];

    var runStatus = function(run) {
        for (var i in runStatusRules) {
            var rule = runStatusRules[i][0];
            var parts = rule.length;
            if ((parts == 0)
                || (parts == 1
                    && rule[0] == run.status)
                || (parts == 2
                    && rule[0] == run.status
                    && rule[1] == run.exit_status))
            {
                return runStatusRules[i][1];
            }
        }
        throw "unreachable"; // rules must have catch-all
    };

    var runForId = function(runId, runs) {
        return runs.find(function(run) {
            return run.id == runId;
        });
    };

    var isRunning = function(run) {
        return run && run.status == "running";
    };

    this.runLabel = runLabel;
    this.runStatus = runStatus;
    this.runForId = runForId;
    this.isRunning = isRunning;
};
