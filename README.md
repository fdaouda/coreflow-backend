# 🚀 CoreFlow — Plateforme SaaS de Gestion de Commandes, Événements & Agent IA

[![Build Status](https://github.com/fdaouda/coreflow-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/fdaouda/coreflow-backend/actions)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-green)
![Angular](https://img.shields.io/badge/Angular-18-red)
![Architecture](https://img.shields.io/badge/Architecture-Event--Driven-blue)

**CoreFlow** est une plateforme SaaS Fullstack événementielle conçue pour le traitement de commandes à haut débit, le messaging résilient et l'automatisation par Agent IA[cite: 1, 3].

Le projet est structuré en **deux dépôts distincts (Multi-Repository Architecture)** :

1. **`coreflow-backend`** : Backend Spring Boot 3.5.14 événementiel avec Agent IA (Spring AI `1.0.0`), brokers (Kafka & RabbitMQ), Flyway migrations, stockage Cloud AWS (`eu-west-3`, S3 & CloudWatch) et observabilité[cite: 7, 8, 9].
2. **`coreflow-frontend`** : Single Page Application (SPA) Angular 18 basée sur l'architecture Standalone (`AppComponent`), les Signals et RxJS[cite: 1, 3].

---

## 📂 Structure Multi-Dépôts GitHub

```text
├── 📦 fdaouda/coreflow-backend (Dépôt API & Services Cloud)
│   ├── Spring Boot 3.5.14 (Java 17, Spring Data JPA, Spring AI 1.0.0, Flyway 12.5.0)
│   ├── Messaging (Apache Kafka & RabbitMQ Broker avec DLQ)
│   ├── AWS SDK v2 v2.51.x (Bucket 'coreflow-invoices', CloudWatch Audit Logs)
│   ├── Observabilité (Actuator, Micrometer Prometheus, Grafana)
│   └── Multi-Stage Dockerfile & GitHub Actions CI Pipeline
│
└── 📦 fdaouda/coreflow-frontend (Dépôt Client SPA)
    ├── Angular 18 (Standalone Architecture - AppComponent unique)
    ├── Interface de gestion des commandes & prévisualisation factures
    ├── Module Chat IA conversationnel connecté à Spring AI
    └── Intégration CORS autorisée pour http://localhost:4200

```

---

## 📐 Architecture Globale du Système

```text
       +-------------------------------------------------------------------+
       |                 coreflow-frontend (Angular 18)                    |
       |        (Standalone AppComponent & Signals @ localhost:4200)        |
       +---------------------------------+---------------------------------+
                                         |
                                         | HTTP REST (OpenAPI 3.1 & CORS)
                                         v
       +-------------------------------------------------------------------+
       |                    coreflow-backend (Spring Boot 3)               |
       |                                                                   |
       |  +--------------------+   +-------------------+   +-------------+ |
       |  |  Order Controller  |   | InvoiceController |   | AI Agent    | |
       |  |     (/orders)      |   |   (/invoice/{id}) |   | (/api/ai)   | |
       |  +---------+----------+   +---------+---------+   +------+------+ |
       +------------|------------------------|--------------------|--------+
                    |                        |                    |
  +-----------------+--------------+         |                    | Spring AI (Function Calling)
  |                 |              |         |                    v
  v                 v              v         v                 +----------------------+
+-----------+   +---------+   +--------+  +-----------+        | OpenAI gpt-4o-mini   |
|PostgreSQL |   | Kafka   |   |RabbitMQ|  | AWS S3    |        | (Spring AI Tools)    |
|(Port 5432)|   |(p: 9092)|   |(p:5672)|  |(Port 4566)|        +----------------------+
+-----------+   +----+----+   +---+----+  +-----+-----+                   |
                     |            |             |             +-----------+----------+
                     v            v             v             | OrderAiToolsConfig   |
               +--------------------+   +---------------+     | - getOrderStatus     |
               | Retry & DLQ        |   | AWS           |     | - getByCustomerId    |
               | (orders-dlt)       |   | CloudWatch    |     +----------------------+
               +--------------------+   +---------------+
                         |
                         v
                +-------------------+
                | Prometheus        | ---> Grafana Dashboards
                | (Port 9090)       |      (Port 3000 - JVM / HTTP Metrics)
                +-------------------+

```

---

## ⚙️ Configuration & Variables d'Environnement (`.env` / `application.yml`)

Afin d'exécuter le projet en local ou en intégration continue, créez un fichier `.env` à la racine de `coreflow-backend` ou configurez les variables d'environnement suivantes :

```bash
# Clé API OpenAI pour Spring AI (Modèle gpt-4o-mini, temp: 0.2)
OPENAI_API_KEY=sk-proj-YOUR_OPENAI_KEY_HERE

# Authentification LocalStack (AWS S3, CloudWatch, Lambda, SQS en Local)
LOCALSTACK_AUTH_TOKEN=ls-YOUR_LOCALSTACK_TOKEN_HERE

# Configuration Base de données PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/coreflow
SPRING_DATASOURCE_USERNAME=coreflow
SPRING_DATASOURCE_PASSWORD=coreflow

```

---

## 🤖 Agent IA Spring AI & Function Calling (`OrderAiToolsConfig`)

L'agent IA s'appuie sur `spring-ai-starter-model-openai` (modèle **`gpt-4o-mini`**) et utilise le mécanisme de **Function Calling / Tools** (`@Tool`) :

* **`getOrderStatusTool(orderId: String)`** : Interroge `OrderService` pour récupérer le statut d'une commande (`CREATED`, `PROCESSING`, `COMPLETED`, etc.) via son identifiant UUID (`OrderStatusResponse`).
* **`getOrderByCustomerId(customerId: UUID)`** : Récupère la liste de tous les UUIDs de commandes associés à un identifiant client (`customerId`).

---

## 📨 Topologie Événementielle & Résilience

### 1. Apache Kafka Consumer (`OrderKafkaConsumer`)



* **Bootstrap Server :** `localhost:9092` | **Topic principal :** `orders` | **Consumer Group :** `coreflow-group`

* **Stratégie de Retry :** Annoté `@RetryableTopic` avec **5 tentatives** et un backoff exponentiel (`delay = 1000ms`, `multiplier = 2.0`).


* **Dead Letter Topic (DLQ) :** En cas d'échec répété, le message est automatiquement routé vers le topic `orders-dlt` géré par `@DltHandler`.



### 2. RabbitMQ Broker Topologie (`RabbitMQConfig`)



* **Host :** `localhost:5672` (AMQP) | Dashboard UI : `http://localhost:15672` (`guest`/`guest`)


* **Exchange :** `orders.notifications.exchange` (TopicExchange)


* **Routing Key :** `notification.order.created`

* **Queue :** `orders.notifications.queue` (Durable).



---

## 🔌 API Documentation (Endpoints OpenAPI 3.1)

L'API backend est documentée via SpringDoc OpenAPI `2.8.0` et accessible sur `http://localhost:8080/swagger-ui/index.html`.

### 📦 Order Management (`/orders`) — `@Tag("Order Management")`

* **`POST /orders`** — *Créer une nouvelle commande*

* Request Body : `@Valid CreateOrderRequest`
* Response : `HTTP 201 Created` avec l'en-tête `Location` (`/orders/{id}`) et `OrderResponse`.


* **`GET /orders`** — *Récupérer toutes les commandes*

* Response : `HTTP 200 OK` (`List<OrderResponse>`).




* **`GET /orders/{id}`** — *Récupérer commandes utilisateur*

* Parameter : `UUID id`
* Response : `HTTP 200 OK` (`OrderResponse`).





### 🤖 Agent IA CoreFlow (`/api/ai/chat`) — `@Tag("Agent IA CoreFlow")`

* **`POST /api/ai/chat`** — *Interroger l'agent IA CoreFlow*

* Permet d'interagir en langage naturel avec l'assistant `gpt-4o-mini`. L'agent utilise le *Function Calling* pour interroger la base de données via `OrderAiToolsConfig`.





### 🧾 Invoice Controller (`/invoice`)



* **`POST /invoice/{id}`** — *Générer et stocker la facture d'une commande sur S3*

* Parameter : `Long id`
* Action 1 : Génération et upload du PDF (`invoices/2026/facture-order-{id}.pdf`) vers **AWS S3** (`coreflow-invoices` en région `eu-west-3`).


* Action 2 : Émission d'un log d'audit JSON (`INVOICE_GENERATED`) vers **AWS CloudWatch Logs**.



---

## 🧪 Stratégie de Test & Isolation

Le projet met en œuvre deux niveaux de tests automatisés afin de garantir la qualité logicielle et la rapidité des builds CI/CD :

### 1. Tests Unitaires isolés (`OrderServiceTest`)

* **Stack :** JUnit 5, Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`), AssertJ (`assertThat`, `assertThatThrownBy`).
* **Couverture Métier :**
* `createOrder_shouldSaveAndReturnOrder()` : Validation de la persistance via `OrderRepository` et émission d'événements.
* `getOrderById_whenFound_shouldReturnOrder()` : Lecture d'une commande par son UUID.
* `getOrderById_whenNotFound_shouldThrowException()` : Assertion sur le levé d'une `ResponseStatusException` pour un ID inexistant.
* `getAllOrders_shouldReturn_listOfOrders()` : Vérification de la récupération de la liste des commandes.



### 2. Isolation des Tests d'Intégration Contextuelle (`CoreflowBackendApplicationTests`)

* **Profil de test :** `@ActiveProfiles("test")`.


* **Base de données H2 RAM :** URL `jdbc:h2:mem:testdb` en mode PostgreSQL avec Flyway désactivé.


* **Listener Messaging :** Les listeners Kafka et RabbitMQ ont l'option `auto-startup: false` pour éviter de bloquer l'initialisation.


* **Mocking Cloud AWS :** Les clients Cloud AWS (`S3Client`, `CloudWatchLogsClient`) sont simulés via `@Mock` pour éliminer tout appel HTTP réseau externe vers LocalStack ou AWS pendant les builds.



---

## 🐳 Infrastructure Conteneurisée (`docker-compose.yml`)



L'infrastructure repose sur 7 conteneurs Docker orchestrés :

| Service | Image Docker | Port(s) Exposé(s) | Description |
| --- | --- | --- | --- |
| **PostgreSQL** | `postgres:15` | `5432` | BDD Principale (`coreflow` / `coreflow`)
|
| **Zookeeper** | `confluentinc/cp-zookeeper:7.5.0` | `2181` | Coordination du cluster Kafka
|
| **Kafka** | `confluentinc/cp-kafka:7.5.0` | `9092` | Broker de messages événementiels
|
| **RabbitMQ** | `rabbitmq:3-management` | `5672`, `15672` | Messaging asynchrone & Dashboard UI
|
| **Prometheus** | `prom/prometheus:latest` | `9090` | Collecte des métriques Spring Actuator
|
| **Grafana** | `grafana/grafana:latest` | `3000` | Dashboards de monitoring JVM & HTTP (`admin`/`admin`)
|
| **LocalStack** | `localstack/localstack` | `4566` | Émulation Cloud AWS (S3, CloudWatch, Lambda, SQS)
|
---

## 🛠️ Stack Technique Globale

* **Backend (`coreflow-backend`) :** Java 17, Spring Boot 3.5.14, Spring Data JPA, Flyway 12.5.0 (Migrations SQL), Spring AI 1.0.0 (`gpt-4o-mini`), SpringDoc OpenAPI 2.8.0, Lombok, Dotenv.


* **Messaging & Résilience :**
* **Apache Kafka :** Consumer avec `@RetryableTopic` (5 tentatives, Backoff x2) et gestion de DLT (`orders-dlt`).


* **RabbitMQ :** TopicExchange (`orders.notifications.exchange`), Queue (`orders.notifications.queue`), Routing Key (`notification.order.created`).




* **Frontend (`coreflow-frontend`) :** Angular 18 (Architecture Standalone centralisée sur `AppComponent`, Angular Signals, RxJS).


* **Cloud AWS (AWS SDK v2.51.x) :**

* **AWS S3 :** Bucket `coreflow-invoices` (Région `eu-west-3`).


* **AWS CloudWatch :** Journalisation d'audit JSON.




* **Observabilité :** Spring Boot Actuator, Micrometer Prometheus, Dashboards Grafana.


* **DevOps & CI/CD :** Docker Multi-stage build, Docker Compose, Pipeline GitHub Actions (`ci.yml`).



---

## ⚡ Démarrage Rapide

### Prérequis

* **Docker & Docker Compose**
* **Java 17+** & **Maven 3.9+**
* **Node.js 20+** & **Angular CLI 18**

### 1. Démarrer l'infrastructure & le Backend (`coreflow-backend`)

```bash
# Cloner le dépôt backend
git clone [https://github.com/fdaouda/coreflow-backend.git](https://github.com/fdaouda/coreflow-backend.git)
cd coreflow-backend

# Lancer toute l'infrastructure Docker (Postgres, Kafka, RabbitMQ, Prometheus, Grafana, LocalStack)
docker-compose up -d

# Configurer la clé OpenAI
export OPENAI_API_KEY=sk-proj-YOUR_KEY

# Lancer l'application Spring Boot
./mvnw spring-boot:run

```

* **Swagger UI :** `http://localhost:8080/swagger-ui/index.html`

* **Prometheus Metrics :** `http://localhost:8080/actuator/prometheus`
* **Grafana Dashboard :** `http://localhost:3000` (`admin` / `admin`)


* **RabbitMQ UI :** `http://localhost:15672` (`guest` / `guest`)



### 2. Démarrer le Frontend (`coreflow-frontend`)

```bash
# Cloner le dépôt frontend
git clone [https://github.com/fdaouda/coreflow-frontend.git](https://github.com/fdaouda/coreflow-frontend.git)
cd coreflow-frontend

# Installer les dépendances et démarrer le serveur Angular
npm install
ng serve

```

Accéder à l'application web sur **`http://localhost:4200`**.

---

## 🧪 Pipeline CI/CD (`coreflow-backend`)

Le pipeline GitHub Actions (`.github/workflows/ci.yml`) s'exécute automatiquement à chaque push sur `main` :

1. **Compilation & Cache :** Validation sous Java 17 avec cache Maven.
2. **Tests isolés :** Exécution de la suite `OrderServiceTest` et validation du contexte Spring Boot isolée via `@ActiveProfiles("test")`.


3. **Docker Multi-Stage Build :** Compilation isolée du JAR (`maven:3.9-eclipse-temurin-17`) et conteneurisation d'une image Alpine ultra-légère (`eclipse-temurin:17-jdk-alpine`).

```
