# Danish Electricity Price Collector

A Spring Boot application for collecting and displaying Danish electricity prices with all tariffs included. The application focuses on West Denmark (DK1) region and provides a modern web interface built with HTMX and Tailwind CSS.

## 🔋 Features

- **Real-time Price Display**: Current electricity prices with comprehensive tariff breakdown
- **Historical Data**: View today's and tomorrow's hourly prices
- **Interactive UI**: Dynamic updates using HTMX without full page reloads
- **Tariff Breakdown**: Complete breakdown of:
  - Spot prices (market price)
  - Transmission tariffs
  - System tariffs
  - Electricity taxes
  - Total price per kWh
- **Regional Support**: Support for both DK1 (West Denmark) and DK2 (East Denmark)
- **Responsive Design**: Mobile-friendly interface with Tailwind CSS

## 🏗️ Technology Stack

- **Backend**: Spring Boot 3.2.0
- **Database**: PostgreSQL 15
- **Frontend**: Thymeleaf + HTMX + Tailwind CSS
- **Build Tool**: Maven
- **Java Version**: 17
- **Containerization**: Docker Compose for PostgreSQL

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for database)
- Git

## 🚀 Quick Start

### 1. Clone and Navigate to Project

```bash
git clone <your-repo-url>
cd danish-electricity-price-collector
```

### 2. Start PostgreSQL Database

```bash
docker-compose up -d
```

This will start a PostgreSQL database on `localhost:5432` with:
- Database: `electricity_prices`
- Username: `postgres`
- Password: `postgres`

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

Or if you're on Windows:
```cmd
mvnw.cmd spring-boot:run
```

### 4. Access the Application

Open your browser and navigate to:
- **Dashboard**: http://localhost:8080/
- **Prices Overview**: http://localhost:8080/prices

## 📊 API Endpoints

### Web Pages
- `GET /` - Dashboard with summary and charts
- `GET /prices` - Detailed price tables

### HTMX Endpoints (HTML fragments)
- `GET /api/current-price?region=DK1` - Current price card
- `GET /api/todays-prices?region=DK1` - Today's price list
- `GET /api/tomorrows-prices?region=DK1` - Tomorrow's price list
- `GET /api/price-summary?region=DK1` - Price summary

### JSON API Endpoints
- `GET /api/prices/current.json?region=DK1` - Current price as JSON
- `GET /api/prices/today.json?region=DK1` - Today's prices as JSON
- `GET /api/prices/tomorrow.json?region=DK1` - Tomorrow's prices as JSON

### Development Endpoints
- `GET /api/test/add-sample-data` - Add sample data for testing

## 📁 Project Structure

```
src/
├── main/
│   ├── java/dk/electricity/pricecollector/
│   │   ├── controller/          # Web controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # Data access layer
│   │   ├── model/               # JPA entities
│   │   └── config/              # Configuration classes
│   └── resources/
│       ├── templates/           # Thymeleaf HTML templates
│       ├── static/
│       │   ├── css/            # Custom stylesheets
│       │   └── js/             # Custom JavaScript
│       └── application.yml      # Application configuration
└── test/                        # Test classes
```

## 🔧 Configuration

### Database Configuration

The application is configured to connect to PostgreSQL. You can modify the database settings in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/electricity_prices
    username: postgres
    password: postgres
```

### Application Properties

Key configuration options in `application.yml`:

- **Server Port**: `server.port` (default: 8080)
- **Database Settings**: `spring.datasource.*`
- **JPA Settings**: `spring.jpa.*`
- **Logging Levels**: `logging.level.*`

## 🗄️ Database Schema

The application uses a single main entity:

### ElectricityPrice Table
- `id` - Primary key
- `price_date_time` - Hour timestamp
- `spot_price` - Market spot price (DKK/MWh)
- `transmission_tariff` - Transmission tariff (DKK/MWh)
- `system_tariff` - System tariff (DKK/MWh)
- `electricity_tax` - Electricity tax (DKK/MWh)
- `total_price` - Total price (calculated)
- `region` - DK1 or DK2
- `created_at` / `updated_at` - Timestamps

## 🧪 Testing

### Add Sample Data

For development and testing purposes, visit:
```
http://localhost:8080/api/test/add-sample-data
```

This will populate the database with realistic sample prices for the current day.

### Run Tests

```bash
./mvnw test
```

## 🎨 Frontend Features

### HTMX Integration
- Dynamic content updates without page reloads
- Form submissions with enhanced UX
- Auto-refresh capabilities
- Loading indicators

### Tailwind CSS Styling
- Responsive grid layouts
- Hover effects and animations
- Color-coded price indicators
- Modern card-based design

### Interactive Elements
- Region switching (DK1/DK2)
- Real-time price updates
- Price comparison indicators
- Mobile-friendly navigation

## 📈 Future Enhancements

Potential areas for expansion:

1. **Data Collection**
   - Integration with Danish energy APIs
   - Automated price fetching
   - Historical data import

2. **Analytics**
   - Price trend analysis
   - Cost optimization suggestions
   - Usage pattern insights

3. **Notifications**
   - Price alerts
   - Low-price notifications
   - Email/SMS integration

4. **Additional Features**
   - Price predictions
   - Energy supplier comparison
   - Carbon footprint tracking

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙋‍♂️ Support

If you encounter any issues or have questions:

1. Check the [Issues](../../issues) page
2. Create a new issue with detailed description
3. Include relevant logs and configuration

## 📊 Monitoring

### Health Check
The application includes Spring Boot Actuator endpoints (if enabled):
- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information

### Logging
Logs are configured to show:
- SQL queries (in DEBUG mode)
- HTMX request/response cycles
- Service layer operations
- Error details and stack traces

---

**Built with ❤️ for the Danish energy market**