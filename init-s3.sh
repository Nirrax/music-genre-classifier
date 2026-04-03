#!/bin/bash

echo "Creating S3 bucket..."
#Create bucket only if not exists
awslocal s3 ls s3://mp3files >/dev/null 2>&1 || \

awslocal s3 mb s3://mp3files
awslocal s3api put-bucket-cors --bucket mp3files --cors-configuration '{
  "CORSRules": [
    {
      "AllowedOrigins": ["*"],
      "AllowedMethods": ["GET", "PUT", "POST", "DELETE", "HEAD"],
      "AllowedHeaders": ["*"],
      "ExposeHeaders": ["ETag"]
    }
  ]
}'

echo "S3 bucket 'mp3Files' created successfully"