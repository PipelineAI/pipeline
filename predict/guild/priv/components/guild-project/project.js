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

Guild.Project = new function() {

    this.namedSection = function(project, type, name) {
        return project[type + "\t" + name];
    };

    this.namedSectionOrDefault = function(project, type, name) {
        return project[type + "\t" + name] || project[type];
    };

    this.orderedSections = function(project, type) {
        var sections = [];
        project.__meta__.sectionOrder.forEach(function(key) {
            var keyParts = key.split("\t");
            if (keyParts[0] == type) {
                sections.push(project[key]);
            }
        });
        return sections;
    };

    this.orderedNamedSections = function(project, type, name) {
        var sections = [];
        project.__meta__.sectionOrder.forEach(function(key) {
            var keyParts = key.split("\t");
            if (keyParts[0] == type
                && (keyParts.length == 2
                    || keyParts[1] == name)) {
                sections.push(project[key]);
            }
        });
        return sections;
    };
};
