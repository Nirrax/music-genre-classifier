import json
from dataclasses import dataclass


@dataclass
class ClassificationRequestMessage:
    classification_id: str
    s3_key: str

    @staticmethod
    def from_json(json_str: str) -> "ClassificationRequestMessage":
        data = json.loads(json_str)
        return ClassificationRequestMessage(
            classification_id=data["classificationId"], s3_key=data["s3key"]
        )
