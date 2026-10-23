# AI-CRM Spring Boot Backend

Production-ready Spring Boot backend for the AI-CRM platform with Spring Data MongoDB, RESTful APIs, intelligent AI recommendation engine, and seamless MongoDB Atlas support.

---

## 🚀 Quick Start

### 1. Run the Backend
From the `backend` directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

*(On Linux / macOS: `./mvnw spring-boot:run`)*

The server will start on **`http://localhost:8081`**.

---

## 🍃 Connecting MongoDB Atlas (When You Are Ready)

The backend is built to run immediately even before you add your Atlas URI (it defaults to graceful in-memory demo mode until you provide Atlas credentials).

When you're ready to connect MongoDB Atlas:

1. **Create / Open Cluster**: Log in to [MongoDB Atlas](https://cloud.mongodb.com) and create or select your cluster (Free M0 tier works great).
2. **Database Access**: Under **Security > Database Access**, create a user (e.g. `crm_user`) and password.
3. **Network Access**: Under **Security > Network Access**, click **Add IP Address** and choose **Allow Access from Anywhere** (`0.0.0.0/0`) for development.
4. **Get Connection String**:
   - Go to **Database > Clusters > Connect**
   - Choose **Drivers** (Java)
   - Copy the connection string, for example:
     ```
     mongodb+srv://crm_user:<password>@cluster0.abcde.mongodb.net/ai_crm?retryWrites=true&w=majority
     ```
5. **Add to Backend**:
   - **Option A (Recommended)**: Open `backend/src/main/resources/application.properties` and replace:
     ```properties
     spring.data.mongodb.uri=mongodb+srv://crm_user:YourPasswordHere@cluster0.abcde.mongodb.net/ai_crm?retryWrites=true&w=majority
     ```
   - **Option B (Environment Variable)**: Set the `MONGODB_URI` environment variable:
     ```powershell
     $env:MONGODB_URI="mongodb+srv://crm_user:YourPasswordHere@cluster0.abcde.mongodb.net/ai_crm?retryWrites=true&w=majority"
     .\mvnw.cmd spring-boot:run
     ```

---

## 📡 REST API Reference

All endpoints are prefixed with `/api` and support CORS for frontend applications on `http://localhost:5173` and `http://localhost:3000`.

### 👥 Customers (`/api/customers`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/customers` | Get all customers (supports `?search=query` for name, email, company) |
| `GET` | `/api/customers/{id}` | Get customer by ID |
| `POST` | `/api/customers` | Create a new customer (validates email uniqueness & required fields) |
| `PUT` | `/api/customers/{id}` | Update customer details |
| `DELETE` | `/api/customers/{id}` | Delete customer and cascades delete to associated leads |

#### Example: Create Customer Payload
```json
{
  "name": "Priya Sharma",
  "email": "priya@example.com",
  "company": "Bright Solutions",
  "phone": "9876543210"
}
```

---

### 💼 Leads / Opportunities (`/api/leads`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/leads` | Get all leads (supports `?customerId=...` and `?status=...`) |
| `GET` | `/api/leads/{id}` | Get lead by ID |
| `POST` | `/api/leads` | Create a new sales lead associated with a customer |
| `PUT` | `/api/leads/{id}` | Update opportunity title, value, status, or notes |
| `PATCH` | `/api/leads/{id}/status` | Fast update of lead status (`New`, `Contacted`, `Converted`) |
| `DELETE` | `/api/leads/{id}` | Delete a lead |

#### Example: Create Lead Payload
```json
{
  "customerId": "1",
  "title": "Website Development",
  "value": 25000.0,
  "status": "New"
}
```

#### Example: Update Lead Status Payload (`PATCH /api/leads/{id}/status`)
```json
{
  "status": "Contacted"
}
```

---

### ✦ AI Insights (`/api/ai`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/ai/insights` | Generates prioritized intelligent recommendations for all pipeline leads |
| `POST` | `/api/ai/analyze` | Evaluates a custom lead payload and returns instant win probability & action plan |

#### Example AI Insight Response:
```json
[
  {
    "leadId": "101",
    "customerName": "Priya Sharma",
    "opportunityTitle": "Website Development",
    "opportunityValue": 25000.0,
    "status": "New",
    "title": "Initial Outreach Required",
    "description": "Introduce your solution to Priya Sharma and understand their project requirements.",
    "action": "Send Introduction Email & Product Overview",
    "priority": "MEDIUM",
    "score": 45
  }
]
```

---

### 📊 Dashboard Analytics (`/api/dashboard/stats`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard/stats` | Aggregated metrics: total customers, total leads, conversion rate, pipeline value, status breakdown |

#### Example Dashboard Stats Response:
```json
{
  "totalCustomers": 2,
  "totalLeads": 2,
  "convertedLeads": 0,
  "conversionRate": 0,
  "totalPipelineValue": 40000.0,
  "statusCounts": {
    "New": 1,
    "Contacted": 1,
    "Converted": 0
  },
  "databaseStatus": "MongoDB Atlas (Connected)"
}
```

---

### 🩺 Health & System Status (`/api/system/status` & `/api/health`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Lightweight health check reporting uptime & database connectivity |
| `GET` | `/api/system/status` | Diagnostic information including active Java runtime, masked MongoDB URI, and setup status |

---

## 🏗️ Project Architecture

```
backend/
├── pom.xml                               # Maven build configuration (Java 25, Spring Boot 4.1.2)
├── mvnw & mvnw.cmd                       # Maven wrappers (runs without needing local Maven installed)
├── .env.example                          # Example environment variables
└── src/
    ├── main/
    │   ├── java/com/aicrm/
    │   │   ├── AiCrmBackendApplication.java # Spring Boot main entry point
    │   │   ├── config/
    │   │   │   └── CorsConfig.java          # Cross-Origin Resource Sharing setup
    │   │   ├── controller/
    │   │   │   ├── CustomerController.java  # Customer REST API
    │   │   │   ├── LeadController.java      # Lead REST API
    │   │   │   ├── AiInsightController.java # AI Insights API
    │   │   │   ├── DashboardController.java # Dashboard metrics API
    │   │   │   └── SystemStatusController.java # Health & Atlas status API
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java # Friendly error handling
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   └── DuplicateResourceException.java
    │   │   ├── model/
    │   │   │   ├── Customer.java            # MongoDB @Document collection("customers")
    │   │   │   ├── Lead.java                # MongoDB @Document collection("leads")
    │   │   │   ├── AiInsight.java           # AI Insight DTO
    │   │   │   └── DashboardStats.java      # Dashboard metrics DTO
    │   │   ├── repository/
    │   │   │   ├── CustomerRepository.java  # Spring Data MongoRepository
    │   │   │   └── LeadRepository.java      # Spring Data MongoRepository
    │   │   └── service/
    │   │       ├── CustomerService.java     # Business logic with Atlas & fallback
    │   │       ├── LeadService.java         # Opportunity pipeline logic
    │   │       ├── AiInsightService.java    # AI recommendation engine
    │   │       ├── DashboardService.java    # Metric aggregation
    │   │       └── MongoHealthService.java  # Connection probe & status checker
    │   └── resources/
    │       └── application.properties       # Spring & MongoDB Atlas configuration
    └── test/
```
