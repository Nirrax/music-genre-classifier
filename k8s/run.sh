#!bin/bash

kubectl apply -f k8s-namespace.yaml
kubectl apply -f k8s-secrets.yaml
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.0/deploy/static/provider/cloud/deploy.yaml
kubectl apply -f k8s-postgres-single.yaml
kubectl apply -f k8s-s3.yaml
kubectl apply -f k8s-rabbitmq.yaml
kubectl apply -f k8s-worker.yaml
kubectl apply -f k8s-backend.yaml
kubectl apply -f k8s-frontend.yaml
kubectl apply -f k8s-ingress.yaml

echo "Waiting for frontend to spin up"
sleep 30
kubectl port-forward -n music-classifier svc/localstack 4566:4566

