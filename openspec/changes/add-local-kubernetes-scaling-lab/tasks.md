# Tasks: Local Kubernetes scaling lab

## Container images

- [x] Add a maintainable Java 17 runtime image build for each Spring Boot service, with configurable image tags and no hard-coded developer paths.
- [x] Document/build all five service images and load them into Minikube without a remote registry.

## Kubernetes baseline

- [x] Add a dedicated namespace and manifests for Eureka, gateway, inventory, order, and product services.
- [x] Add stable Kubernetes Services and configure Eureka clients and gateway routes to work inside the cluster.
- [x] Add PostgreSQL and MySQL workloads, Services, Secrets, and persistent storage suitable for local experiments.
- [x] Configure requests, limits, probes, and externally accessible gateway/Eureka endpoints.
- [x] Add a baseline deployment configuration with no autoscaler controlling the selected experiment target.

## HPA/VPA experiments

- [x] Add documented Minikube setup with Metrics Server for CPU-based HPA.
- [ ] Add a pinned VPA installation procedure and a separate VPA experiment configuration that applies resource recommendations.
- [ ] Ensure HPA and VPA experiment modes are mutually exclusive for the same target deployment.
- [x] Add a repeatable load-generation procedure using a valid existing API route.
- [x] Document metrics collection, warm-up, reset, run duration, and a comparison results template for latency, errors, CPU/memory, replicas, and requests.

## Developer workflow

- [ ] Add scripts or make targets to build/load images, deploy baseline, select HPA or VPA, observe, and cleanly reset the lab.
- [x] Update the README with prerequisites, startup/access instructions, experiment steps, resource guidance, and troubleshooting.
- [ ] Validate manifests and exercise baseline, HPA, and VPA workflows on a local Minikube cluster; record known machine-dependent limitations.
