package dk.electricity.pricecollector.config;

import dk.electricity.pricecollector.service.ElprisenLigenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Component that automatically fetches electricity price data when the application starts.
 * Only fetches data if it's not already present in the database.
 */
@Component
public class StartupDataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupDataInitializer.class);
    
    @Autowired
    private ElprisenLigenuService elprisenLigenuService;
    
    /**
     * This method is called when the application is fully started and ready to serve requests.
     * It will attempt to fetch today's and tomorrow's electricity prices if they don't already exist.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeElectricityData() {
        logger.info("Application startup complete - initializing electricity price data...");
        
        try {
            // Fetch today's prices
            logger.info("Checking and fetching today's electricity prices...");
            elprisenLigenuService.fetchAndSaveTodaysPrices();
            
            // Fetch tomorrow's prices (if available)
            logger.info("Checking and fetching tomorrow's electricity prices...");
            try {
                elprisenLigenuService.fetchAndSaveTomorrowsPrices();
            } catch (Exception e) {
                // Tomorrow's prices might not be available yet, which is normal
                logger.info("Tomorrow's prices not available yet (normal if before 13:00 CET): {}", e.getMessage());
            }
            
            logger.info("Electricity price data initialization completed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize electricity price data during startup", e);
            // Don't throw the exception - let the application continue to run
            // Users can manually fetch prices later if needed
        }
    }
}