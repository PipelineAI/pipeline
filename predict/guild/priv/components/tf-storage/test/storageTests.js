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
// tslint:disable-next-line:no-var-keyword
var assert = chai.assert;
/* tslint:disable:no-namespace */
var TF;
(function (TF) {
    var URIStorage;
    (function (URIStorage) {
        describe('URIStorage', function () {
            it('get/setString', function () {
                URIStorage.setString('key_a', 'hello');
                URIStorage.setString('key_b', 'there');
                assert.equal('hello', URIStorage.getString('key_a'));
                assert.equal('there', URIStorage.getString('key_b'));
                assert.equal(null, URIStorage.getString('key_c'));
            });
            it('get/setNumber', function () {
                URIStorage.setNumber('key_a', 12);
                URIStorage.setNumber('key_b', 3.4);
                assert.equal(12, URIStorage.getNumber('key_a'));
                assert.equal(3.4, URIStorage.getNumber('key_b'));
                assert.equal(null, URIStorage.getNumber('key_c'));
            });
            it('get/setObject', function () {
                var obj = { 'foo': 2.3, 'bar': 'barstr' };
                URIStorage.setObject('key_a', obj);
                assert.deepEqual(obj, URIStorage.getObject('key_a'));
            });
            it('get/setWeirdValues', function () {
                URIStorage.setNumber('key_a', NaN);
                assert.deepEqual(NaN, URIStorage.getNumber('key_a'));
                URIStorage.setNumber('key_a', +Infinity);
                assert.equal(+Infinity, URIStorage.getNumber('key_a'));
                URIStorage.setNumber('key_a', -Infinity);
                assert.equal(-Infinity, URIStorage.getNumber('key_a'));
                URIStorage.setNumber('key_a', 1 / 3);
                assert.equal(1 / 3, URIStorage.getNumber('key_a'));
                URIStorage.setNumber('key_a', -0);
                assert.equal(-0, URIStorage.getNumber('key_a'));
            });
            it('set/getTab', function () {
                URIStorage.setString(URIStorage.TAB, TF.Globals.TABS[0]);
                assert.equal(TF.Globals.TABS[0], URIStorage.getString(URIStorage.TAB));
            });
        });
    })(URIStorage = TF.URIStorage || (TF.URIStorage = {}));
})(TF || (TF = {}));
