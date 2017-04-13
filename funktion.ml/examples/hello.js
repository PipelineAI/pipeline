module.exports = function(context, callback) {
  var name = JSON.stringify(context.request.body) || "World";
  callback(200, "Hello " + name + "!");
};