module.exports = function (context, callback) {
  const http = require('http');
  if (!http) {
    console.log("could not require http");
    callback(400, "Could not require http");
    return;
  }

  var body = context.request.body;
  if (body && body.constructor === Array) {
    var counter = 0;
    var result = {
      responses: []
    };
    var status = 200;
    var firstError = null;

    function returnError(e) {
      console.log("Caught error: " + e);
      result.error = e;
      status = 400;
      var resultJson = JSON.stringify(result);
      console.log("result: " + status + " " + resultJson);
      callback(status, resultJson);
    }

    try {
      var lastIndex = body.length - 1;
      body.forEach(function (item, idx) {
        var postData = JSON.stringify(item);
        var postOptions = {
          hostname: 'blogendpoint',
          port: '80',
          path: '/',
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache',
            'Content-Length': postData.length
          }
        };

        req = http.request(postOptions, function (res) {
          result[idx] = {
            statusCode: res.statusCode
          };
          res.setEncoding('utf8');
          var data = "";
          res.on('data', function (chunk) {
            data += chunk;
          });
          res.on('end', function () {
            result[idx].response = data;
            if (idx === lastIndex) {
              result.count = counter;
              if (firstError) {
                console.log("Failed with error: " + firstError);
                results.error = firstError;
                status = 400;
              }
              var resultJson = JSON.stringify(result);
              callback(status, resultJson);
            }
          });
        });

        req.on('error', function (e) {
          console.log('problem with request: ' + e.message);
          if (idx === lastIndex) {
            returnError(e);
          }
        });

        req.write(postData);
        req.end();
        counter++;
      });
    } catch (e) {
      returnError(e);
    }
  } else {
    callback(400, "No array is passed in. Was given: " + JSON.stringify(body));
  }
};


