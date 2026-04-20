# Bank Card Management System (2026)

REST API built with **Spring Boot 4** for managing bank cards, users, and fund transfers.

## ✨ Features

Spring Security authentication and authorization. Role-based accessing the API using a JWT token.  
Filtering and paginated search of users, cards, bank transactions, card-blocking requests, data encryption, masking card
numbers.   
Error handling, validation of incoming data, logging, Unit-tests.

| Role      | Capability                                         |
|-----------|----------------------------------------------------|
| **ADMIN** | Create, block, activate, delete cards              |
| **ADMIN** | Manage users (list, toggle, delete)                |
| **ADMIN** | View all cards with filtering                      |
| **USER**  | View own cards (search + pagination), card balance |
| **USER**  | Transfer between own cards                         |
| **USER**  | Request card block                                 |
| **USER**  | Change own password                                |

**Security highlights:**

- JWT authentication
- Card numbers AES-encrypted
- Masked display (`**** **** **** 1234`)
- Role-based access control (ADMIN / USER)
- BCrypt password hashing

---

## 🛠 Tech Stack

| Layer            | Technology                   |
|------------------|------------------------------|
| Development tool | Java 25                      |
| Framework        | Spring Boot 4                |
| Security         | Spring Security + JWT (jjwt) |
| Persistence      | Spring Data JPA + PostgreSQL |
| Migrations       | Liquibase                    |
| Docs             | SpringDoc OpenAPI / Swagger  |
| Build            | Maven                        |
| Container        | Docker + Docker Compose      |
| Testing          | JUnit 5 + Mockito + MockMvc  |

---

## 🚀 Quick Start

### Important:

You can connect to the PostgreSQL database inside the container using localhost:5433 (to avoid conflicts if port 5432 is
already in use by a locally installed PostgreSQL instance).

There must be a .env file in the root of the project with specified parameters  
DB_USER=  
DB_PASS=  
JWT_SECRET=  
ENCRYPTION_KEY=  
KEY_SALT=

The JWT_SECRET and ENCRYPTION_KEY must be min 32 chars, and the KEY_SALT must be min 16 chars.  
The DB_USER and DB_PASS environment variables are used for database access, JWT_SECRET is used for generating access
tokens, and ENCRYPTION_KEY and KEY_SALT are used for encrypting card numbers.

### Option A — Docker Compose

**Prerequisites:** Docker + Docker Compose installed.

```bash
# 1. Clone the project
git clone <repo-url>
cd bank_rest

# 2. Start everything (PostgreSQL + app)
docker-compose up --build

# 3. API is ready at:
#    http://localhost:8080
#    Swagger UI: http://localhost:8080/swagger-ui.html
```

### Option B — Run locally

**Prerequisites:** Java 25+, Maven, PostgreSQL running.

```bash
# 1. Create database (with DB_USER and DB_PASS from .env file)
psql -U postgres -c "CREATE DATABASE bankrest;"
psql -U postgres -c "CREATE USER dbuser WITH PASSWORD 'dbpass';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE bankrest TO dbuser;"

# 2. Build and run
mvn spring-boot:run

# 3. API is ready at:
#    http://localhost:8080
#    Swagger UI: http://localhost:8080/swagger-ui.html
```

During the build, an initial user with the ADMIN role is added to the database (Liquibase migration file
`005-insert-admin.yaml`):

| Username | Password     | Role  |
|----------|--------------|-------|
| `admin`  | `Pass_admin` | ADMIN |

After authentication via the /api/auth/login endpoint, a JWT token is returned, which can be used to log into the
system (in Swagger, use the Authorize button in the upper right corner) and make necessary requests.
The token lifetime in milliseconds is set in the access-token-expiration parameter in the
`src/main/resources/application.yml` settings.
---

## 📖 API Reference

Full OpenAPI spec: [`docs/openapi.yaml`](docs/openapi.yaml)

The full list of endpoints is available at the link of Interactive Swagger UI:  
`http://localhost:8080/swagger-ui.html`
---

The project was published:
https://github.com/sol1772/bank_rest

**Author: Maksyutov Salavat**
___
___

# Система управления банковскими картами (2026)

REST API, созданный с использованием **Spring Boot 4**, предназначен для управления банковскими картами, пользователями
и денежными переводами.

## ✨ Функционал

Аутентификация и авторизация пользователей по логину и паролю с помощью Spring Security.
Доступ на основе ролей к API с помощью JWT токена.  
Фильтрация и постраничный поиск пользователей, карт, банковских транзакций, запросов на блокировку карт, шифрование
данных, маскировка номеров карт.
Обработка ошибок, валидация входящих данных, логирование операций, Unit-тестирование.

| Роль      | Возможности                                                  |
|-----------|--------------------------------------------------------------|
| **ADMIN** | Создание, блокировка, активация, удаление карт               |
| **ADMIN** | Управление пользователями (создание, чтение, удаление)       |
| **ADMIN** | Просмотр всех карт с фильтрацией                             |
| **USER**  | Просмотр собственных карт (поиск + пагинация), баланса карты |
| **USER**  | Переводы между своими картами                                |
| **USER**  | Запрос на блокировку карты                                   |
| **USER**  | Изменение своего пароля                                      |

**Безопасность:**

- JWT аутентификация
- AES-шифрование номеров карт
- Маскирование номеров карт (`**** **** **** 1234`)
- Управление доступом на основе ролей (ADMIN / USER)
- BCrypt хэширование пароля

---

## 🛠 Стек

| Layer              | Технологии                   |
|--------------------|------------------------------|
| Программная среда  | Java 25                      |
| Фреймворк          | Spring Boot 4                |
| Безопасность       | Spring Security + JWT (jjwt) |
| Управление данными | Spring Data JPA + PostgreSQL |
| Миграции           | Liquibase                    |
| Документация       | SpringDoc OpenAPI / Swagger  |
| Сборка проекта     | Maven                        |
| Развёртывание      | Docker + Docker Compose      |
| Тестирование       | JUnit 5 + Mockito + MockMvc  |

---

## 🚀 Запуск проекта

### Важно:

Подключиться к базе данных PostgreSQL внутри контейнера можно по адресу localhost:5433 (чтобы избежать конфликтов, если
порт 5432 уже занят локально установленным экземпляром PostgreSQL).

В корне проекта должен находиться файл .env с заполненными параметрами.  
DB_USER=  
DB_PASS=  
JWT_SECRET=  
ENCRYPTION_KEY=  
KEY_SALT=

Длина JWT_SECRET и ENCRYPTION_KEY минимум 32 символа, KEY_SALT минимум 16 символов.  
Переменные окружения DB_USER и DB_PASS предназначены для доступа к БД, JWT_SECRET - для генерации токенов доступа,
ENCRYPTION_KEY, KEY_SALT - для шифрования номеров карт.

### Вариант A — запуск с помощью Docker Compose

**Предварительные требования:** должны быть установлены Docker + Docker Compose.

```bash
# 1. Клонируйте проект
git clone <repo-url>
cd bank_rest

# 2. Запустите всё (PostgreSQL + приложение).
docker-compose up --build

# 3. API доступен по адресу:
#    http://localhost:8080
#    Swagger UI: http://localhost:8080/swagger-ui.html
```

### Вариант B — Локальный запуск

**Предварительные требования:** Java 25+, Maven, PostgreSQL.

```bash
# 1. Создайте базу данных (со значениями параметров DB_USER and DB_PASS из файла .env)
psql -U postgres -c "CREATE DATABASE bankrest;"
psql -U postgres -c "CREATE USER dbuser WITH PASSWORD 'dbpass';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE bankrest TO dbuser;"

# 2. Соберите и запустите проект
mvn spring-boot:run

# 3. API доступен по адресу:
#    http://localhost:8080
#    Swagger UI: http://localhost:8080/swagger-ui.html
```

При сборке в БД добавляется начальный пользователь с ролью ADMIN (файл миграции Liquibase `005-insert-admin.yaml`):

| Username | Password     | Role  |
|----------|--------------|-------|
| `admin`  | `Pass_admin` | ADMIN |

После аутентификации через эндпоинт `/api/auth/login` возвращается JWT-токен, с помощью которого можно авторизоваться в
системе (в Swagger - кнопка Authorize в правом верхнем углу) и выполнять необходимые запросы.
Время жизни токена в миллисекундах задается в параметре `access-token-expiration` в настройках
`(src/main/resources/application.yml)`
---

## 📖 Справка по API

Полная OpenAPI спецификация: [`docs/openapi.yaml`](docs/openapi.yaml)

Полный список эндпойнтов доступен по ссылке Swagger UI:  
`http://localhost:8080/swagger-ui.html`

---
Проект опубликован:
https://github.com/sol1772/bank_rest

**Автор: Максютов Салават**
  
