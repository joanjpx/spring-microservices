# spring-microservices
Spring Boot Microservices course

## Local Kubernetes scaling labs

The OpenSpec proposal for running these microservices locally on Kubernetes and comparing horizontal and vertical pod scaling is in `openspec/changes/add-local-kubernetes-scaling-lab/`.

The HPA lab is developed on branch `codex/lab-hpa`. Its Minikube setup and experiment instructions are in [`microservices/kubernetes/README.md`](microservices/kubernetes/README.md). It scales `product-service` horizontally using CPU utilization. The VPA lab is planned separately on branch `codex/lab-vpa`.
