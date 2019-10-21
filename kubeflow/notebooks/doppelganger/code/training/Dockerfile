FROM python:3.6-slim

COPY Images/ Images/

COPY requirements.txt requirements.txt
RUN pip install -r requirements.txt

COPY doppelganger-train.py doppelganger-train.py
CMD [ "python", "doppelganger-train.py" ]

