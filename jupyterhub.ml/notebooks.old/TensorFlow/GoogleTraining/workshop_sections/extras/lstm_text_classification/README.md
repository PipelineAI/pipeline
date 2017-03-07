# Text Classification With LSTMs

WIP: Currently only preprocess.py is finished

## Preprocessing

To run preprocessing ensure you have followed the optional Cloud ML Setup instructions, and run

```
python preprocess.py --output-dir gs://$BUCKET/textclassification/data --project $PROJECT_ID --cloud
```

This will write TFRecords to the `textclassification` directory in your bucket.
