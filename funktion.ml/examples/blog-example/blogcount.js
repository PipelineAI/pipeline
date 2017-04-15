module.exports = function(context, callback) {
  var body = context.request.body;
  body.titleWordCount = countWords(body.title);
  body.bodyWordCount = countWords(body.body);

  callback(200, JSON.stringify(body));
};

//
function countWords(text) {
  var words = null;
  if (text) {
    words = text.split(" ");
    return words.length;
  }
  return 0;
}

