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
/**
 * The Storage Module provides storage for URL parameters, and an API for
 * getting and setting TensorBoard's stateful URI.
 *
 * It generates URI components like: events&runPrefix=train*
 * which TensorBoard uses after like localhost:8000/#events&runPrefix=train*
 * to store state in the URI.
 */
var TF;
(function (TF) {
    var URIStorage;
    (function (URIStorage) {
        /**
         * A key that users cannot use, since TensorBoard uses this to store info
         * about the active tab.
         */
        URIStorage.TAB = '__tab__';
        /**
         * The name of the property for users to set on a Polymer component
         * in order for its stored properties to be stored in the URI unambiguously.
         * (No need to set this if you want mutliple instances of the component to
         * share URI state)
         *
         * Example:
         * <my-component disambiguator="0"></my-component>
         *
         * The disambiguator should be set to any unique value so that multiple
         * instances of the component can store properties in URI storage.
         *
         * Because it's hard to dereference this variable in HTML property bindings,
         * it is NOT safe to change the disambiguator string without find+replace
         * across the codebase.
         */
        URIStorage.DISAMBIGUATOR = 'disambiguator';
        /**
         * Return a boolean stored in the URI, given a corresponding key.
         * Undefined if not found.
         */
        function getBoolean(key) {
            var items = _componentToDict(_readComponent());
            var item = items[key];
            return item === 'true' ? true : item === 'false' ? false : undefined;
        }
        URIStorage.getBoolean = getBoolean;
        /**
         * Store a boolean in the URI, with a corresponding key.
         */
        function setBoolean(key, value) {
            var items = _componentToDict(_readComponent());
            items[key] = value.toString();
            _writeComponent(_dictToComponent(items));
        }
        URIStorage.setBoolean = setBoolean;
        /**
         * Return a string stored in the URI, given a corresponding key.
         * Undefined if not found.
         */
        function getString(key) {
            var items = _componentToDict(_readComponent());
            return items[key];
        }
        URIStorage.getString = getString;
        /**
         * Store a string in the URI, with a corresponding key.
         */
        function setString(key, value) {
            var items = _componentToDict(_readComponent());
            items[key] = value;
            _writeComponent(_dictToComponent(items));
        }
        URIStorage.setString = setString;
        /**
         * Return a number stored in the URI, given a corresponding key.
         * Undefined if not found.
         */
        function getNumber(key) {
            var items = _componentToDict(_readComponent());
            return items[key] === undefined ? undefined : +items[key];
        }
        URIStorage.getNumber = getNumber;
        /**
         * Store a number in the URI, with a corresponding key.
         */
        function setNumber(key, value) {
            var items = _componentToDict(_readComponent());
            items[key] = '' + value;
            _writeComponent(_dictToComponent(items));
        }
        URIStorage.setNumber = setNumber;
        /**
         * Return an object stored in the URI, given a corresponding key.
         * Undefined if not found.
         */
        function getObject(key) {
            var items = _componentToDict(_readComponent());
            return items[key] === undefined ? undefined : JSON.parse(atob(items[key]));
        }
        URIStorage.getObject = getObject;
        /**
         * Store an object in the URI, with a corresponding key.
         */
        function setObject(key, value) {
            var items = _componentToDict(_readComponent());
            items[key] = btoa(JSON.stringify(value));
            _writeComponent(_dictToComponent(items));
        }
        URIStorage.setObject = setObject;
        /**
         * Get a unique storage name for a (Polymer component, propertyName) tuple.
         *
         * DISAMBIGUATOR must be set on the component, if other components use the
         * same propertyName.
         */
        function getURIStorageName(component, propertyName) {
            var d = component[URIStorage.DISAMBIGUATOR];
            var components = d == null ? [propertyName] : [d, propertyName];
            return components.join('.');
        }
        URIStorage.getURIStorageName = getURIStorageName;
        /**
         * Return a function that:
         * (1) Initializes a Polymer boolean property with a default value, if its
         *     value is not already set
         * (2) Sets up listener that updates Polymer property on hash change.
         */
        function getBooleanInitializer(propertyName, defaultVal) {
            return _getInitializer(getBoolean, propertyName, defaultVal);
        }
        URIStorage.getBooleanInitializer = getBooleanInitializer;
        /**
         * Return a function that:
         * (1) Initializes a Polymer string property with a default value, if its
         *     value is not already set
         * (2) Sets up listener that updates Polymer property on hash change.
         */
        function getStringInitializer(propertyName, defaultVal) {
            return _getInitializer(getString, propertyName, defaultVal);
        }
        URIStorage.getStringInitializer = getStringInitializer;
        /**
         * Return a function that:
         * (1) Initializes a Polymer number property with a default value, if its
         *     value is not already set
         * (2) Sets up listener that updates Polymer property on hash change.
         */
        function getNumberInitializer(propertyName, defaultVal) {
            return _getInitializer(getNumber, propertyName, defaultVal);
        }
        URIStorage.getNumberInitializer = getNumberInitializer;
        /**
         * Return a function that:
         * (1) Initializes a Polymer Object property with a default value, if its
         *     value is not already set
         * (2) Sets up listener that updates Polymer property on hash change.
         *
         * Generates a deep clone of the defaultVal to avoid mutation issues.
         */
        function getObjectInitializer(propertyName, defaultVal) {
            return _getInitializer(getObject, propertyName, defaultVal);
        }
        URIStorage.getObjectInitializer = getObjectInitializer;
        /**
         * Return a function that updates URIStorage when a string property changes.
         */
        function getBooleanObserver(propertyName, defaultVal) {
            return _getObserver(getBoolean, setBoolean, propertyName, defaultVal);
        }
        URIStorage.getBooleanObserver = getBooleanObserver;
        /**
         * Return a function that updates URIStorage when a string property changes.
         */
        function getStringObserver(propertyName, defaultVal) {
            return _getObserver(getString, setString, propertyName, defaultVal);
        }
        URIStorage.getStringObserver = getStringObserver;
        /**
         * Return a function that updates URIStorage when a number property changes.
         */
        function getNumberObserver(propertyName, defaultVal) {
            return _getObserver(getNumber, setNumber, propertyName, defaultVal);
        }
        URIStorage.getNumberObserver = getNumberObserver;
        /**
         * Return a function that updates URIStorage when an object property changes.
         * Generates a deep clone of the defaultVal to avoid mutation issues.
         */
        function getObjectObserver(propertyName, defaultVal) {
            var clone = _.cloneDeep(defaultVal);
            return _getObserver(getObject, setObject, propertyName, clone);
        }
        URIStorage.getObjectObserver = getObjectObserver;
        /**
         * Read component from URI (e.g. returns "events&runPrefix=train*").
         */
        function _readComponent() {
            return TF.Globals.USE_HASH ? window.location.hash.slice(1) :
                TF.Globals.FAKE_HASH;
        }
        /**
         * Write component to URI.
         */
        function _writeComponent(component) {
            if (TF.Globals.USE_HASH) {
                window.location.hash = component;
            }
            else {
                TF.Globals.FAKE_HASH = component;
            }
        }
        /**
         * Convert dictionary of strings into a URI Component.
         * All key value entries get added as key value pairs in the component,
         * with the exception of a key with the TAB value, which if present
         * gets prepended to the URI Component string for backwards comptability
         * reasons.
         */
        function _dictToComponent(items) {
            var component = '';
            // Add the tab name e.g. 'events', 'images', 'histograms' as a prefix
            // for backwards compatbility.
            if (items[URIStorage.TAB] !== undefined) {
                component += items[URIStorage.TAB];
            }
            // Join other strings with &key=value notation
            var nonTab = _.pairs(items)
                .filter(function (pair) { return pair[0] !== URIStorage.TAB; })
                .map(function (pair) {
                return encodeURIComponent(pair[0]) + '=' +
                    encodeURIComponent(pair[1]);
            })
                .join('&');
            return nonTab.length > 0 ? (component + '&' + nonTab) : component;
        }
        /**
         * Convert a URI Component into a dictionary of strings.
         * Component should consist of key-value pairs joined by a delimiter
         * with the exception of the tabName.
         * Returns dict consisting of all key-value pairs and
         * dict[TAB] = tabName
         */
        function _componentToDict(component) {
            var items = {};
            var tokens = component.split('&');
            tokens.forEach(function (token) {
                var kv = token.split('=');
                // Special backwards compatibility for URI components like #events
                if (kv.length === 1 && _.contains(TF.Globals.TABS, kv[0])) {
                    items[URIStorage.TAB] = kv[0];
                }
                else if (kv.length === 2) {
                    items[decodeURIComponent(kv[0])] = decodeURIComponent(kv[1]);
                }
            });
            return items;
        }
        /**
         * Return a function that:
         * (1) Initializes a Polymer property with a default value, if its
         *     value is not already set
         * (2) Sets up listener that updates Polymer property on hash change.
         */
        function _getInitializer(get, propertyName, defaultVal) {
            return function () {
                var _this = this;
                var URIStorageName = getURIStorageName(this, propertyName);
                // setComponentValue will be called every time the hash changes, and is
                // responsible for ensuring that new state in the hash will be propagated
                // to the component with that property.
                // It is important that this function does not re-assign needlessly,
                // to avoid Polymer observer churn.
                var setComponentValue = function () {
                    var uriValue = get(URIStorageName);
                    var currentValue = _this[propertyName];
                    // if uriValue is undefined, we will ensure that the property has the
                    // default value
                    if (uriValue === undefined) {
                        if (!_.isEqual(currentValue, defaultVal)) {
                            // If we don't have an explicit URI value, then we need to ensure
                            // the property value is equal to the default value.
                            // We will assign a clone rather than the canonical default, because
                            // the component receiving this property may mutate it, and we need
                            // to keep a pristine copy of the default.
                            _this[propertyName] = _.clone(defaultVal);
                        }
                    }
                    else if (!_.isEqual(uriValue, currentValue)) {
                        _this[propertyName] = uriValue;
                    }
                };
                // Set the value on the property.
                setComponentValue();
                // Update it when the hashchanges.
                window.addEventListener('hashchange', setComponentValue);
            };
        }
        /**
         * Return a function that updates URIStorage when a property changes.
         */
        function _getObserver(get, set, propertyName, defaultVal) {
            return function () {
                var URIStorageName = getURIStorageName(this, propertyName);
                var newVal = this[propertyName];
                if (!_.isEqual(newVal, get(URIStorageName))) {
                    if (_.isEqual(newVal, defaultVal)) {
                        _unset(URIStorageName);
                    }
                    else {
                        set(URIStorageName, newVal);
                    }
                }
            };
        }
        /**
         * Delete a key from the URI.
         */
        function _unset(key) {
            var items = _componentToDict(_readComponent());
            delete items[key];
            _writeComponent(_dictToComponent(items));
        }
    })(URIStorage = TF.URIStorage || (TF.URIStorage = {}));
})(TF || (TF = {}));
