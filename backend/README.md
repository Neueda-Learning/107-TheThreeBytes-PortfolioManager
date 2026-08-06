# Backend Environment Setup

Copy `backend/.env.example` to `backend/.env` and fill sensitive values.

## Required Variables

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `FINNHUB_API_KEY` (optional for local; stock data falls back when empty)

## Optional Variables

- `SERVER_PORT`
- `APP_CORS_ALLOWED_ORIGINS`
- `FINNHUB_API_BASE_URL`
- `FINNHUB_CACHE_QUOTE_TTL_SECONDS`
- `FINNHUB_CACHE_CANDLE_TTL_HOURS`
- `MYSQL_ROOT_PASSWORD`
- `MYSQL_DATABASE`

## Local Run (PowerShell)

```powershell
Push-Location backend
.\mvnw.cmd spring-boot:run
Pop-Location
```

## Docker Compose

`docker-compose.yml` is wired to `backend/.env` and uses env placeholders for secrets.

