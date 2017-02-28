
# Prediction web server: Using the Cloud ML API for prediction

This directory contains a little webserver that shows how you can use the Cloud ML API for prediction once you have created a transfer learning model.
It allows you to upload an image and get a prediction (classification) and score back.

The example assumes the "hugs/not-hugs" example of the parent directory w.r.t how it displays its response.
However, it is easily modified if you have done the transfer learning training on a different set of images with different labels.

This code assumes that you have installed the gcloud SDK and have run:

```shell
gcloud beta auth application-default login
```

## Running the server

If you're using virtual environments, run the server from the same virtual environment that you set up to do the model training. Then, make sure that you have all of the web server requirements installed in that environment:

```shell
pip install -r requirements.txt
```

Start the server like this:

```shell
python predict_server.py --model_name hugs --project aju-vtests2 --dict static/dict.txt
```

Then, upload an image (it must be of JPEG format) and see what category is predicted for it.

If you want to modify the server for a different set of classification labels, you will need to update the `static/dict.txt` file as well as modify the `show_result()` method and its template.

## Calling the Cloud ML API

This example uses the Google client API libraries to access the Cloud ML API.

In `predict_server.py`, the `create_client()` method shows how to create the client object.
The `make_request_json()` method uses `PIL.Image` to generate a base64 encoding of the image for the API request.
Then, `get_prediction()` executes the request.

