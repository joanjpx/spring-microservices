# spring-microservices
Spring Boot Microservices course

## Branching model

This repository uses Git Flow. `master` contains production-ready changes and `develop` is the integration branch. Create `feature/*` branches from `develop` and merge them back through pull requests; prepare releases on `release/*` branches from `develop`, and urgent production fixes on `hotfix/*` branches from `master`. See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the commands and merge targets.

## Local Kubernetes scaling labs

The OpenSpec proposal for running these microservices locally on Kubernetes and comparing horizontal and vertical pod scaling is in `openspec/changes/add-local-kubernetes-scaling-lab/`.

The HPA lab runs on Minikube and scales `product-service` horizontally using CPU utilization. Its setup, configuration, access options, and Ingress notes are in [`microservices/kubernetes/README.md`](microservices/kubernetes/README.md). Docker Compose remains a separate local development workflow.
