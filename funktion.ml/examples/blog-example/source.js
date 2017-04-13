module.exports = function(context, callback) {
  var foo = "James";
  callback(200, "Hello World " + foo);
};