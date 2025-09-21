package dk.electricity.pricecollector.repository;

import dk.electricity.pricecollector.model.ElectricityPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ElectricityPriceRepository extends JpaRepository<ElectricityPrice, Long> {
    
    // Find prices for a specific region
    List<ElectricityPrice> findByRegionOrderByPriceDateTimeDesc(String region);
    
    // Find prices for a specific region within a date range
    List<ElectricityPrice> findByRegionAndPriceDateTimeBetweenOrderByPriceDateTimeAsc(
        String region, LocalDateTime startDateTime, LocalDateTime endDateTime);
    
    // Find the latest price for a specific region
    Optional<ElectricityPrice> findFirstByRegionOrderByPriceDateTimeDesc(String region);
    
    // Find prices for today for a specific region
    @Query(value = "SELECT * FROM electricity_prices ep WHERE ep.region = :region " +
           "AND ep.price_date = CURRENT_DATE ORDER BY ep.price_date_time ASC", nativeQuery = true)
    List<ElectricityPrice> findTodaysPricesForRegion(@Param("region") String region);
    
    // Find prices for tomorrow for a specific region
    @Query(value = "SELECT * FROM electricity_prices ep WHERE ep.region = :region " +
           "AND ep.price_date = CURRENT_DATE + INTERVAL '1 day' ORDER BY ep.price_date_time ASC", nativeQuery = true)
    List<ElectricityPrice> findTomorrowsPricesForRegion(@Param("region") String region);
    
    // Find prices for a specific date and region
    @Query(value = "SELECT * FROM electricity_prices ep WHERE ep.region = :region " +
           "AND ep.price_date = :priceDate ORDER BY ep.price_date_time ASC", nativeQuery = true)
    List<ElectricityPrice> findPricesForDateAndRegion(@Param("region") String region, @Param("priceDate") LocalDate priceDate);
    
    // Check if price already exists for specific datetime and region
    boolean existsByPriceDateTimeAndRegion(LocalDateTime priceDateTime, String region);
    
    // Find the lowest price for a specific date and region
    @Query(value = "SELECT * FROM electricity_prices ep WHERE ep.region = :region " +
           "AND ep.price_date = :priceDate " +
           "ORDER BY ep.total_price ASC LIMIT 1", nativeQuery = true)
    Optional<ElectricityPrice> findLowestPriceForDate(@Param("region") String region, 
                                                     @Param("priceDate") LocalDate priceDate);
    
    // Find the highest price for a specific date and region
    @Query(value = "SELECT * FROM electricity_prices ep WHERE ep.region = :region " +
           "AND ep.price_date = :priceDate " +
           "ORDER BY ep.total_price DESC LIMIT 1", nativeQuery = true)
    Optional<ElectricityPrice> findHighestPriceForDate(@Param("region") String region, 
                                                      @Param("priceDate") LocalDate priceDate);
    
    // Get hourly prices for the last N hours
    @Query("SELECT ep FROM ElectricityPrice ep WHERE ep.region = :region " +
           "AND ep.priceDateTime >= :fromDateTime ORDER BY ep.priceDateTime DESC")
    List<ElectricityPrice> findRecentPricesForRegion(@Param("region") String region, 
                                                    @Param("fromDateTime") LocalDateTime fromDateTime);
    
    // Find price for current hour (today's date and current hour)
    @Query(value = "SELECT * FROM electricity_prices ep WHERE ep.region = :region " +
           "AND ep.price_date = CURRENT_DATE AND ep.hour = :hour LIMIT 1", nativeQuery = true)
    Optional<ElectricityPrice> findPriceForCurrentHour(@Param("region") String region, 
                                                       @Param("hour") int hour);
}