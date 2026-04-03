from dataclasses import dataclass
from datetime import date
from typing import Dict, List


@dataclass
class ClassificationResponseMessage:
    classification_id: str  # UUID as string for JSON
    genre: str
    genre_sequence: List[str]
    genre_counts: Dict[str, int]
    completed_at: str  # Date as ISO string for JSON
    status: str

    @classmethod
    def create(
        cls,
        classification_id: str,
        genre: str,
        genre_sequence: List[str],
        genre_counts: Dict[str, int],
        status: str,
    ) -> "ClassificationResponseMessage":
        """Factory method to create response with auto-generated fields"""
        return cls(
            classification_id=classification_id,
            genre=genre,
            genre_sequence=genre_sequence,
            genre_counts=genre_counts,
            completed_at=date.today().isoformat(),
            status=status,
        )

    def to_dict(self) -> dict:
        return {
            "classificationId": self.classification_id,
            "genre": self.genre,
            "genreSequence": self.genre_sequence,
            "genreCounts": self.genre_counts,
            "completedAt": self.completed_at,
            "status": self.status,
        }
