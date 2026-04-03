import json
from tensorflow import keras
from clients.mqclient import MQClient
from clients.s3client import S3Client
from dotenv import load_dotenv
from dto.ClassificationRequestMessage import ClassificationRequestMessage
from dto.ClassificationResponseMessage import ClassificationResponseMessage
from utils.utils import clear_temp, convert_mp3_to_wav, generate_mfcc_from_file, predict
from utils.health import start_health_server, wait_for_port, set_ready, set_alive, record_message_processed

load_dotenv()

def make_callback(mq: MQClient, s3: S3Client, model):
    def message_callback(ch, method, properties, body):
        try:
            request = ClassificationRequestMessage.from_json(body)
            print(f"Received message: {request.classification_id} | {request.s3_key}")

            if not s3.file_exists(request.s3_key):
                _publish_failed(mq, request.classification_id)
                ch.basic_ack(delivery_tag=method.delivery_tag)
                return

            s3.download_file(request.s3_key, f"temp/{request.s3_key}")
            convert_mp3_to_wav(f"temp/{request.s3_key}")

            mfccs = generate_mfcc_from_file(f"temp/{request.s3_key}.wav")
            clear_temp()

            if mfccs.size == 0:
                _publish_failed(mq, request.classification_id)
                ch.basic_ack(delivery_tag=method.delivery_tag)
                return

            genre, genre_distribution, genre_sequence = predict(model, mfccs)

            mq.publish(
                ClassificationResponseMessage.create(
                    classification_id=request.classification_id,
                    genre=genre,
                    genre_sequence=genre_sequence,
                    genre_counts=genre_distribution,
                    status="DONE",
                ).to_dict()
            )

            record_message_processed()
            ch.basic_ack(delivery_tag=method.delivery_tag)

        except Exception as e:
            print(f"Error processing message: {e}")
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)

    return message_callback


def _publish_failed(mq: MQClient, classification_id: str):
    mq.publish(
        ClassificationResponseMessage.create(
            classification_id=classification_id,
            genre="Unknown",
            genre_sequence=["Unknown"],
            genre_counts={"Unknown": 1},
            status="FAILED",
        ).to_dict()
    )


if __name__ == "__main__":
    health_server = start_health_server(port=8080)
    wait_for_port(8080)

    mqclient = None
    try:
        print("Loading model...")
        model = keras.models.load_model("model_saved")
        print("Model loaded successfully")

        print("Initializing clients...")
        mqclient = MQClient()
        s3client = S3Client()

        set_ready(True)
        print("Service is ready")

        mqclient.consume(callback=make_callback(mqclient, s3client, model))

    except KeyboardInterrupt:
        print("Shutting down...")
    except Exception as e:
        print(f"Fatal error: {e}")
        raise
    finally:
        set_ready(False)
        set_alive(False)
        if mqclient is not None:
            mqclient.stop_consuming()
            mqclient.close()
        health_server.shutdown()