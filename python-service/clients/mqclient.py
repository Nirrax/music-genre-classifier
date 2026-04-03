import json
import os
import time

import pika


class MQClient:
    def __init__(self):
        self.user = os.getenv("RABBITMQ_USER", "user")
        self.password = os.getenv("RABBITMQ_PASSWORD", "password")
        self.host = os.getenv("RABBITMQ_HOST", "localhost")
        self.port = int(os.getenv("RABBITMQ_PORT", 5672))
        self.exchange_name = os.getenv("RABBITMQ_EXCHANGE", "exchange")
        self.inbound_routing_key = os.getenv("RABBITMQ_INBOUND_ROUTING_KEY", "inbound")
        self.outbound_routing_key = os.getenv(
            "RABBITMQ_OUTBOUND_ROUTING_KEY", "outbound"
        )
        self.queue_name = os.getenv("RABBITMQ_QUEUE_NAME", "queue")
        self.connection = None
        self.channel = None
        self.connect()

    def connect(self, max_retries=5, retry_delay=5):
        credentials = pika.PlainCredentials(self.user, self.password)
        parameters = pika.ConnectionParameters(
            host=self.host,
            port=self.port,
            credentials=credentials,
            heartbeat=600,
            blocked_connection_timeout=300,
        )

        for attempt in range(max_retries):
            try:
                self.connection = pika.BlockingConnection(parameters)
                self.channel = self.connection.channel()

                self.channel.exchange_declare(
                    exchange=self.exchange_name, exchange_type="topic", durable=True
                )
                print(f"Successfully connected to RabbitMQ at {self.host}:{self.port}")
                return

            except Exception as e:
                if attempt < max_retries - 1:
                    print(
                        f"Connection attempt {attempt + 1} failed: {e}. Retrying in {retry_delay}s..."
                    )
                    time.sleep(retry_delay)
                else:
                    print(f"Failed to connect after {max_retries} attempts")
                    raise

    def close(self):
        self.stop_consuming()
        if self.connection and not self.connection.is_closed:
            self.connection.close()

    def stop_consuming(self):
        if self.channel:
            self.channel.stop_consuming()

    def setup_queue(self):
        # Declare queue and bind it to exchange with routing key
        if not self.channel:
            raise Exception("Connection is not established.")

        self.channel.queue_declare(queue=self.queue_name, durable=True)
        self.channel.queue_bind(
            exchange=self.exchange_name,
            queue=self.queue_name,
            routing_key=self.inbound_routing_key,
        )

    def consume(self, callback):
        if not self.channel:
            raise Exception("Connection is not established.")

        # Ensure queue exists and is bound
        self.setup_queue()

        self.channel.basic_qos(prefetch_count=1)
        self.channel.basic_consume(
            queue=self.queue_name,
            on_message_callback=callback,
            auto_ack=False,
        )
        print(
            f"Waiting for messages on queue '{self.queue_name}' with routing key '{self.inbound_routing_key}'"
        )
        self.channel.start_consuming()

    def publish(self, message):
        if not self.channel:
            raise Exception("Connection is not established.")

        try:
            message_body = json.dumps(message)
        except (TypeError, ValueError) as e:
            raise ValueError(f"Message is not JSON serializable: {e}")

        try:
            self.channel.basic_publish(
                exchange=self.exchange_name,
                routing_key=self.outbound_routing_key,
                body=message_body,
                properties=pika.BasicProperties(
                    delivery_mode=2,
                    content_type="application/json",
                ),
            )
            print(f"Sent message with routing key '{message}'")
        except Exception as e:
            print(f"Failed to publish message: {e}")
            raise
