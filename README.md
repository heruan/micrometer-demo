# micrometer-demo

A small Vaadin + Spring Boot 4 app wired up with Spring Boot Actuator and the
Micrometer Prometheus registry, plus a Docker Compose file to run Prometheus
locally and scrape the app.

## Stack

- Java 25
- Spring Boot 4.0.6
- Vaadin 25.1.5
- Micrometer Prometheus registry
- Gradle (Kotlin DSL) with version catalog (`gradle/libs.versions.toml`)

## Run the app

```
./gradlew bootRun
```

The UI is served on http://localhost:8080.

- http://localhost:8080/ — placeholder home view
- http://localhost:8080/people — Grid backed by `PersonService` whose methods are
  annotated with Micrometer's `@Observed`. Scrolling triggers paged fetches and
  feeds the `person.fetch` / `person.count` timers. The view also displays the
  current call count and average duration from the `MeterRegistry`.

Actuator endpoints exposed over HTTP (see `src/main/resources/application.yaml`):

- http://localhost:8080/actuator/health
- http://localhost:8080/actuator/info
- http://localhost:8080/actuator/prometheus

## Run Prometheus

A `compose.yaml` runs Prometheus and points it at the app on the host:

```
docker compose up -d
```

Then open http://localhost:9090. Under **Status → Target health** the `vaadin`
target should be `UP` within ~15s (the default scrape interval).

Grafana is available at http://localhost:3000 (anonymous Admin access enabled,
no login required). The Prometheus data source is auto-provisioned from
`grafana/provisioning/datasources/prometheus.yaml`.

Useful queries in the **Graph** tab:

- `jvm_memory_used_bytes`
- `http_server_requests_seconds_count`
- `process_cpu_usage`
- `person_fetch_seconds_count` — count of `@Observed` fetch calls
- `person_fetch_seconds_sum / person_fetch_seconds_count` — average fetch latency

Stop with:

```
docker compose down
```

## Project layout

```
build.gradle.kts              Gradle build
gradle/libs.versions.toml     Versions & dependencies
compose.yaml                  Prometheus container
prometheus.yml                Prometheus scrape config
src/main/java/                Application + Vaadin views
src/main/resources/           application.yaml
```
