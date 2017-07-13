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
// Example usage:
// runs = ["train", "test", "test1", "test2"]
// ccs = new TF.ColorScale();
// ccs.domain(runs);
// ccs.getColor("train");
// ccs.getColor("test1");
var TF;
(function (TF) {
    var ColorScale = (function () {
        /**
         * Creates a color scale with optional custom palette.
         *  @param {string[]} [palette=TF.palettes.googleColorBlind] - The color
         *                 palette you want as an Array of hex strings.
         */
        function ColorScale(palette) {
            if (palette === void 0) { palette = TF.palettes.googleColorBlindAssist; }
            this.identifiers = d3.map();
            this.palette = palette;
        }
        /**
         * Set the domain of strings.
         * @param {string[]} strings - An array of possible strings to use as the
         *                             domain for your scale.
         */
        ColorScale.prototype.domain = function (strings) {
            var _this = this;
            this.identifiers = d3.map();
            strings.forEach(function (s, i) {
                _this.identifiers.set(s, _this.palette[i % _this.palette.length]);
            });
            return this;
        };
        /**
         * Use the color scale to transform an element in the domain into a color.
         * @param {string} The input string to map to a color.
         * @return {string} The color corresponding to that input string.
         * @throws Will error if input string is not in the scale's domain.
         */
        ColorScale.prototype.scale = function (s) {
            if (!this.identifiers.has(s)) {
                throw new Error('String was not in the domain.');
            }
            return this.identifiers.get(s);
        };
        return ColorScale;
    }());
    TF.ColorScale = ColorScale;
})(TF || (TF = {}));
