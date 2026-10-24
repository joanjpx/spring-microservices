# Design: Local Kubernetes scaling lab

## Environment

Use Minikube with the Docker driver as the documented local target. Build Java 17 runtime images from the existing Maven package outputs, load those images into Minikube, and deploy through versioned Kubernetes manifests. Keep image tags deterministic for local rebuilds and avoid requiring a remote image registry.

Use a namespace dedicated to the lab. Deploy the gateway, Eureka, inventory, order, and product services, plus PostgreSQL for inventory and products and MySQL for orders. Use Kubernetes Services for stable in-cluster addresses. Retain Eureka for application registration and the gateway's existing `lb://` routes in this change; configure clients to reach Eureka through its Kubernetes Service DNS name. Expose the gateway and Eureka with Minikube-supported local access instructions.

Database credentials should be supplied through Kubernetes Secrets, with non-production sample values clearly identified for local use. Database storage should use persistent volume claims where supported by Minikube so a pod restart does not silently reset experiment data. The lab documentation must explain how to reset it deliberately.

## Scaling experiments

Provide a baseline deployment with explicit CPU and memory requests and limits for scalable application services. Add separate experiment configurations:

1. **HPA:** Metrics Server enabled; an HPA targets CPU utilization and scales a selected stateless service between documented minimum and maximum replicas. Requests are required because the utilization target is measured relative to requests.
2. **VPA:** VPA components installed; a VPA targets the same service in an update mode that actually applies recommendations (initially `Recreate`/`Auto`, as supported by the installed VPA version). The HPA for that service is absent during this run. Document that VPA may evict/recreate pods and can affect availability.

The experiment procedure starts from the same baseline, warms up the service, applies the same workload duration and request rate, records response latency/error rate, CPU/memory usage, replica count, and resource requests, then resets before switching modes. Include a recommendation-only VPA option if useful for observing recommendations without changing pods, but do not present it as a vertical scaling run.

## Workload and observability

Use a small, reproducible HTTP load generator that can target existing gateway routes. Do not assume endpoint semantics: inspect the service controllers during implementation and document a valid read or safe test request. Provide commands to watch `kubectl top pods`, deployments, HPA status, VPA recommendations/status, and pod events. Record commands and a results table template; avoid embedding machine-specific performance conclusions.

## Health and resource management

Use Spring Actuator health endpoints only where the dependency and exposure are configured; otherwise add an appropriate lightweight readiness/liveness endpoint or explicitly scope probes to the endpoints that already exist. Avoid using liveness checks for dependencies such as databases. Set conservative resource requests/limits suitable for a local developer machine and make them configurable in the manifests.

## Rollout and compatibility

Kubernetes resources live alongside the existing Compose files. Document separate start/stop/reset commands. Compose remains available and is not rewritten as a prerequisite for Kubernetes. Keep experiment profiles mutually exclusive and make reset steps return the selected service to the same base replica count and resource requests before the next run.

## Risks and mitigations

- Local VPA installation can be version-sensitive: pin and document the VPA release and installation steps, and verify its admission/controller components are ready before applying VPA resources.
- CPU throttling and host contention can skew results: record host/runtime details, use repeatable load settings, and compare multiple runs.
- Running all databases and replicas can exceed a laptop's resources: use small defaults and document how to reduce optional replicas or memory requests.
- HPA and VPA can conflict when both control CPU/memory for the same target: provide mutually exclusive profiles and explicit cleanup between experiments.
