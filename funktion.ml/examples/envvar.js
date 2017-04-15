module.exports = function(context, callback) {
  var name = process.env.NAME || "World";
  callback(200, "Hello " + name + "!");
};