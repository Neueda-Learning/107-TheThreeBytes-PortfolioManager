# 📈 Portfolio Manager

A modern full-stack **Portfolio Management System** built with **Spring Boot**, **React (Vite)**, **MySQL**, and **Docker**. The application enables investors to manage their investment portfolios, monitor asset performance, analyze portfolio risk, estimate taxes, and track market prices through an intuitive dashboard.

Designed with a layered backend architecture and a responsive frontend, the project demonstrates modern software engineering practices including RESTful APIs, containerization, CI/CD, validation, and scalable application design.

---

# ✨ Features

## Portfolio Management
- Create, update, delete, and manage investment holdings
- Track Stocks, Bonds, and Cryptocurrency assets
- Automatic portfolio summary generation
- Cost basis calculation

## Transaction Management
- Record buy and sell transactions
- Transaction history
- Asset-wise transaction tracking
- Total transaction value calculation

## Dashboard & Analytics
- Portfolio overview dashboard
- Asset allocation visualization
- Portfolio value tracking
- Gain/Loss analysis
- Current vs invested value comparison

## Tax Estimation
- Short-term capital gains estimation
- Long-term capital gains estimation
- Estimated tax liability
- Gain-based tax calculations

## Watchlist
- Create and manage watchlists
- Track favorite assets
- Live market prices
- Quick asset monitoring

## Market Data
- Live market price retrieval
- Historical price tracking
- Portfolio valuation using live prices
- Graceful fallback when price service is unavailable

---

# 🏗️ Architecture

The application follows a layered architecture.

```
                React Frontend
                      │
                REST API (HTTP)
                      │
              Spring Boot Backend
                      │
        ┌─────────────┼─────────────┐
        │             │             │
  Controller      Service      Repository
        │             │             │
        └────────── Domain ─────────┘
                      │
                   MySQL
```

### Backend Layers

- **Controller** – REST API endpoints
- **Service** – Business logic and calculations
- **Repository** – Database operations using Spring Data JPA
- **Domain** – Entity models
- **DTO** – Request and response models
- **Exception** – Global exception handling
- **Configuration** – CORS and application configuration

---

# 🚀 Tech Stack

## Backend

- Java 21+
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Maven
- Bean Validation
- Lombok
- REST APIs

## Frontend

- React 19
- Vite
- React Router
- Axios
- Tailwind CSS
- Recharts
- Lucide React

## Database

- MySQL 8
- H2 (Development)

## DevOps

- Docker
- Docker Compose
- Jenkins

---

# 📂 Project Structure

```text
PortfolioManager/
│
├── backend/
│   ├── src/
│   ├── schema.sql
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── Dockerfile
│
├── Mobile/
│
├── docker-compose.yml
├── Jenkinsfile
└── README.md
```

---

# 📦 Core Modules

- Portfolio Management
- Transactions
- Dashboard
- Performance Analytics
- Risk Analysis
- Tax Estimation
- Watchlist
- Market Prices
- Portfolio Tracking
- Historical Portfolio Snapshots

---

# 📊 Domain Model

The application manages the following primary entities:

- Portfolio Items
- Transactions
- Dividend Records
- Watchlist Items

Supported asset types:

- Stocks
- Bonds
- Cryptocurrency

---

# 🌐 REST API Overview

## Portfolio

| Method | Endpoint |
|---------|----------|
| GET | `/api/portfolio-items` |
| GET | `/api/portfolio-items/{id}` |
| POST | `/api/portfolio-items` |
| PUT | `/api/portfolio-items/{id}` |
| DELETE | `/api/portfolio-items/{id}` |
| GET | `/api/portfolio-items/summary` |

---

## Transactions

| Method | Endpoint |
|---------|----------|
| GET | `/api/transactions` |
| GET | `/api/transactions/{id}` |
| POST | `/api/transactions` |
| DELETE | `/api/transactions/{id}` |

---

## Dashboard

| Method | Endpoint |
|---------|----------|
| GET | `/api/dashboard` |
| GET | `/api/dashboard/{assetType}` |

---

## Performance

| Method | Endpoint |
|---------|----------|
| GET | `/api/performance` |
| GET | `/api/performance/{id}` |

---

## Risk

| Method | Endpoint |
|---------|----------|
| GET | `/api/risk/analysis` |

---

## Tax

| Method | Endpoint |
|---------|----------|
| GET | `/api/tax/estimate` |

---

## Watchlist

| Method | Endpoint |
|---------|----------|
| GET | `/api/watchlist` |
| POST | `/api/watchlist` |
| DELETE | `/api/watchlist/{id}` |

---

## Prices

| Method | Endpoint |
|---------|----------|
| GET | `/api/prices/{ticker}` |
| GET | `/api/prices/history/{ticker}` |

---

# 🗄️ Database

The backend supports:

- MySQL 8 (Production)
- H2 In-Memory Database (Development)

The repository includes:

```
backend/schema.sql
```

for schema creation and initial seed data.

---

# ⚙️ Getting Started

## Prerequisites

- Java 21+
- Node.js 20+
- Maven
- MySQL 8
- Docker (Optional)

---

## Option 1 — Run with Docker

```bash
docker compose up --build
```

This starts:

- MySQL
- Spring Boot Backend
- React Frontend

---

## Option 2 — Local Development

### 1. Clone Repository

```bash
git clone <repository-url>

cd PortfolioManager
```

---

### 2. Configure Database

Create a MySQL database:

```sql
CREATE DATABASE portfolio_db;
```

Update the datasource configuration in:

```
backend/src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/portfolio_db
spring.datasource.username=root
spring.datasource.password=your_password
```

---

### 3. Start Backend

```bash
cd backend

mvn spring-boot:run
```

Backend runs on:

```
http://localhost:8080
```

Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

---

### 4. Start Frontend

```bash
cd frontend

npm install

npm run dev
```

Frontend:

```
http://localhost:5173
```

---

# 📈 Business Logic Highlights

- Automatic ticker normalization
- Portfolio summary aggregation
- Live market price integration
- Cost basis calculations
- Gain/Loss analysis
- Risk categorization
- Diversification scoring
- Tax estimation
- Asset allocation analysis

---

# 🔒 Validation & Error Handling

The backend includes:

- Bean Validation
- Global Exception Handling
- Standardized API error responses
- Request validation
- User-friendly validation messages

---

# 🧪 Testing

Backend includes:

- Spring Boot tests
- Integration tests
- Controller tests

Run tests:

```bash
cd backend

mvn test
```

---

# 🚀 CI/CD

The project includes:

- Docker support
- Docker Compose
- Jenkins Pipeline

---

# 🔮 Future Enhancements

- JWT Authentication & Authorization
- Spring Security
- Multiple portfolios per user
- Email notifications
- Price alerts
- News integration
- Advanced analytics
- CSV/Excel import & export
- Mobile application enhancements
- Portfolio benchmarking
- Audit logging
- Observability & monitoring

---

# 👥 Team

Developed collaboratively as part of a Portfolio Manager project.

---

# 📄 License

This project is intended for educational and learning purposes.
