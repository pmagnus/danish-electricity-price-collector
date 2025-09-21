# Database Migrations with Flyway

This project uses Flyway for database schema versioning and migrations.

## Configuration

Flyway is configured in `application.yml`:

```yaml
flyway:
  enabled: true
  baseline-on-migrate: true
  baseline-version: 0
  locations: classpath:db/migration
  validate-on-migrate: true
```

## Migration Files

Migration files are located in `src/main/resources/db/migration/` and follow the naming convention:

- `V{version}__{description}.sql` (e.g., `V1__Create_electricity_prices_table.sql`)
- Version numbers should be sequential integers
- Descriptions use underscores instead of spaces

## Current Migrations

### V1 - Create electricity_prices table
- Creates the main `electricity_prices` table with all columns
- Adds primary key and performance indexes
- Includes table and column comments for documentation

### V2 - Add performance indexes
- Adds additional indexes for common query patterns:
  - `idx_region_hour` - Optimizes current price queries by region and hour
  - `idx_total_price` - Optimizes lowest/highest total price queries
  - `idx_spot_price` - Optimizes spot price rankings
  - `idx_recent_prices` - Optimizes recent prices queries with DESC ordering

## Database Schema

The main table `electricity_prices` stores:
- **Pricing data**: spot_price, transmission_tariff, system_tariff, electricity_tax, total_price
- **Temporal data**: price_date_time, price_date, hour
- **Regional data**: region (DK1/DK2)
- **Audit data**: created_at, updated_at

All price fields use `NUMERIC(10,6)` precision for accurate financial calculations in DKK per kWh.

## Indexes

The table has comprehensive indexing for optimal query performance:
- Primary key on `id`
- Composite indexes on `(price_date, region)` and `(price_date, region, hour)`
- Single column indexes on frequently queried fields
- DESC ordering indexes for recent data queries

## Migration Commands

View migration history:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

Check table structure:
```sql
\d electricity_prices
```

## Adding New Migrations

1. Create a new file: `V{next_version}__{description}.sql`
2. Write the SQL migration script
3. Restart the application - Flyway will automatically apply the new migration
4. Verify the migration in `flyway_schema_history` table

## Best Practices

- **Never modify existing migration files** - once applied, they should remain unchanged
- **Use meaningful descriptions** in migration file names
- **Include comments** in SQL files for documentation
- **Test migrations** on a copy of production data when possible
- **Use IF NOT EXISTS** for DDL operations where appropriate
- **Be aware of PostgreSQL-specific syntax** (e.g., CURRENT_DATE functions in partial indexes)

## Rollback Strategy

Flyway Community Edition doesn't support automatic rollbacks. For rollback scenarios:
1. Create a new migration with the reverse changes
2. Or manually revert changes via SQL if needed
3. Update the `flyway_schema_history` table if necessary

## Environment Configuration

The current setup uses:
- **PostgreSQL 15** database
- **Hibernate validation mode** (`ddl-auto: validate`)
- **Baseline version 0** for existing databases
- **Automatic baseline** on first run