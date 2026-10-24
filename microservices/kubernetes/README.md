# Local Kubernetes HPA lab

This branch adds a Minikube lab for horizontal scaling of `product-service`. Docker Compose remains available as a separate workflow. The cluster runs the five Spring Boot services and their three databases; only `product-service` is autoscaled in this experiment.

## Prerequisites

- Windows 10/11 with Docker Desktop configured for Linux containers.
- JDK 17 or newer (`JAVA_HOME` may point to JDK 21, as used in this repository).
- `kubectl` and Minikube on `PATH`.
- At least 4 CPUs and 6 GiB of RAM available to Minikube for the full stack; reduce the `--cpus`/`--memory` values in `scripts/up.ps1` for a smaller machine.

Install Minikube using the [official Windows instructions](https://minikube.sigs.k8s.io/docs/start/). The lab uses the Docker driver and the Minikube Metrics Server addon; Kubernetes needs CPU requests to calculate HPA CPU utilization ([HPA resource metrics](https://kubernetes.io/docs/concepts/workloads/autoscaling/horizontal-pod-autoscale/)).

## Start the baseline

From the `microservices` directory, run:

```powershell
.\kubernetes\scripts\up.ps1 -Experiment baseline
```

The script starts Minikube if necessary, enables Metrics Server, builds and loads the Java 17 images, creates local-only database credentials, waits for the databases, then deploys the application. The baseline has one `product-service` pod and no HPA.

## Start the HPA experiment

```powershell
.\kubernetes\scripts\up.ps1 -Experiment hpa
.\kubernetes\scripts\access.ps1
```

The script leaves both port-forward processes running in the background and prints their PIDs. It uses local ports `8081` and `8762` by default so Docker Compose can keep using `8080` and `8761`. Browse to [Eureka](http://localhost:8762) and query the product API at `http://localhost:8081/api/product`. Stop the tunnels afterward with `Stop-Process -Id <PID>` for each printed PID. Override the local ports with `-GatewayLocalPort` and `-EurekaLocalPort` if needed.

The HPA targets average CPU utilization at 60%, with one to five product pods. Its CPU request is `100m`; utilization is calculated relative to that request. Run the repeatable k6 scenario from inside the cluster:

```powershell
.\kubernetes\scripts\run-load.ps1
```

The run warms up with 12 virtual users for 60 seconds and then samples the same 12 users for 180 seconds. k6 prints request rate, latency percentiles, and error rate; the job is removed automatically before a later run.

Watch scaling and metrics in another terminal:

```powershell
kubectl get hpa,pods -n spring-microservices -w
kubectl top pods -n spring-microservices
kubectl describe hpa product-service -n spring-microservices
```

When the HPA reports a CPU target, record the initial/maximum pod count and elapsed time to scale up. After the 180 second sample ends, allow at least three minutes for HPA scale-down stabilization before the next run.

## Compare runs and reset

Run the baseline and HPA workload from the same cluster state and with the same duration. Run `scripts/run-load.ps1 -Experiment baseline` during the baseline; it emits k6 request rate, p95 latency, and error rate. `kubectl top` and HPA status provide CPU, memory, and replica data. Record the host CPU/RAM and the `product-service` resource requests. Local load and the database can affect results, so repeat each run before comparing.

| Run | Host CPU/RAM | Virtual users | Warm-up | Sample duration | Request rate | p95 latency | Errors | Product pod CPU/memory | Product replicas | CPU/memory requests |
|---|---|---:|---:|---:|---:|---:|---:|---|---:|---|
| Baseline | | 12 | 60 s | 180 s | | | | | 1 | 100m / 256Mi |
| HPA | | 12 | 60 s | 180 s | | | | | | 100m / 256Mi |

Return to baseline without deleting persistent database data:

```powershell
.\kubernetes\scripts\up.ps1 -Experiment baseline
```

Delete the lab namespace and its persistent volume claims when you intentionally want to reset the database data:

```powershell
.\kubernetes\scripts\down.ps1
```

Add `-DeleteCluster` only when you also want to remove the Minikube cluster. Database credentials (`root` and `12345678`) are local demo values created by `up.ps1`; never reuse them outside this local lab.

## HPA settings

The HPA manifest is under `overlays/hpa/`. It uses the Kubernetes `autoscaling/v2` API and CPU utilization. The baseline Kustomize set does not include an autoscaler. VPA is intentionally not part of this branch; the later VPA lab should use a separate experiment profile and target the same service only after the HPA has been removed.
