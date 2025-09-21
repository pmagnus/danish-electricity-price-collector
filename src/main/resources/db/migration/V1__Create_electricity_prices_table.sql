-- Initial schema for electricity prices
-- This migration creates the main table for storing Danish electricity prices

CREATE SEQUENCE IF NOT EXISTS electricity_prices_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS electricity_prices (
    id BIGINT DEFAULT nextval('electricity_prices_id_seq'::regclass) NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    electricity_tax NUMERIC(10,6) NOT NULL,
    price_date_time TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    region VARCHAR(10) NOT NULL,
    spot_price NUMERIC(10,6) NOT NULL,
    system_tariff NUMERIC(10,6) NOT NULL,
    total_price NUMERIC(10,6) NOT NULL,
    transmission_tariff NUMERIC(10,6) NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    price_date DATE NOT NULL,
    hour INTEGER NOT NULL,
    
    CONSTRAINT electricity_prices_pkey PRIMARY KEY (id)
);

-- Create indexes for optimal query performance
CREATE INDEX IF NOT EXISTS idx_price_datetime ON electricity_prices (price_date_time);
CREATE INDEX IF NOT EXISTS idx_region ON electricity_prices (region);
CREATE INDEX IF NOT EXISTS idx_price_date_region ON electricity_prices (price_date, region);
CREATE INDEX IF NOT EXISTS idx_price_date_region_hour ON electricity_prices (price_date, region, hour);

-- Comments for documentation
COMMENT ON TABLE electricity_prices IS 'Stores Danish electricity prices with all tariffs and taxes for DK1 and DK2 regions';
COMMENT ON COLUMN electricity_prices.spot_price IS 'Spot price in DKK per kWh';
COMMENT ON COLUMN electricity_prices.transmission_tariff IS 'Transmission tariff in DKK per kWh';
COMMENT ON COLUMN electricity_prices.system_tariff IS 'System tariff in DKK per kWh';
COMMENT ON COLUMN electricity_prices.electricity_tax IS 'Electricity tax in DKK per kWh';
COMMENT ON COLUMN electricity_prices.total_price IS 'Total price including all tariffs and taxes in DKK per kWh';
COMMENT ON COLUMN electricity_prices.region IS 'Denmark region: DK1 (West) or DK2 (East)';
COMMENT ON COLUMN electricity_prices.price_date IS 'The date these prices are valid for';
COMMENT ON COLUMN electricity_prices.hour IS 'Hour of the day (0-23) for this price';