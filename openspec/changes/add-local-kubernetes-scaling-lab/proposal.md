# Proposal: Add a local Kubernetes scaling lab

## Why

The project currently runs its Spring Boot services and databases through Docker Compose. Compose can start fixed replicas, but it does not provide a Kubernetes environment in which to observe and compare Horizontal Pod Autoscaling (HPA) with Vertical Pod Autoscaling (VPA). The project needs a repeatable local lab that makes the two scaling strategies observable under the same workload.

## What Changes

- Add a local Minikube-based Kubernetes deployment for the five Spring Boot services and their databases.
- Package each application as a container image that can be built and loaded into the local cluster.
- Configure Kubernetes Services, resource requests and limits, startup/readiness/liveness checks, and local access to the gateway and Eureka.
- Add a documented, repeatable load scenario and commands to observe pod counts and resource allocation.
- Provide separate HPA and VPA experiment profiles. HPA changes replica count; VPA changes pod resource requests. The profiles must not control the same workload at the same time.
- Keep the existing Docker Compose setup available as the simpler non-Kubernetes option.

## Capabilities

### New Capabilities
- `local-kubernetes-scaling-lab`: Run the application stack locally on Kubernetes and compare horizontal and vertical scaling experiments.

### Modified Capabilities
- None.

## Impact

- Adds Kubernetes manifests/configuration and local developer scripts under `microservices/`.
- Requires Docker, kubectl, Minikube, and a compatible local Kubernetes runtime. VPA experiments also require the VPA components; HPA CPU experiments require the Metrics Server.
- Adds container build configuration for the Spring Boot applications.
- Does not change the existing HTTP API or remove the Docker Compose workflow.

## Out of Scope

- Cloud Kubernetes deployment, production hardening, or managed autoscaling.
- A public benchmark suite or claims that HPA or VPA is universally faster/better.
- Running HPA and VPA in competing modes against the same deployment in one experiment.
- Replacing Eureka with Kubernetes-native service discovery in this first iteration.
