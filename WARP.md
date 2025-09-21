# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Commands

### Development
```bash
# Start PostgreSQL database (required)
docker compose up -d

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build without running tests
./mvnw clean compile

# Package the application
./mvnw clean package
```

### Testing
```bash
# Run a single test class
./mvnw test -Dtest=ElectricityPriceServiceTest

# Run a specific test method
./mvnw test -Dtest=ElectricityPriceServiceTest#testGetCurrentPrice

# Add sample test data (via HTTP endpoint)
curl http://localhost:8080/api/test/add-sample-data
```

### Database
```bash
# Connect to PostgreSQL (when running via Docker)
docker exec -it electricity-prices-db psql -U postgres -d electricity_prices

# Stop and remove database container with data
docker compose down -v
```

## Architecture

### Core Components
- **Spring Boot 3.2.0** application with Java 17
- **PostgreSQL 15** database with single `electricity_prices` table
- **Thymeleaf + HTMX + Tailwind CSS** frontend stack
- **Maven** for build and dependency management

### Data Model
Central entity is `ElectricityPrice` with these key fields:
- `priceDateTime` - hourly timestamp (indexed)
- `spotPrice`, `transmissionTariff`, `systemTariff`, `electricityTax` - price components in DKK/MWh
- `totalPrice` - auto-calculated sum of all components
- `region` - "DK1" (West Denmark) or "DK2" (East Denmark, indexed)

### Service Layer Architecture
`ElectricityPriceService` provides business logic with methods for:
- Current, today's, and tomorrow's prices by region
- Price statistics (lowest/highest for date)
- Bulk operations and cleanup
- `PriceSummary` inner class for dashboard data

### Repository Layer
`ElectricityPriceRepository` extends JpaRepository with custom queries:
- Native SQL queries for date-based filtering (today/tomorrow)
- Region-specific filtering with proper indexing
- Price comparison queries for min/max operations

### Controller Architecture
`DashboardController` serves both full pages and HTMX fragments:
- Full page endpoints: `/` (dashboard), `/prices` (detailed view)
- HTMX fragment endpoints: `/api/*` returning Thymeleaf fragments
- JSON API endpoints: `/api/prices/*.json` for programmatic access
- Test endpoint: `/api/test/add-sample-data` for development

### Frontend Architecture
**HTMX Integration**: Dynamic updates without page reloads using `hx-get`, `hx-target`, and `hx-indicator` attributes.

**Template Structure**:
- `layout.html` - Base template with navigation and common elements
- `dashboard.html` - Summary cards, price chart, and quick actions
- `prices.html` - Detailed price tables
- Fragment templates in `templates/fragments/` for HTMX responses

**Styling**: Tailwind CSS with utility classes, custom CSS in `static/css/app.css`, custom JS in `static/js/app.js`.

### Configuration
Key settings in `application.yml`:
- Database connection to localhost:5432/electricity_prices
- JPA with `ddl-auto: update` and SQL logging enabled
- Thymeleaf caching disabled for development
- Debug logging for application and Spring Web

## Development Notes

### Price Data Structure
- All monetary values stored as `BigDecimal` with precision 10, scale 6
- Prices in DKK per MWh, with utility methods for DKK per kWh conversion
- Total price automatically calculated in JPA lifecycle callbacks (`@PrePersist`, `@PreUpdate`)

### Regional Support
- Default region is "DK1" (West Denmark) throughout the application
- All service methods have overloads accepting region parameter
- Frontend allows region switching via query parameters

### Database Schema
- Automatic schema updates via Hibernate (`ddl-auto: update`)
- Composite indexing on `priceDateTime` and `region` for query optimization
- Audit fields `createdAt`/`updatedAt` automatically managed

### HTMX Integration Patterns
- Fragment responses use Thymeleaf fragment expressions (`:: fragment-name`)
- Loading indicators with `.htmx-indicator` class
- Dynamic content updates targeted to specific DOM elements
- Region switching implemented via query parameter changes

### Development Workflow
1. Start database: `docker compose up -d`
2. Run application: `./mvnw spring-boot:run`
3. Visit http://localhost:8080/api/test/add-sample-data to populate test data
4. Access dashboard at http://localhost:8080/

The application uses Spring Boot DevTools for automatic restart on code changes during development.