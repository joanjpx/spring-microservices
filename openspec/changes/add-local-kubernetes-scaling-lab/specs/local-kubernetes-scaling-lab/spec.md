# local-kubernetes-scaling-lab Specification

## ADDED Requirements

### Requirement: Deploy the application stack locally on Kubernetes

The project SHALL provide a documented local Kubernetes deployment for the gateway, Eureka discovery server, inventory service, order service, product service, and the databases required by those services. The deployment SHALL use stable in-cluster service names and SHALL preserve the existing API gateway routes.

#### Scenario: Start the local baseline

- **WHEN** a developer follows the documented Minikube setup and deploys the baseline configuration
- **THEN** all application services and their required databases become available in the lab namespace
- **AND** the developer can reach the gateway and Eureka using the documented local access method

### Requirement: Compare horizontal and vertical pod scaling

The project SHALL provide separate, documented HPA and VPA experiment configurations for at least one stateless application service. Both experiments SHALL start from the same documented baseline and use the same workload procedure. The HPA configuration SHALL scale replica count from CPU utilization. The VPA configuration SHALL apply resource recommendations to pod requests. The experiment instructions SHALL prevent HPA and VPA from simultaneously controlling the same target deployment.

#### Scenario: Observe horizontal scaling

- **WHEN** the developer applies the HPA experiment and generates the documented load
- **THEN** Kubernetes reports CPU utilization and HPA desired/current replica counts
- **AND** the target service can scale between its configured replica bounds

#### Scenario: Observe vertical scaling

- **WHEN** the developer applies the VPA experiment and generates the same documented load
- **THEN** VPA reports resource recommendations and applies updated requests using the documented update mode
- **AND** the target deployment's HPA is not active for that experiment

### Requirement: Make experiment runs repeatable and observable

The project SHALL document how to generate the same workload, observe resource use and scaling events, and reset the target service to baseline between runs. The documentation SHALL include a results template for request latency, error rate, CPU and memory use, replica count, and CPU/memory requests. It SHALL describe local host resource contention as a factor when interpreting results.

#### Scenario: Reset between scaling modes

- **WHEN** a developer finishes one scaling experiment and selects the other mode
- **THEN** the documented reset procedure removes the prior autoscaler configuration and restores the target service's baseline replica count and resource requests before the next run
