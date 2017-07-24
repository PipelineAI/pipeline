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

Guild.JSONDiffPatch = new function() {

    var KVFmt = function KVFmt() {};

    KVFmt.prototype = new jsondiffpatch.formatters.base.BaseFormatter();

    KVFmt.prototype.prepareContext = function(context) {
        context.rows = [];
        context.unchanged = function(val) {
            this.rows.push({
                key: this.key,
                type: "unchanged",
                left: val,
                right: val
            });
        };
        context.added = function(val) {
            this.rows.push({
                key: this.key,
                type: "added",
                left: null,
                right: val
            });
        };
        context.modified = function(left, right) {
            this.rows.push({
                key: this.key,
                type: "modified",
                left: left,
                right: right
            });
        };
        context.deleted = function(val) {
            this.rows.push({
                key: this.key,
                type: "deleted",
                left: val,
                right: null
            });
        };
    };

    KVFmt.prototype.finalize = function(context) {
        return context.rows;
    };

    KVFmt.prototype.rootBegin = function(context, type, nodeType) {
        // pass
    };

    KVFmt.prototype.rootEnd = function(context) {
        // pass
    };

    KVFmt.prototype.nodeBegin = function(context, key, leftKey, type, nodeType) {
        context.key = key;
    };

    KVFmt.prototype.nodeEnd = function(context) {
        // pass
    };

    KVFmt.prototype.format_node = function(context, delta, left) {
        this.formatDeltaChildren(context, delta, left);
    };

    KVFmt.prototype.format_unchanged = function(context, delta, left) {
        context.unchanged(left);
    };

    KVFmt.prototype.format_movedestination = function(context, delta, left) {
        throw ["format_movedestination", delta, left];
    };

    KVFmt.prototype.format_added = function(context, delta) {
        context.added(delta[0]);
    };

    KVFmt.prototype.format_modified = function(context, delta) {
        context.modified(delta[0], delta[1]);
    };

    KVFmt.prototype.format_deleted = function(context, delta) {
        context.deleted(delta[0]);
    };

    KVFmt.prototype.format_moved = function(context, delta) {
        throw ["format_moved", delta, left];
    };

    KVFmt.prototype.format_textdiff = function(context, delta) {
        throw ["format_textdiff", delta];
    };

    var inst = jsondiffpatch.create({
        textDiff: {
            minLength: Number.MAX_SAFE_INTEGER
        }
    });

    var diff = function(left, right) {
        return inst.diff(left, right);
    };

    var kvfmt = new KVFmt();

    var formatKeyvalDelta = function(delta, left) {
        return delta
            ? kvfmt.format(delta, left)
            : formatKeyvalUnchanged(left);
    };

    var formatKeyvalUnchanged = function(kvs) {
        var keys = Object.keys(kvs);
        return keys.map(function(key) {
            var val = kvs[key];
            return {
                key: key,
                type: "unchanged",
                left: val,
                right: val
            };
        });
    };

    this.diff = diff;
    this.formatKeyvalDelta = formatKeyvalDelta;
};
