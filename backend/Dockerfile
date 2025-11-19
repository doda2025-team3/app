FROM python:3.12.9-slim AS builder
WORKDIR /app

COPY backend/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY backend/src src/
COPY backend/smsspamcollection smsspamcollection/
RUN mkdir output

ENV MODEL_PORT="8081"

CMD ["sh", "-c", "python src/read_data.py && python src/text_preprocessing.py && python src/text_classification.py"]