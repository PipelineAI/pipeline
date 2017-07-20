/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the 'License');
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an 'AS IS' BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
var Categorizer;
(function (Categorizer) {
    var assert = chai.assert;
    describe('categorizer', function () {
        describe('topLevelNamespaceCategorizer', function () {
            it('returns empty array on empty tags', function () {
                assert.lengthOf(Categorizer.topLevelNamespaceCategorizer([]), 0);
            });
            it('handles a simple case', function () {
                var simple = [
                    'foo1/bar', 'foo1/zod', 'foo2/bar', 'foo2/zod', 'gosh/lod/mar',
                    'gosh/lod/ned'
                ];
                var expected = [
                    { name: 'foo1', tags: ['foo1/bar', 'foo1/zod'] },
                    { name: 'foo2', tags: ['foo2/bar', 'foo2/zod'] },
                    { name: 'gosh', tags: ['gosh/lod/mar', 'gosh/lod/ned'] },
                ];
                assert.deepEqual(Categorizer.topLevelNamespaceCategorizer(simple), expected);
            });
            it('orders the categories', function () {
                var test = ['e', 'f', 'g', 'a', 'b', 'c'];
                var expected = [
                    { name: 'a', tags: ['a'] },
                    { name: 'b', tags: ['b'] },
                    { name: 'c', tags: ['c'] },
                    { name: 'e', tags: ['e'] },
                    { name: 'f', tags: ['f'] },
                    { name: 'g', tags: ['g'] },
                ];
                assert.deepEqual(Categorizer.topLevelNamespaceCategorizer(test), expected);
            });
            it('handles cases where category names overlap node names', function () {
                var test = ['a', 'a/a', 'a/b', 'a/c', 'b', 'b/a'];
                var actual = Categorizer.topLevelNamespaceCategorizer(test);
                var expected = [
                    { name: 'a', tags: ['a', 'a/a', 'a/b', 'a/c'] },
                    { name: 'b', tags: ['b', 'b/a'] },
                ];
                assert.deepEqual(actual, expected);
            });
            it('handles singleton case', function () {
                assert.deepEqual(Categorizer.topLevelNamespaceCategorizer(['a']), [{ name: 'a', tags: ['a'] }]);
            });
        });
        describe('legacyUnderscoreCategorizer', function () {
            it('splits by shorter of first _ or /', function () {
                var tags = [
                    'l0_bar/foo', 'l0_bar/baz', 'l0_foo/wob', 'l1_zoink/bla',
                    'l1_wibble/woz', 'l1/foo_woink', 'l2/wozzle_wizzle'
                ];
                var actual = Categorizer.legacyUnderscoreCategorizer(tags);
                var expected = [
                    { name: 'l0', tags: ['l0_bar/baz', 'l0_bar/foo', 'l0_foo/wob'] },
                    { name: 'l1', tags: ['l1/foo_woink', 'l1_wibble/woz', 'l1_zoink/bla'] },
                    { name: 'l2', tags: ['l2/wozzle_wizzle'] },
                ];
                assert.deepEqual(actual, expected);
            });
        });
        describe('customCategorizer', function () {
            function noFallbackCategorizer(tags) {
                return [];
            }
            function testCategorizer(defs, fallback, tags) {
                var catDefs = defs.map(Categorizer.defineCategory);
                return Categorizer._categorizer(catDefs, fallback)(tags);
            }
            it('categorizes by regular expression', function () {
                var defs = ['foo..', 'bar..'];
                var tags = ['fooab', 'fooxa', 'barts', 'barms'];
                var actual = testCategorizer(defs, noFallbackCategorizer, tags);
                var expected = [
                    { name: 'foo..', tags: ['fooab', 'fooxa'] },
                    { name: 'bar..', tags: ['barms', 'barts'] },
                ];
                assert.deepEqual(actual, expected);
            });
            it('matches non-exclusively', function () {
                var tags = ['abc', 'bar', 'zod'];
                var actual = testCategorizer(['...', 'bar'], noFallbackCategorizer, tags);
                var expected = [
                    { name: '...', tags: ['abc', 'bar', 'zod'] },
                    { name: 'bar', tags: ['bar'] },
                ];
                assert.deepEqual(actual, expected);
            });
            it('creates categories for unmatched rules', function () {
                var actual = testCategorizer(['a', 'b', 'c'], noFallbackCategorizer, []);
                var expected = [
                    { name: 'a', tags: [] },
                    { name: 'b', tags: [] },
                    { name: 'c', tags: [] },
                ];
                assert.deepEqual(actual, expected);
            });
            it('category regexs work with special characters', function () {
                var defs = ['^\\w+$', '^\\d+$', '^\\/..$'];
                var tags = ['foo', '3243', '/xa'];
                var actual = testCategorizer(defs, noFallbackCategorizer, tags);
                var expected = [
                    { name: '^\\w+$', tags: ['3243', 'foo'] },
                    { name: '^\\d+$', tags: ['3243'] },
                    { name: '^\\/..$', tags: ['/xa'] },
                ];
                assert.deepEqual(actual, expected);
            });
            it('category tags are sorted', function () {
                var tags = ['a', 'z', 'c', 'd', 'e', 'x', 'f', 'y', 'g'];
                var sorted = tags.slice().sort();
                var expected = [{ name: '.*', tags: sorted }];
                var actual = testCategorizer(['.*'], noFallbackCategorizer, tags);
                assert.deepEqual(actual, expected);
            });
            it('if nonexclusive: all tags passed to fallback', function () {
                var passedToDefault = null;
                function defaultCategorizer(tags) {
                    passedToDefault = tags;
                    return [];
                }
                var tags = ['foo', 'bar', 'foo123'];
                testCategorizer(['foo'], defaultCategorizer, tags);
                assert.deepEqual(passedToDefault, tags);
            });
        });
    });
})(Categorizer || (Categorizer = {}));
