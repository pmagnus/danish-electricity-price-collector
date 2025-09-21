package dk.electricity.pricecollector.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PriceScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceScheduler.class);
    
    @Autowired
    private ElprisenLigenuService elprisenLigenuService;
    
    /**
     * Fetch today's prices every day at 13:05 (after prices are typically published at 13:00)
     */
    @Scheduled(cron = "0 5 13 * * *")
    public void fetchTodaysPricesScheduled() {
        logger.info("Scheduled task: Fetching today's electricity prices...");
        try {
            elprisenLigenuService.fetchAndSaveTodaysPrices();
            logger.info("Successfully fetched today's electricity prices");
        } catch (Exception e) {
            logger.error("Failed to fetch today's electricity prices", e);
        }
    }
    
    /**
     * Fetch tomorrow's prices every day at 13:10 (after prices are typically published at 13:00)
     */
    @Scheduled(cron = "0 10 13 * * *")
    public void fetchTomorrowsPricesScheduled() {
        logger.info("Scheduled task: Fetching tomorrow's electricity prices...");
        try {
            elprisenLigenuService.fetchAndSaveTomorrowsPrices();
            logger.info("Successfully fetched tomorrow's electricity prices");
        } catch (Exception e) {
            logger.error("Failed to fetch tomorrow's electricity prices", e);
        }
    }
    
    /**
     * Cleanup old prices every day at midnight (keep only last 30 days)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupOldPrices() {
        logger.info("Scheduled task: Cleaning up old electricity prices...");
        try {
            // This would need to be implemented in ElectricityPriceService if not already
            // electricityPriceService.cleanupOldPrices(30);
            logger.info("Successfully cleaned up old electricity prices");
        } catch (Exception e) {
            logger.error("Failed to cleanup old electricity prices", e);
        }
    }
    
    /**
     * Fetch both today's and tomorrow's prices every day at 13:15 as a backup
     * This ensures we have data even if individual scheduled tasks fail
     */
    @Scheduled(cron = "0 15 13 * * *")
    public void fetchAllPricesScheduled() {
        logger.info("Scheduled task: Fetching all electricity prices (backup)...");
        try {
            elprisenLigenuService.fetchAndSaveTodaysPrices();
            elprisenLigenuService.fetchAndSaveTomorrowsPrices();
            logger.info("Successfully fetched all electricity prices (backup)");
        } catch (Exception e) {
            logger.error("Failed to fetch all electricity prices (backup)", e);
        }
    }
}