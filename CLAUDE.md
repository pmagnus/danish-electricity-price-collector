# CLAUDE.md - AI Assistant Guide

## Project Overview

Danish Electricity Price Collector is a **Spring Boot 3.2** application that fetches, stores, and displays Danish electricity spot prices with full tariff breakdowns. It pulls real-time pricing data from the **elprisenligenu.dk** public API, adds Danish transmission/system tariffs and electricity tax, and presents it through a server-rendered web dashboard.

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.0
- **Build tool**: Maven (with Maven Wrapper)
- **Database**: PostgreSQL 15
- **Templating**: Thymeleaf + HTMX 1.9.8 + Tailwind CSS 3
- **Migrations**: Flyway

## Repository Structure

```
src/main/java/dk/electricity/pricecollector/
├── DanishElectricityPriceCollectorApplication.java  # Entry point (@SpringBootApplication, @EnableScheduling)
├── config/
│   └── StartupDataInitializer.java                  # Fetches prices on ApplicationReadyEvent
├── controller/
│   └── DashboardController.java                     # Web pages + HTMX fragments + JSON API + debug endpoints
├── model/
│   └── ElectricityPrice.java                        # JPA entity for electricity_prices table
├── repository/
│   └── ElectricityPriceRepository.java              # Spring Data JPA with native SQL queries
└── service/
    ├── ElectricityPriceService.java                 # Business logic, summaries, CRUD operations
    ├── ElprisenLigenuService.java                   # External API client (elprisenligenu.dk)
    └── PriceScheduler.java                          # Cron-based scheduled price fetching

src/main/resources/
├── application.yml                                  # App config (datasource, JPA, Flyway, logging)
├── db/migration/
│   ├── V1__Create_electricity_prices_table.sql       # Schema + core indexes
│   └── V2__Add_performance_indexes.sql               # Additional performance indexes
├── static/
│   ├── css/app.css
│   └── js/app.js
└── templates/
    ├── layout.html                                   # Thymeleaf base layout
    ├── dashboard.html                                # Main dashboard page
    ├── prices.html                                   # Detailed prices with region selector
    └── test-prices.html                              # Debug/test page
```

## Build and Run Commands

```bash
# Build the project (skip tests since none exist yet)
./mvnw clean compile

# Package as JAR
./mvnw clean package -DskipTests

# Run the application (requires PostgreSQL)
./mvnw spring-boot:run

# Start PostgreSQL with Docker
docker-compose up -d
```

The app starts on **port 8080**.

## Architecture

### Layered Architecture

```
Controller (DashboardController)
    ↓
Service (ElectricityPriceService, ElprisenLigenuService)
    ↓
Repository (ElectricityPriceRepository)
    ↓
Database (PostgreSQL via Flyway migrations)
```

### Key Architectural Decisions

- **Single controller** handles all endpoints: full pages, HTMX fragments, JSON API, and debug/fetch triggers
- **Two service classes**: `ElectricityPriceService` for business logic/DB operations, `ElprisenLigenuService` for external API integration
- **Scheduler** (`PriceScheduler`) runs cron jobs daily at 13:05-13:15 to fetch prices after the market publishes them at 13:00
- **Startup initializer** fetches prices on app boot via `ApplicationReadyEvent`
- **HTMX** is used for partial page updates without full JavaScript SPA complexity
- **Tailwind CSS** via CDN for styling (no build step required for frontend)

### Data Flow

1. External API (`elprisenligenu.dk`) provides spot prices in DKK/kWh
2. `ElprisenLigenuService` fetches spot prices and adds fixed tariffs:
   - Transmission tariff: 0.058 DKK/kWh
   - System tariff: 0.0125 DKK/kWh
   - Electricity tax: 0.090 DKK/kWh
3. `ElectricityPrice` entity auto-calculates `totalPrice` via `@PrePersist`/`@PreUpdate`
4. Prices are stored per hour (0-23) per region (DK1/DK2) per date
5. Dashboard renders 24-hour tables with color-coded cheapest hours (green gradient for top 8)

### Regions

- **DK1** - West Denmark (default)
- **DK2** - East Denmark

## Database

### Schema

Single table `electricity_prices` with columns: `id`, `price_date_time`, `price_date`, `hour` (0-23), `spot_price`, `transmission_tariff`, `system_tariff`, `electricity_tax`, `total_price`, `region`, `created_at`, `updated_at`.

All monetary values use `NUMERIC(10,6)` (mapped to `BigDecimal` in Java).

### Migrations

Flyway migrations live in `src/main/resources/db/migration/` and follow the naming convention `V{N}__{Description}.sql`. Hibernate `ddl-auto` is set to `validate` (schema changes must go through Flyway).

### Connection

The `application.yml` has a hardcoded PostgreSQL connection to `192.168.50.195:5432`. For local development, use the `docker-compose.yml` which runs PostgreSQL on `localhost:5432`.

## API Endpoints

### Web Pages
| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Main dashboard (today's prices, DK1) |
| GET | `/tomorrow` | Tomorrow's prices dashboard |
| GET | `/prices?region=DK1` | Detailed prices with region selector |
| GET | `/test-prices` | Debug/test page |

### HTMX Fragment Endpoints
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/current-price?region=` | Current price card fragment |
| GET | `/api/todays-prices?region=` | Today's price list fragment |
| GET | `/api/tomorrows-prices?region=` | Tomorrow's price list fragment |
| GET | `/api/price-summary?region=` | Summary statistics fragment |

### JSON API
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/prices/current.json?region=` | Current hour price |
| GET | `/api/prices/today.json?region=` | Today's 24-hour prices |
| GET | `/api/prices/tomorrow.json?region=` | Tomorrow's 24-hour prices |

### Data Management
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/fetch/today` | Trigger fetch of today's prices |
| GET | `/api/fetch/tomorrow` | Trigger fetch of tomorrow's prices |
| GET | `/api/fetch/both` | Trigger fetch for both days |
| GET | `/api/fetch/force-refresh` | Delete and re-fetch today's prices |
| GET | `/api/test/add-sample-data` | Generate mock price data |

### Debug
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/debug/hourly-prices` | Debug hourly price mapping |
| GET | `/api/debug/timezone` | Debug timezone conversions |

## Code Conventions

### Naming
- **Packages**: `dk.electricity.pricecollector.*` (layered: config, controller, model, repository, service)
- **Classes**: PascalCase (`ElectricityPrice`, `ElprisenLigenuService`)
- **Methods**: camelCase (`getTodaysPrices`, `fetchAndSavePricesForDate`)
- **Constants**: UPPER_SNAKE_CASE (`API_BASE_URL`, `TRANSMISSION_TARIFF`)
- **Database columns**: snake_case (`price_date_time`, `spot_price`)

### Patterns
- **Dependency injection**: `@Autowired` field injection throughout
- **Logging**: SLF4J (`LoggerFactory.getLogger(ClassName.class)`)
- **Financial math**: Always use `BigDecimal`, never `double`/`float` for prices
- **Null handling**: `Optional<>` return types for single-entity queries
- **Error handling**: Try-catch in schedulers to prevent failure propagation; services return empty lists on API errors
- **Transactions**: `@Transactional` on service methods that modify data
- **Entity lifecycle**: `@PrePersist` / `@PreUpdate` for computed fields (totalPrice, timestamps)
- **Timezone**: All times converted to `Europe/Copenhagen` before storage

### Frontend Patterns
- Thymeleaf templates with HTMX attributes (`hx-get`, `hx-target`, `hx-indicator`)
- Tailwind CSS utility classes for styling
- JavaScript auto-refresh every 5 minutes
- Color-coded price ranking: green gradient (`bg-green-50` through `bg-green-300`) for cheapest 8 hours

## Important Notes for AI Assistants

- **No test suite exists**. There are no unit or integration tests in `src/test/`. The `spring-boot-starter-test` dependency is declared but unused.
- **No CI/CD pipeline** is configured.
- **Database credentials are hardcoded** in `application.yml`. The password for the configured host differs from the docker-compose default.
- **Tariff values are hardcoded** as constants in `ElprisenLigenuService`. These may change over time and are not externally configurable.
- **The cleanup scheduler is commented out** in `PriceScheduler.java:52` - the `cleanupOldPrices()` call is disabled.
- **All data management endpoints use GET** instead of POST/DELETE, which is not RESTful but functional for the current use case.
- **The external API** (`elprisenligenu.dk`) publishes next-day prices around 13:00 CET, which is why the schedulers run at 13:05-13:15.
- **Frontend assets** (Tailwind CSS, HTMX) are loaded from CDN - no frontend build tooling exists.
