# 💳 Bank Cards Management API

Production-style REST API for managing bank cards, users, and money transfers.  
Built with a focus on security, clean architecture, and real-world backend practices.
---

## ✨ Why this project?

This is not just a CRUD app. It demonstrates:

* 🔐 Security-first design (JWT, encryption, access control)
* 🧱 Layered architecture (Controller → Service → Repository)
* ⚙️ Production-ready practices (Liquibase, Docker, validation, logging)
* 🧪 Test coverage (unit + security + controller tests)

---

## ✨ Features

### 👤 Authentication & Authorization

- JWT-based authentication
- Role-based access control (ADMIN / USER)
- Secure password hashing (BCrypt)

### 💳 Card Management

- Create / activate / block / delete cards (ADMIN)
- View own cards with pagination (USER)
- Balance tracking

### 💸 Transactions

- Transfer funds between user cards
- Transaction history with filtering

### 🔒 Security

- AES encryption of card numbers
- Masking card numbers (**** **** **** 1234)
- Method-level security (@PreAuthorize)
- Input validation & centralized exception handling

### 📊 Additional

- Pagination & filtering
- Logging of business operations
- OpenAPI (Swagger) documentation

---

## 🛠 Tech Stack

| Layer      | Technology                   |
|------------|------------------------------|
| Language   | Java 25                      |
| Framework  | Spring Boot 4                |
| Security   | Spring Security + JWT        |
| Data       | Spring Data JPA + PostgreSQL |
| Migrations | Liquibase                    |
| Testing    | JUnit 5 + Mockito + MockMvc  |
| Docs       | OpenAPI (Swagger)            |
| Build      | Maven                        |
| Deployment | Docker + Docker Compose      |

---

## ⚙️ Getting Started

### 🔑 Requirements

Create `.env` file in project root:  
DB_USER=  
DB_PASS=  
JWT_SECRET=  
ENCRYPTION_KEY=  
KEY_SALT=

**Constraints**:

* JWT_SECRET, ENCRYPTION_KEY ≥ 32 chars
* KEY_SALT ≥ 16 chars

---

## 🐳 Run with Docker (recommended)

```bash
git clone <repo-url>
cd bank_rest
docker-compose up --build
```

**Access:**

* API → http://localhost:8080
* Swagger → http://localhost:8080/swagger-ui.html
* PostgreSQL → localhost:5433

---

## 💻 Run locally

```bash
# create DB
psql -U postgres -c "CREATE DATABASE bankrest;"
psql -U postgres -c "CREATE USER dbuser WITH PASSWORD 'dbpass';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE bankrest TO dbuser;"

# run app
mvn spring-boot:run
```

---

## 🔐 Default Admin

Created automatically via Liquibase:

| Username | Password     | Role  |
|----------|--------------|-------|
| `admin`  | `Pass_admin` | ADMIN |

---

## 🔑 Authentication Flow

1. Call: `POST /api/auth/login`
2. Receive JWT token
3. Use it in Swagger → Authorize

---

## 📖 API Documentation

* OpenAPI spec → `docs/openapi.yaml`
* Swagger UI → http://localhost:8080/swagger-ui.html

---

## 🧪 Testing

Includes:

* Unit tests (Mockito)
* Controller tests (MockMvc)
* Security tests (@WithMockUser)

---

## 🧱 Architecture

Controller → Service → Repository → DB

**Principles used:**

* Separation of concerns
* Interface-based services
* DTO mapping layer
* Transaction management
* Validation at service level

---

## 🔄 Database Management

* Managed via **Liquibase**
* Includes:
    * schema creation
    * constraints
    * initial admin user
* Uses **preConditions** for safe migrations

---

## 🧠 What I focused on

* Writing **clean, readable, production-like code**
* Avoiding "toy project" patterns
* Handling real-world concerns:
    * security
    * data integrity
    * migrations
    * testing

---

## 📌 Notes

* Card numbers and CVC are **encrypted**, not stored in plain text
* CVC is **not exposed** after creation (in real banking apps cvc is stored in the Hardware Security Module)
* API enforces strict access control

---

## 💬 Final note

This project was built as a **serious backend exercise**, not just a demo.  
If you're looking for someone who understands not only how to code, but also why systems are built this way — this
project reflects that.

## 👨‍💻 Author

Maksyutov Salavat

Backend developer focused on:

* Java & Spring ecosystem
* Clean architecture
* Writing maintainable production code

https://github.com/sol1772/bank_rest

