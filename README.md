# Music Genre Classifier

This project is a full-stack web application designed to classify the genre of music from an MP3 file. It utilizes a microservices architecture, featuring a React frontend, a Spring Boot backend, and a Python worker that leverages a Convolutional Neural Network (CNN) for genre prediction.

## Architecture

The system is composed of several independent services that communicate asynchronously to handle music classification tasks.

-   **React Frontend**: A modern single-page application (SPA) built with React and Vite. It provides the user interface for registration, login, file uploads, and viewing classification results with data visualizations.
-   **Spring Boot Backend**: A robust API service that manages user authentication (JWT-based), classification requests, and data persistence. It orchestrates the classification process by interacting with the S3-compatible storage and the RabbitMQ message queue.
-   **Python Worker**: A dedicated service responsible for the heavy lifting of machine learning. It consumes tasks from RabbitMQ, downloads the corresponding MP3 file from S3, preprocesses the audio (converts to WAV, extracts MFCCs), and performs genre classification using a pre-trained TensorFlow/Keras model. The results are then published back to the queue.
-   **PostgreSQL**: The primary relational database used for storing user information and classification metadata.
-   **RabbitMQ**: A message broker that facilitates asynchronous, reliable communication between the Spring Boot backend and the Python worker, ensuring that classification jobs are processed without blocking the user interface.
-   **S3 (LocalStack)**: An S3-compatible object storage service used to store the uploaded MP3 files. Presigned URLs are used for secure and direct uploads from the client.
-   **Docker & Docker Compose**: Used for containerizing all services to create a consistent and easy-to-run local development environment.
-   **Kubernetes**: Manifests are provided for deploying the application to a Kubernetes cluster, enabling scalability, fault tolerance, and a production-ready setup.

### Data Flow

1.  A user uploads an MP3 file through the React frontend.
2.  The frontend requests a presigned S3 upload URL from the Spring Boot backend.
3.  The frontend uploads the file directly to the S3 bucket using the presigned URL.
4.  After a successful upload, the frontend notifies the backend to initiate classification, providing the file's S3 key.
5.  The backend saves the initial classification record in the PostgreSQL database with a `PENDING` status.
6.  The backend publishes a message to a RabbitMQ queue containing the classification ID and S3 key.
7.  A Python worker picks up the message, downloads the audio file from S3, and processes it.
8.  The worker's CNN model predicts the genre and generates a distribution analysis.
9.  The worker publishes the results back to a different RabbitMQ queue.
10. The Spring backend listens for these results, updates the corresponding record in PostgreSQL with the genre, analysis data, and a `DONE` status.
11. The user can view the completed classification details and visualizations on the frontend.

## Technology Stack

-   **Frontend**: React, Vite, Axios, Recharts (for data visualization)
-   **Backend**: Java 21, Spring Boot 3, Spring Security, JPA/Hibernate, JJWT
-   **Worker**: Python 3.11, TensorFlow/Keras, Librosa, Pydub, Boto3, Pika
-   **Database**: PostgreSQL
-   **Message Queue**: RabbitMQ
-   **Storage**: S3-compatible object storage (e.g., MinIO, AWS S3)
-   **Containerization**: Docker, Docker Compose
-   **Orchestration**: Kubernetes

## Getting Started

### Prerequisites

-   Docker
-   Docker Compose
-   (Optional for Kubernetes deployment) A running Kubernetes cluster (e.g., Minikube, Docker Desktop) and `kubectl` CLI.

### Running with Docker Compose (Local Development)

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/Nirrax/music-genre-classifier.git
    cd music-genre-classifier
    ```

2.  **Configure the Python Worker:**
    Navigate to the `python-service` directory and create a `.env` file from the example. The values are pre-configured for the `docker-compose` environment.
    ```sh
    cd python-service
    cp .env.example .env
    cd ..
    ```

3.  **Build and run the services:**
    From the root directory, run the following command:
    ```sh
    docker-compose up --build
    ```

4.  **Access the application:**
    -   Frontend UI: [http://localhost:8081](http://localhost:8081)
    -   RabbitMQ Management: [http://localhost:15672](http://localhost:15672) (user: `rabbit`, pass: `pass`)

### Running with Kubernetes

The provided Kubernetes manifests are configured to use locally built Docker images.

1.  **Enable Kubernetes** in Docker Desktop or start your Minikube cluster.

2.  **Build the Docker images:**
    Make sure your Docker environment is pointed to the cluster's daemon (for Minikube, run `eval $(minikube docker-env)`). Then, build the images with the specified tags from the root directory.
    ```sh
    # Build Spring Boot Backend
    docker build -t music-genre-classifier-backend:latest ./spring-api

    # Build React Frontend
    docker build -t music-genre-classifier-frontend:latest ./react-frontend

    # Build Python Worker
    docker build -t music-genre-classifier-worker:latest ./python-service
    ```

3.  **Deploy the application:**
    Navigate to the `k8s` directory and execute the run script.
    ```sh
    cd k8s
    ./run.sh
    ```
    This script will:
    -   Create the `music-classifier` namespace.
    -   Apply all necessary Kubernetes secrets.
    -   Deploy components like the NGINX Ingress controller and Metrics Server.
    -   Deploy PostgreSQL, RabbitMQ, and LocalStack (S3).
    -   Deploy the backend, worker, and frontend applications.
    -   Set up the Ingress rules to route traffic.

4.  **Access the application:**
    The application will be available through the NGINX Ingress controller. Find its external IP address:
    ```sh
    kubectl get ingress -n music-classifier
    ```
    Access the application using the address shown, typically `http://localhost`.

## Project Structure

```
.
├── docker-compose.yml          # Docker Compose for local development
├── k8s/                          # Kubernetes manifests and deployment scripts
├── python-service/               # Python ML worker service
│   ├── clients/                  # S3 and RabbitMQ clients
│   ├── model/                    # ML model training and data parsing scripts
│   ├── model_saved/              # Pre-trained Keras model
│   └── main.py                   # Main entry point for the worker
├── react-frontend/               # React frontend application
│   ├── src/
│   │   ├── components/           # React components
│   │   ├── context/              # Auth context provider
│   │   └── hooks/                # Custom React hooks
│   └── vite.config.js            # Vite configuration
└── spring-api/                   # Spring Boot backend API
    ├── src/main/java/com/sa/spring_api/
    │   ├── auth/                 # Authentication (JWT) logic
    │   ├── classification/       # Classification management logic
    │   ├── config/               # App configuration (Security, DB, MQ, S3)
    │   └── user/                 # User management logic
    └── pom.xml                   # Maven project configuration
