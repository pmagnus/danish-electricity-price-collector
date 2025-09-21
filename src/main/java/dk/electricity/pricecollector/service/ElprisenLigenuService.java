package dk.electricity.pricecollector.service;

import dk.electricity.pricecollector.model.ElectricityPrice;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElprisenLigenuService {
    
    private static final Logger logger = LoggerFactory.getLogger(ElprisenLigenuService.class);
    private static final String API_BASE_URL = "https://www.elprisenligenu.dk/api/v1/prices/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM-dd");
    
    // Standard Danish tariffs and taxes (DKK per kWh)
    private static final BigDecimal TRANSMISSION_TARIFF = new BigDecimal("0.058");
    private static final BigDecimal SYSTEM_TARIFF = new BigDecimal("0.0125");
    private static final BigDecimal ELECTRICITY_TAX = new BigDecimal("0.090");
    
    @Autowired
    private ElectricityPriceService electricityPriceService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    
    public ElprisenLigenuService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Fetch today's spot prices for a specific region
     */
    public List<ElectricityPrice> fetchTodaysPrices(String region) {
        return fetchPricesForDate(LocalDate.now(), region);
    }
    
    /**
     * Fetch tomorrow's spot prices for a specific region
     */
    public List<ElectricityPrice> fetchTomorrowsPrices(String region) {
        return fetchPricesForDate(LocalDate.now().plusDays(1), region);
    }
    
    /**
     * Fetch spot prices for a specific date and region
     */
    public List<ElectricityPrice> fetchPricesForDate(LocalDate date, String region) {
        try {
            String url = buildApiUrl(date, region);
            logger.info("Fetching prices from: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                logger.warn("No response received from API for date: {} region: {}", date, region);
                return List.of();
            }
            
            SpotPrice[] spotPrices = objectMapper.readValue(response, SpotPrice[].class);
            return Arrays.stream(spotPrices)
                    .map(spotPrice -> convertToElectricityPrice(spotPrice, region, date))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Error fetching prices for date: {} region: {}", date, region, e);
            return List.of();
        }
    }
    
    /**
     * Fetch and save today's prices for both regions
     */
    public void fetchAndSaveTodaysPrices() {
        fetchAndSavePricesForDate(LocalDate.now());
    }
    
    /**
     * Fetch and save tomorrow's prices for both regions
     */
    public void fetchAndSaveTomorrowsPrices() {
        fetchAndSavePricesForDate(LocalDate.now().plusDays(1));
    }
    
    /**
     * Fetch and save prices for a specific date for both regions
     */
    public void fetchAndSavePricesForDate(LocalDate date) {
        logger.info("Fetching and saving prices for date: {}", date);
        
        // Fetch for both DK1 and DK2 regions
        List<String> regions = List.of("DK1", "DK2");
        
        for (String region : regions) {
            List<ElectricityPrice> prices = fetchPricesForDate(date, region);
            
            if (!prices.isEmpty()) {
                // Filter out prices that already exist
                List<ElectricityPrice> newPrices = prices.stream()
                        .filter(price -> !electricityPriceService.priceExists(price.getPriceDateTime(), price.getRegion()))
                        .collect(Collectors.toList());
                
                if (!newPrices.isEmpty()) {
                    electricityPriceService.savePrices(newPrices);
                    logger.info("Saved {} new prices for region {} on {}", newPrices.size(), region, date);
                } else {
                    logger.info("No new prices to save for region {} on {}", region, date);
                }
            } else {
                logger.warn("No prices fetched for region {} on {}", region, date);
            }
        }
    }
    
    private String buildApiUrl(LocalDate date, String region) {
        String formattedDate = date.format(DATE_FORMATTER);
        return API_BASE_URL + formattedDate + "_" + region + ".json";
    }
    
    private ElectricityPrice convertToElectricityPrice(SpotPrice spotPrice, String region, LocalDate forDate) {
        // Use spot price directly (already in DKK per kWh)
        BigDecimal spotPricePerKWh = spotPrice.dkkPerKWh;
        
        // Convert to Danish timezone for consistent storage
        LocalDateTime priceDateTime = spotPrice.timeStart.atZoneSameInstant(
            java.time.ZoneId.of("Europe/Copenhagen")).toLocalDateTime();
        
        // Extract hour from the converted priceDateTime (should match exactly)
        int hour = priceDateTime.getHour();
        
        ElectricityPrice electricityPrice = new ElectricityPrice(
                priceDateTime,
                forDate, // Explicitly set which date these prices are for
                hour,    // Hour of the day (0-23) from original timezone
                spotPricePerKWh,
                TRANSMISSION_TARIFF,
                SYSTEM_TARIFF,
                ELECTRICITY_TAX,
                region
        );
        
        logger.info("Converting: original={}, converted={}, extractedHour={}, spotPrice={}, region={}, forDate={}", 
                     spotPrice.timeStart, priceDateTime, hour, spotPricePerKWh, region, forDate);
        
        return electricityPrice;
    }
    
    /**
     * Inner class to represent the JSON response from elprisenligenu.dk API
     */
    public static class SpotPrice {
        @JsonProperty("DKK_per_kWh")
        public BigDecimal dkkPerKWh;
        
        @JsonProperty("EUR_per_kWh")
        public BigDecimal eurPerKWh;
        
        @JsonProperty("EXR")
        public BigDecimal exchangeRate;
        
        @JsonProperty("time_start")
        public OffsetDateTime timeStart;
        
        @JsonProperty("time_end")
        public OffsetDateTime timeEnd;
        
        // Default constructor for Jackson
        public SpotPrice() {}
        
        @Override
        public String toString() {
            return "SpotPrice{" +
                   "dkkPerKWh=" + dkkPerKWh +
                   ", timeStart=" + timeStart +
                   ", timeEnd=" + timeEnd +
                   '}';
        }
    }
}