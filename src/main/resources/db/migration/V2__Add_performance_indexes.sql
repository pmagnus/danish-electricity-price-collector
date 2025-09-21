-- Performance optimization migration
-- Adds additional indexes for common query patterns

-- Index for queries filtering by region and hour (useful for current hour queries)
CREATE INDEX IF NOT EXISTS idx_region_hour ON electricity_prices (region, hour);

-- Index for total price ordering (useful for finding lowest/highest prices)
CREATE INDEX IF NOT EXISTS idx_total_price ON electricity_prices (total_price);

-- Index for spot price ordering (useful for ranking by spot price)
CREATE INDEX IF NOT EXISTS idx_spot_price ON electricity_prices (spot_price);

-- Index for price_date_time DESC ordering (useful for recent prices queries)
CREATE INDEX IF NOT EXISTS idx_recent_prices 
ON electricity_prices (price_date_time DESC, region);

-- Comments for documentation
COMMENT ON INDEX idx_region_hour IS 'Optimizes queries for current price by region and hour';
COMMENT ON INDEX idx_total_price IS 'Optimizes queries for lowest/highest total prices';
COMMENT ON INDEX idx_spot_price IS 'Optimizes queries for lowest/highest spot prices and rankings';
COMMENT ON INDEX idx_recent_prices IS 'Optimizes queries for recent prices with DESC ordering';
