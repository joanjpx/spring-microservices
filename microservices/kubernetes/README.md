# Local Kubernetes HPA lab

This lab runs the five Spring Boot services and their three databases in Minikube; only `product-service` is autoscaled in the HPA experiment. Docker Compose remains a separate development workflow and can continue running beside Kubernetes.

## Prerequisites

- Windows 10/11 with Docker Desktop configured for Linux containers.
- JDK 17 or newer (`JAVA_HOME` may point to JDK 21, as used in this repository).
- `kubectl` and Minikube on `PATH`.
- At least 4 CPUs and 6 GiB of RAM available to Minikube for the full stack; reduce the `--cpus`/`--memory` values in `scripts/up.ps1` for a smaller machine.
- The setup uses the Minikube profile and Kubernetes context `spring-microservices`, with namespace `spring-microservices`.

Install Minikube using the [official Windows instructions](https://minikube.sigs.k8s.io/docs/start/). The lab uses the Docker driver and the Minikube Metrics Server addon; Kubernetes needs CPU requests to calculate HPA CPU utilization ([HPA resource metrics](https://kubernetes.io/docs/concepts/workloads/autoscaling/horizontal-pod-autoscale/)).

## Start the baseline

From the `microservices` directory, run:

```powershell
.\kubernetes\scripts\up.ps1 -Experiment baseline
```

The script starts Minikube if necessary, enables Metrics Server, builds and loads the Java 17 images, creates local-only database credentials, waits for the databases, then deploys the application. The baseline has one `product-service` pod and no HPA. Metrics Server is required because HPA reads CPU usage; CPU requests are required to calculate utilization.

## Start the HPA experiment

```powershell
.\kubernetes\scripts\up.ps1 -Experiment hpa
.\kubernetes\scripts\access.ps1
```

The script leaves both port-forward processes running in the background and prints their PIDs. It uses local ports `8081` and `8762` by default so Docker Compose can keep using `8080` and `8761`. Browse to [Eureka](http://localhost:8762) and query the product API at `http://localhost:8081/api/product`. Stop the tunnels afterward with `Stop-Process -Id <PID>` for each printed PID. Override the local ports with `-GatewayLocalPort` and `-EurekaLocalPort` if needed.

The HPA manifest at `overlays/hpa/hpa.yaml` targets average CPU utilization at 60%, with one to five product pods. The `product-service` Deployment requests `100m` CPU and `256Mi` memory, and limits CPU to `750m` and memory to `512Mi`; CPU utilization is calculated relative to the CPU request. Scale-up allows up to two additional pods or 100% growth per minute, while scale-down uses a 180-second stabilization window and can remove at most 50% per minute. These settings make the experiment repeatable and limit sudden scale-down. Run the k6 scenario from inside the cluster:

```powershell
.\kubernetes\scripts\run-load.ps1
```

The run warms up with 12 virtual users for 60 seconds and then samples the same 12 users for 180 seconds. k6 prints request rate, latency percentiles, and error rate; the job is removed automatically before a later run.

Watch the HPA and pods in separate terminals (some `kubectl` versions reject multiple resource types with `-w`):

```powershell
kubectl get hpa -n spring-microservices -w
kubectl get pods -n spring-microservices -w
kubectl top pods -n spring-microservices
kubectl describe hpa product-service -n spring-microservices
```

When the HPA reports a CPU target, record the initial/maximum pod count and elapsed time to scale up. After the 180 second sample ends, allow at least three minutes for HPA scale-down stabilization before the next run.

## Network access and Ingress

There is **no Ingress configured or deployed by this lab**. Kubernetes Services provide in-cluster connectivity, and `scripts/access.ps1` uses `kubectl port-forward` to make the gateway and Eureka reachable from the host. It defaults to `http://localhost:8081` and `http://localhost:8762`, leaving Compose's usual `8080` and `8761` ports available. Stop the two background processes using the PIDs printed by the script.

An Ingress is a Kubernetes resource that describes HTTP/HTTPS routes from outside the cluster to Services inside it, commonly routing by hostname and URL path. It needs an Ingress Controller (such as Minikube's NGINX addon) to implement those rules; creating only an Ingress resource does not provide routing by itself.

To expose the gateway through Minikube instead of using port-forward, enable the controller and apply an Ingress resource like this:

```powershell
minikube addons enable ingress -p spring-microservices
minikube ip -p spring-microservices
```

Use the IP printed by the second command to add this line to the Windows hosts file (`C:\Windows\System32\drivers\etc\hosts`), replacing the placeholder with the actual IP:

```text
<MINIKUBE_IP> spring-microservices.test
```

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: spring-microservices
  namespace: spring-microservices
spec:
  ingressClassName: nginx
  rules:
    - host: spring-microservices.test
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: gateway-service
                port:
                  number: 8080
```

Save it as `ingress.yaml` and run `kubectl --context spring-microservices apply -f ingress.yaml`; then open `http://spring-microservices.test`. This optional example exposes only the gateway over HTTP and does not configure TLS. The normal lab does not install the addon, edit the hosts file, or create this resource.

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
