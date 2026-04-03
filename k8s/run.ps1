kubectl create namespace music-classifier
kubectl apply -f k8s-secrets.yaml
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.0/deploy/static/provider/cloud/deploy.yaml
kubectl apply -f k8s-postgres-single.yaml
kubectl apply -f k8s-s3.yaml
kubectl apply -f k8s-rabbitmq.yaml
kubectl apply -f k8s-worker.yaml
kubectl apply -f k8s-backend.yaml
kubectl apply -f k8s-ingress.yaml
kubectl apply -f k8s-frontend.yaml

Write-Host "Waiting for frontend to spin up"
Start-Sleep -Seconds 30
kubectl port-forward -n music-classifier svc/localstack 4566:4566

