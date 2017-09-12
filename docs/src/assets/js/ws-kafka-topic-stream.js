if ( $( "#prediction-stream" ).length ) {
  var ws = new WebSocket("ws://drop.community.pipeline.ai:5959/ws/kafka/predictions");
  ws.onmessage = function(event) {
  //        console.log(event);
    eventJson = JSON.parse(event.data)
  //        console.log(document.getElementsByName("prediction-samples"))
    var prediction = '<p/>Date: ' + new Date() + '<p/><b>Prediction: ' + eventJson.outputs + '</b><br/><br/>Image: ' + eventJson.inputs + '<p/><hr/>';
    $('#prediction-stream').html(prediction)
  }
}