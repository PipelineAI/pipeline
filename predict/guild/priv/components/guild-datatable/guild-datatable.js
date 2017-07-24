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

Guild.DataTable = new function() {

    var DEFAULT_HEIGHT = "360px";

    var init = function(table, columns, options) {
        return jQuery(table).DataTable({
            data: [],
            columns: columns,
            order: tableOrderOption(columns),
            scrollY: (options && options.height) || DEFAULT_HEIGHT,
            scrollCollapse: true,
            paging: false,
            deferRender: true,
            language: tableLanguageOption(options),
            dom: "<'row'<'col-12'f>>"
                + "<'row'<'col-12'tr>>"
                + "<'row'<'col-12'i>>"
        });
    };

    var tableOrderOption = function(columns) {
        var order = [];
        columns.forEach(function(col, index) {
            var node = col.node;
            var pos = node.sortOrder || 0;
            if (node.sortAsc) {
                order[pos] = [index, "asc"];
            } else if (node.sortDesc) {
                order[pos] = [index, "desc"];
            }
        });
        return order.length > 0 ? order : undefined;
    };

    var tableLanguageOption = function(options) {
        var itemNamePlural = (options && options.itemNamePlural) || "items";
        return {
            info: "_TOTAL_ " + itemNamePlural,
            infoFiltered: " (filtered from _MAX_)",
            infoEmpty: "_TOTAL_ " + itemNamePlural,
            search: "",
            zeroRecords: "Waiting for data"
        };
    };

    var addRows = function(dt, rows) {
        var added = dt.rows.add(rows);
        added.draw(false);
    };

    var defaultColTitle = function(title) {
        return "<span class='header-title'>" + title + "</span>";
    };

    var templateRenderer = function(templatizer, renderType) {
        return function(value, type, data) {
            if (type == renderType) {
                var instance = templatizer.stamp(data);
                return fragmentHtml(instance.root);
            } else {
                return value;
            }
        };
    };

    var fragmentHtml = function(fragment) {
        var val = "";
        fragment.childNodes.forEach(function(node) {
            val += node.outerHTML || node.textContent;
        });
        return val;
    };

    this.init = init;
    this.addRows = addRows;
    this.defaultColTitle = defaultColTitle;
    this.templateRenderer = templateRenderer;
};
