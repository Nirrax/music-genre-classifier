import math
import os
import shutil

import librosa
import numpy as np
from pydub import AudioSegment


def convert_mp3_to_wav(filepath: str) -> None:
    audio = AudioSegment.from_mp3(filepath)
    audio.export(filepath + ".wav", format="wav")


def clear_temp(directory="temp/") -> None:
    if not os.path.exists(directory):
        return

    for item in os.listdir(directory):
        item_path = os.path.join(directory, item)

        if os.path.isfile(item_path) or os.path.islink(item_path):
            os.remove(item_path)
        elif os.path.isdir(item_path):
            shutil.rmtree(item_path)


def generate_mfcc_from_file(filepath: str, n_mfcc=13, n_fft=2048, hop_length=512):
    SAMPLE_RATE = 22050
    mfccs = []

    # load file with librosa
    signal, sr = librosa.load(filepath, sr=SAMPLE_RATE)
    duration = math.floor(librosa.get_duration(y=signal, sr=SAMPLE_RATE))

    # Check if audio is long enough
    if duration < 3:
        print(
            f"Warning: Audio file {filepath} is too short ({duration}s). Minimum 3 seconds required."
        )
        return np.array([])  # Return empty array instead of None

    number_of_segments = int(duration / 3)
    samples_per_track = SAMPLE_RATE * duration
    number_of_samples_per_segment = int(samples_per_track / number_of_segments)
    expected_number_of_mfcc_vectors_per_segment = 130

    for segment in range(number_of_segments):
        start_sample = number_of_samples_per_segment * segment
        finish_sample = start_sample + number_of_samples_per_segment

        # generate mfcc for a segment
        mfcc = librosa.feature.mfcc(
            y=signal[start_sample:finish_sample],
            sr=sr,
            n_fft=n_fft,
            n_mfcc=n_mfcc,
            hop_length=hop_length,
        )
        mfcc = mfcc.T

        # ensure that mfcc count does not exceed expected value
        if len(mfcc) > expected_number_of_mfcc_vectors_per_segment:
            mfcc = np.delete(mfcc, 1, axis=0)

        if len(mfcc) == expected_number_of_mfcc_vectors_per_segment:
            mfccs.append(mfcc.tolist())

    # Check if we got any valid segments
    if len(mfccs) == 0:
        print(f"Warning: No valid MFCC segments generated for {filepath}")
        return np.array([])

    print(f"Generated {len(mfccs)} MFCC segments from {filepath}")
    return np.array(mfccs)


def predict(model, X) -> tuple[str, dict[str, int], list[str]]:
    list_of_genres = [
        "blues",
        "classical",
        "country",
        "disco",
        "hiphop",
        "jazz",
        "metal",
        "pop",
        "reggae",
        "rock",
    ]
    genres_sequence = []
    genres_distribution = {
        "blues": 0,
        "classical": 0,
        "country": 0,
        "disco": 0,
        "hiphop": 0,
        "jazz": 0,
        "metal": 0,
        "pop": 0,
        "reggae": 0,
        "rock": 0,
    }

    for segment in X:
        segment = segment[np.newaxis, ...]

        prediction = model.predict(segment)

        # extract index with max value
        predicted_index = np.argmax(prediction, axis=1)

        # get the predicted genre from the list
        predicted_genre = list_of_genres[int(predicted_index)]
        genres_distribution[predicted_genre] += 1

        # save sequence of genres
        genres_sequence.append(predicted_genre)

    # extract main genre from the dictionary
    genre = get_key_with_max_value(genres_distribution)

    return genre, genres_distribution, genres_sequence


def get_key_with_max_value(d: dict) -> str:
    if not d:
        return ""

    max_value = max(d.values())
    max_keys = [key for key, value in d.items() if value == max_value]

    return "/".join(max_keys)
