# DOWNLOAD HPA

kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
kubectl get pods -n kube-system | grep metrics-server

kubectl logs -n kube-system deployment/metrics-server

# IF TLS ERROR DO THIS:

kubectl edit deployment metrics-server -n kube-system

# FIND ARGS AND MAKE IT LIKE THIS:

args:
- --cert-dir=/tmp
- --secure-port=4443
- --kubelet-use-node-status-port
- --kubelet-insecure-tls
- --kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname

# THEN

kubectl -n kube-system rollout status deployment metrics-server

# TEST

kubectl top pods -n music-classifier
