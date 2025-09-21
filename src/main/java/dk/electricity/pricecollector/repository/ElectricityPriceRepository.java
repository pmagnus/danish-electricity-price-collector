package dk.electricity.pricecollector.repository;

import dk.electricity.pricecollector.model.ElectricityPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    @Query("SELECT ep FROM ElectricityPrice ep WHERE ep.region = :region " +
           "AND DATE(ep.priceDateTime) = CURRENT_DATE ORDER BY ep.priceDateTime ASC")
    List<ElectricityPrice> findTodaysPricesForRegion(@Param("region") String region);
    
    // Find prices for tomorrow for a specific region
    @Query("SELECT ep FROM ElectricityPrice ep WHERE ep.region = :region " +
           "AND DATE(ep.priceDateTime) = CURRENT_DATE + 1 ORDER BY ep.priceDateTime ASC")
    List<ElectricityPrice> findTomorrowsPricesForRegion(@Param("region") String region);
    
    // Check if price already exists for specific datetime and region
    boolean existsByPriceDateTimeAndRegion(LocalDateTime priceDateTime, String region);
    
    // Find the lowest price for a specific date and region
    @Query("SELECT ep FROM ElectricityPrice ep WHERE ep.region = :region " +
           "AND DATE(ep.priceDateTime) = DATE(:date) " +
           "ORDER BY ep.totalPrice ASC LIMIT 1")
    Optional<ElectricityPrice> findLowestPriceForDate(@Param("region") String region, 
                                                     @Param("date") LocalDateTime date);
    
    // Find the highest price for a specific date and region
    @Query("SELECT ep FROM ElectricityPrice ep WHERE ep.region = :region " +
           "AND DATE(ep.priceDateTime) = DATE(:date) " +
           "ORDER BY ep.totalPrice DESC LIMIT 1")
    Optional<ElectricityPrice> findHighestPriceForDate(@Param("region") String region, 
                                                      @Param("date") LocalDateTime date);
    
    // Get hourly prices for the last N hours
    @Query("SELECT ep FROM ElectricityPrice ep WHERE ep.region = :region " +
           "AND ep.priceDateTime >= :fromDateTime ORDER BY ep.priceDateTime DESC")
    List<ElectricityPrice> findRecentPricesForRegion(@Param("region") String region, 
                                                    @Param("fromDateTime") LocalDateTime fromDateTime);
}