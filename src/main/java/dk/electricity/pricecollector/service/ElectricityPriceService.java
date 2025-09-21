package dk.electricity.pricecollector.service;

import dk.electricity.pricecollector.model.ElectricityPrice;
import dk.electricity.pricecollector.repository.ElectricityPriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ElectricityPriceService {
    
    private static final Logger logger = LoggerFactory.getLogger(ElectricityPriceService.class);
    private static final String DEFAULT_REGION = "DK1"; // West Denmark
    
    @Autowired
    private ElectricityPriceRepository repository;
    
    /**
     * Get current electricity price for the default region (DK1 - West Denmark)
     */
    public Optional<ElectricityPrice> getCurrentPrice() {
        return getCurrentPrice(DEFAULT_REGION);
    }
    
    /**
     * Get current electricity price for a specific region
     */
    public Optional<ElectricityPrice> getCurrentPrice(String region) {
        logger.debug("Fetching current price for region: {}", region);
        return repository.findFirstByRegionOrderByPriceDateTimeDesc(region);
    }
    
    /**
     * Get today's prices for the default region (DK1)
     */
    public List<ElectricityPrice> getTodaysPrices() {
        return getTodaysPrices(DEFAULT_REGION);
    }
    
    /**
     * Get today's prices for a specific region
     */
    public List<ElectricityPrice> getTodaysPrices(String region) {
        logger.debug("Fetching today's prices for region: {}", region);
        return repository.findTodaysPricesForRegion(region);
    }
    
    /**
     * Get tomorrow's prices for the default region (DK1)
     */
    public List<ElectricityPrice> getTomorrowsPrices() {
        return getTomorrowsPrices(DEFAULT_REGION);
    }
    
    /**
     * Get tomorrow's prices for a specific region
     */
    public List<ElectricityPrice> getTomorrowsPrices(String region) {
        logger.debug("Fetching tomorrow's prices for region: {}", region);
        return repository.findTomorrowsPricesForRegion(region);
    }
    
    /**
     * Get recent prices (last 24 hours) for the default region
     */
    public List<ElectricityPrice> getRecentPrices() {
        return getRecentPrices(DEFAULT_REGION, 24);
    }
    
    /**
     * Get recent prices for a specific region and number of hours
     */
    public List<ElectricityPrice> getRecentPrices(String region, int hours) {
        LocalDateTime fromDateTime = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
        logger.debug("Fetching recent prices for region: {} from: {}", region, fromDateTime);
        return repository.findRecentPricesForRegion(region, fromDateTime);
    }
    
    /**
     * Get prices for a specific date range
     */
    public List<ElectricityPrice> getPricesInDateRange(String region, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        logger.debug("Fetching prices for region: {} between {} and {}", region, startDateTime, endDateTime);
        return repository.findByRegionAndPriceDateTimeBetweenOrderByPriceDateTimeAsc(region, startDateTime, endDateTime);
    }
    
    /**
     * Get the lowest price for today
     */
    public Optional<ElectricityPrice> getTodaysLowestPrice(String region) {
        logger.debug("Fetching today's lowest price for region: {}", region);
        return repository.findLowestPriceForDate(region, LocalDate.now());
    }
    
    /**
     * Get the highest price for today
     */
    public Optional<ElectricityPrice> getTodaysHighestPrice(String region) {
        logger.debug("Fetching today's highest price for region: {}", region);
        return repository.findHighestPriceForDate(region, LocalDate.now());
    }
    
    /**
     * Save or update electricity price
     */
    public ElectricityPrice savePrice(ElectricityPrice price) {
        logger.debug("Saving electricity price: {}", price);
        return repository.save(price);
    }
    
    /**
     * Save multiple prices (bulk operation)
     */
    public List<ElectricityPrice> savePrices(List<ElectricityPrice> prices) {
        logger.debug("Saving {} electricity prices", prices.size());
        return repository.saveAll(prices);
    }
    
    /**
     * Check if price already exists for specific datetime and region
     */
    public boolean priceExists(LocalDateTime priceDateTime, String region) {
        return repository.existsByPriceDateTimeAndRegion(priceDateTime, region);
    }
    
    /**
     * Delete old prices (older than specified number of days)
     */
    @Transactional
    public int cleanupOldPrices(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(daysToKeep, ChronoUnit.DAYS);
        logger.info("Cleaning up prices older than: {}", cutoffDate);
        
        List<ElectricityPrice> oldPrices = repository.findAll().stream()
            .filter(price -> price.getPriceDateTime().isBefore(cutoffDate))
            .toList();
        
        if (!oldPrices.isEmpty()) {
            repository.deleteAll(oldPrices);
            logger.info("Deleted {} old price records", oldPrices.size());
        }
        
        return oldPrices.size();
    }
    
    /**
     * Delete all prices for a specific date and region
     */
    @Transactional
    public void deletePricesForDate(LocalDate date, String region) {
        List<ElectricityPrice> pricesToDelete = repository.findPricesForDateAndRegion(region, date);
        
        if (!pricesToDelete.isEmpty()) {
            repository.deleteAll(pricesToDelete);
            logger.info("Deleted {} electricity prices for date {} in region {}", 
                pricesToDelete.size(), date, region);
        }
    }
    
    /**
     * Get statistics summary for the current day
     */
    public PriceSummary getTodaysSummary(String region) {
        List<ElectricityPrice> todaysPrices = getTodaysPrices(region);
        
        if (todaysPrices.isEmpty()) {
            return new PriceSummary(region, 0, null, null, null);
        }
        
        ElectricityPrice lowest = todaysPrices.stream()
            .min((p1, p2) -> p1.getTotalPrice().compareTo(p2.getTotalPrice()))
            .orElse(null);
            
        ElectricityPrice highest = todaysPrices.stream()
            .max((p1, p2) -> p1.getTotalPrice().compareTo(p2.getTotalPrice()))
            .orElse(null);
            
        ElectricityPrice current = getCurrentPrice(region).orElse(null);
        
        return new PriceSummary(region, todaysPrices.size(), current, lowest, highest);
    }
    
    /**
     * Inner class for price summary
     */
    public static class PriceSummary {
        private final String region;
        private final int priceCount;
        private final ElectricityPrice currentPrice;
        private final ElectricityPrice lowestPrice;
        private final ElectricityPrice highestPrice;
        
        public PriceSummary(String region, int priceCount, ElectricityPrice currentPrice,
                           ElectricityPrice lowestPrice, ElectricityPrice highestPrice) {
            this.region = region;
            this.priceCount = priceCount;
            this.currentPrice = currentPrice;
            this.lowestPrice = lowestPrice;
            this.highestPrice = highestPrice;
        }
        
        // Getters
        public String getRegion() { return region; }
        public int getPriceCount() { return priceCount; }
        public ElectricityPrice getCurrentPrice() { return currentPrice; }
        public ElectricityPrice getLowestPrice() { return lowestPrice; }
        public ElectricityPrice getHighestPrice() { return highestPrice; }
    }
}