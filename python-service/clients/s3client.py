import logging
import os
from pathlib import Path
from typing import Optional

import boto3
from botocore.exceptions import ClientError

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class S3Client:
    def __init__(
        self,
        endpoint_url: Optional[str] = None,
        access_key_id: Optional[str] = None,
        secret_access_key: Optional[str] = None,
        region_name: Optional[str] = None,
        bucket_name: Optional[str] = None,
    ):
        self.endpoint_url = endpoint_url or os.getenv("AWS_ENDPOINT_URL")
        self.access_key_id = access_key_id or os.getenv("AWS_ACCESS_KEY_ID")
        self.secret_access_key = secret_access_key or os.getenv("AWS_SECRET_ACCESS_KEY")
        self.region_name = region_name or os.getenv("AWS_REGION", "us-east-1")
        self.bucket_name = bucket_name or os.getenv("S3_BUCKET_NAME")

        self.client = boto3.client(
            "s3",
            endpoint_url=self.endpoint_url,
            aws_access_key_id=self.access_key_id,
            aws_secret_access_key=self.secret_access_key,
            region_name=self.region_name,
        )

        logger.info(f"S3Client initialized with endpoint: {self.endpoint_url}")

    def upload_file(self, local_path: str, s3_key: str) -> bool:
        if not self.bucket_name:
            logger.error("No bucket name provided")
            return False

        try:
            self.client.upload_file(local_path, self.bucket_name, s3_key)
            logger.info(
                f"Successfully uploaded {local_path} to s3://{self.bucket_name}/{s3_key}"
            )
            return True
        except FileNotFoundError:
            logger.error(f"File not found: {local_path}")
            return False
        except ClientError as e:
            logger.error(f"Failed to upload file: {e}")
            return False

    def download_file(self, s3_key: str, local_path: str) -> bool:
        if not self.bucket_name:
            logger.error("No bucket name provided")
            return False

        try:
            # Create parent directories if they don't exist
            Path(local_path).parent.mkdir(parents=True, exist_ok=True)

            self.client.download_file(self.bucket_name, s3_key, local_path)
            logger.info(
                f"Successfully downloaded s3://{self.bucket_name}/{s3_key} to {local_path}"
            )
            return True
        except ClientError as e:
            if e.response["Error"]["Code"] == "404":
                logger.error(f"File not found in S3: s3://{self.bucket_name}/{s3_key}")
            else:
                logger.error(f"Failed to download file: {e}")
            return False

    def file_exists(self, s3_key: str) -> bool:
        if not self.bucket_name:
            logger.error("No bucket name provided")
            return False

        try:
            self.client.head_object(Bucket=self.bucket_name, Key=s3_key)
            return True
        except ClientError:
            return False
