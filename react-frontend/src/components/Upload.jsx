import { useState, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import useAxiosPrivate from "../hooks/useAxiosPrivate";
import axios from "../api/axios";

const UPLOAD_URL = "/classifications";

const Upload = () => {
  const [file, setFile] = useState(null);
  const [errMsg, setErrMsg] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [uploading, setUploading] = useState(false);

  const fileInputRef = useRef();
  const axiosPrivate = useAxiosPrivate();
  const navigate = useNavigate();
  const location = useLocation();

  let isMounted = true;
  const controller = new AbortController();

  // Helper function to format duration
  const formatDuration = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];

    if (!selectedFile) {
      setFile(null);
      setErrMsg("");
      return;
    }

    // Check file extension
    const fileExtension = selectedFile.name.split(".").pop().toLowerCase();
    if (fileExtension !== "mp3") {
      setErrMsg("Please select a valid MP3 file.");
      setFile(null);
      fileInputRef.current.value = "";
      return;
    }

    // Check MIME type
    if (
        selectedFile.type !== "audio/mpeg" &&
        selectedFile.type !== "audio/mp3"
    ) {
      setErrMsg("Invalid file type. Only MP3 files are allowed.");
      setFile(null);
      fileInputRef.current.value = "";
      return;
    }

    // Get audio duration
    const audio = new Audio();
    const objectUrl = URL.createObjectURL(selectedFile);

    audio.addEventListener("loadedmetadata", () => {
      const duration = audio.duration;

      // Check if duration is at least 30 seconds
      if (duration < 30) {
        setErrMsg(
            `Track is too short. Minimum duration is 30 seconds. This track is ${formatDuration(duration)}.`
        );
        setFile(null);
        fileInputRef.current.value = "";
        URL.revokeObjectURL(objectUrl);
        return;
      }

      // Store file with duration metadata
      setFile({
        file: selectedFile,
        duration: duration,
        formattedDuration: formatDuration(duration),
      });

      // Clean up the object URL
      URL.revokeObjectURL(objectUrl);
    });

    audio.addEventListener("error", () => {
      setErrMsg("Could not read audio file metadata.");
      setFile(null);
      fileInputRef.current.value = "";
      URL.revokeObjectURL(objectUrl);
    });

    audio.src = objectUrl;
    setErrMsg("");
    setSuccessMsg("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!file) {
      setErrMsg("Please select an MP3 file to upload.");
      return;
    }

    setUploading(true);
    setErrMsg("");
    setSuccessMsg("");

    let isMounted = true;
    const controller = new AbortController();

    try {
      // Step 1: Get presigned URL from your backend
      const response = await axiosPrivate.post(
          "/classifications",
          { filename: file.file.name },
          {
            signal: controller.signal,
            headers: { "Content-Type": "application/json" },
          }
      );

      console.log("Presigned URL:", response.data);
      const presignedUrl = response.data;

      // Step 2: Upload file to S3 using presigned URL
      await axios.put(presignedUrl, file.file, {
        headers: {
          "Content-Type": "audio/mpeg",
        },
        signal: controller.signal,
      });

      // Step 3: Extract S3 key from the presigned URL
      const url = new URL(presignedUrl);
      const pathname = url.pathname;
      // Remove the bucket name from the path to get the key
      // pathname format: /mp3files/files/uuid/filename.mp3
      const pathParts = pathname.split("/");
      const s3Key = pathParts.slice(2).join("/"); // Skip empty string and bucket name

      console.log("S3 Key:", s3Key);

      // Step 4: Send another POST request with the S3 key
      const classificationResponse = await axiosPrivate.post(
          "/classifications/classify", // or whatever your endpoint is
          { s3Key: s3Key },
          {
            signal: controller.signal,
            headers: { "Content-Type": "application/json" },
          }
      );

      if (isMounted) {
        setSuccessMsg("File uploaded successfully!");
        setFile(null);
        fileInputRef.current.value = "";

        // Optional: navigate to classifications after success
        setTimeout(() => navigate("/home"), 5000);
      }
    } catch (err) {
      console.error(err);
      if (!isMounted) return;

      if (!err?.response) {
        setErrMsg("No Server Response");
      } else if (err.response?.status === 401) {
        navigate("/login", { state: { from: location }, replace: true });
      } else {
        setErrMsg(
            err.response?.data?.message || "Upload failed. Please try again."
        );
      }
    } finally {
      if (isMounted) {
        setUploading(false);
      }
    }

    return () => {
      isMounted = false;
      controller.abort();
    };
  };

  return (
      <section>
        <h1>Upload Music</h1>

        {errMsg && <p className="errmsg">{errMsg}</p>}
        {successMsg && (
            <p
                style={{
                  backgroundColor: "lightgreen",
                  color: "darkgreen",
                  fontWeight: "bold",
                  padding: "0.5rem",
                  marginBottom: "0.5rem",
                  borderRadius: "0.5rem",
                }}
            >
              {successMsg}
            </p>
        )}

        <form onSubmit={handleSubmit}>
          <label htmlFor="file-upload">Select MP3 File:</label>
          <input
              type="file"
              id="file-upload"
              ref={fileInputRef}
              accept=".mp3,audio/mpeg,audio/mp3"
              onChange={handleFileChange}
              disabled={uploading}
              style={{
                padding: "0.5rem",
                marginTop: "0.5rem",
              }}
          />

          {file && (
              <div
                  style={{
                    marginTop: "1rem",
                    padding: "0.75rem",
                    background: "rgba(255, 255, 255, 0.1)",
                    borderRadius: "0.5rem",
                  }}
              >
                <p style={{ fontSize: "0.85rem", opacity: 0.8 }}>Selected file:</p>
                <p
                    style={{
                      fontSize: "0.9rem",
                      fontWeight: "bold",
                      marginTop: "0.25rem",
                    }}
                >
                  {file.file.name}
                </p>
                <p
                    style={{ fontSize: "0.8rem", opacity: 0.7, marginTop: "0.25rem" }}
                >
                  Size: {(file.file.size / (1024 * 1024)).toFixed(2)} MB
                </p>
                <p
                    style={{ fontSize: "0.8rem", opacity: 0.7, marginTop: "0.25rem" }}
                >
                  Duration: {file.formattedDuration}
                </p>
              </div>
          )}

          <div className="buttons-container">
            <button
                type="submit"
                disabled={!file || uploading}
                style={{
                  opacity: !file || uploading ? 0.5 : 1,
                  cursor: !file || uploading ? "not-allowed" : "pointer",
                }}
            >
              {uploading ? "Uploading..." : "Upload"}
            </button>
            <button onClick={() => navigate("/home")}>Go Back</button>
          </div>
        </form>
      </section>
  );
};

export default Upload;