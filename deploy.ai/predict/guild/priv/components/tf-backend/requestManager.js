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
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var TF;
(function (TF) {
    var Backend;
    (function (Backend) {
        /**
         * Manages many fetch requests. Launches up to nSimultaneousRequests
         * simultaneously, and maintains a LIFO queue of requests to process when
         * more urls are requested than can be handled at once. The queue can be
         * cleared.
         *
         * When a request is made, a Promise is returned which resolves with the
         * parsed JSON result from the request.
         */
        var RequestCancellationError = (function (_super) {
            __extends(RequestCancellationError, _super);
            function RequestCancellationError() {
                _super.apply(this, arguments);
                this.name = 'RequestCancellationError';
            }
            return RequestCancellationError;
        }(Error));
        Backend.RequestCancellationError = RequestCancellationError;
        var RequestNetworkError = (function (_super) {
            __extends(RequestNetworkError, _super);
            function RequestNetworkError(req, url) {
                _super.call(this);
                this.message = "RequestNetworkError: " + req.status + " at " + url;
                this.name = 'RequestNetworkError';
                this.req = req;
                this.url = url;
            }
            return RequestNetworkError;
        }(Error));
        Backend.RequestNetworkError = RequestNetworkError;
        var RequestManager = (function () {
            function RequestManager(nSimultaneousRequests, maxRetries) {
                if (nSimultaneousRequests === void 0) { nSimultaneousRequests = 10; }
                if (maxRetries === void 0) { maxRetries = 3; }
                this._queue = [];
                this._nActiveRequests = 0;
                this._nSimultaneousRequests = nSimultaneousRequests;
                this._maxRetries = maxRetries;
            }
            /* Gives a promise that loads assets from given url (respects queuing) */
            RequestManager.prototype.request = function (url) {
                var _this = this;
                var promise = new Promise(function (resolve, reject) {
                    var resolver = { resolve: resolve, reject: reject };
                    _this._queue.push(resolver);
                    _this.launchRequests();
                })
                    .then(function () {
                    return _this.promiseWithRetries(url, _this._maxRetries);
                })
                    .then(function (response) {
                    // Success - Let's free space for another active
                    // reqest, and launch it
                    _this._nActiveRequests--;
                    _this.launchRequests();
                    return response;
                }, function (rejection) {
                    if (rejection.name === 'RequestNetworkError') {
                        // If we failed due to network error, we should
                        // decrement
                        // _nActiveRequests because this request was
                        // active
                        _this._nActiveRequests--;
                        _this.launchRequests();
                    }
                    return Promise.reject(rejection);
                });
                return promise;
            };
            RequestManager.prototype.clearQueue = function () {
                while (this._queue.length > 0) {
                    this._queue.pop().reject(new RequestCancellationError('Request cancelled by clearQueue'));
                }
            };
            /* Return number of currently pending requests */
            RequestManager.prototype.activeRequests = function () {
                return this._nActiveRequests;
            };
            /* Return total number of outstanding requests (includes queue) */
            RequestManager.prototype.outstandingRequests = function () {
                return this._nActiveRequests + this._queue.length;
            };
            RequestManager.prototype.launchRequests = function () {
                while (this._nActiveRequests < this._nSimultaneousRequests &&
                    this._queue.length > 0) {
                    this._nActiveRequests++;
                    this._queue.pop().resolve();
                }
            };
            /**
             * Try to request a given URL using overwritable _promiseFromUrl method.
             * If the request fails for any reason, we will retry up to maxRetries
             * times. In practice, this will help us paper over transient network issues
             * like '502 Bad Gateway'.
             * By default, Chrome displays network errors in console, so
             * the user will be able to tell when the requests are failing. I think this
             * is a feature, if the request failures and retries are causing any
             * pain to users, they can see it and file issues.
             */
            RequestManager.prototype.promiseWithRetries = function (url, maxRetries) {
                var _this = this;
                var success = function (x) { return x; };
                var failure = function (x) {
                    if (maxRetries > 0) {
                        return _this.promiseWithRetries(url, maxRetries - 1);
                    }
                    else {
                        return Promise.reject(x);
                    }
                };
                return this._promiseFromUrl(url).then(success, failure);
            };
            /* Actually get promise from url using XMLHttpRequest */
            RequestManager.prototype._promiseFromUrl = function (url) {
                return new Promise(function (resolve, reject) {
                    var req = new XMLHttpRequest();
                    req.open('GET', url);
                    req.onload = function () {
                        if (req.status === 200) {
                            resolve(JSON.parse(req.responseText));
                        }
                        else {
                            reject(new RequestNetworkError(req, url));
                        }
                    };
                    req.onerror = function () {
                        reject(new RequestNetworkError(req, url));
                    };
                    req.send();
                });
            };
            return RequestManager;
        }());
        Backend.RequestManager = RequestManager;
    })(Backend = TF.Backend || (TF.Backend = {}));
})(TF || (TF = {}));
