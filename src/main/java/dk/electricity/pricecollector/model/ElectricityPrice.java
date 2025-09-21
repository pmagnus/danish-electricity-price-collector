package dk.electricity.pricecollector.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "electricity_prices", indexes = {
    @Index(name = "idx_price_datetime", columnList = "priceDateTime"),
    @Index(name = "idx_region", columnList = "region"),
    @Index(name = "idx_price_date_region_hour", columnList = "priceDate, region, hour")
})
public class ElectricityPrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime priceDateTime;
    
    @Column(nullable = false)
    private LocalDate priceDate; // The date these prices are for (e.g., 2025-09-21)
    
    @Column(nullable = false)
    private Integer hour; // Hour of the day (0-23)
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal spotPrice; // DKK per kWh
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal transmissionTariff; // DKK per kWh
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal systemTariff; // DKK per kWh
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal electricityTax; // DKK per kWh
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal totalPrice; // Total including all tariffs and taxes
    
    @Column(nullable = false, length = 10)
    private String region; // DK1 (West Denmark) or DK2 (East Denmark)
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public ElectricityPrice() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ElectricityPrice(LocalDateTime priceDateTime, BigDecimal spotPrice, 
                           BigDecimal transmissionTariff, BigDecimal systemTariff,
                           BigDecimal electricityTax, String region) {
        this();
        this.priceDateTime = priceDateTime;
        this.priceDate = priceDateTime.toLocalDate(); // Default: extract date from datetime
        this.spotPrice = spotPrice;
        this.transmissionTariff = transmissionTariff;
        this.systemTariff = systemTariff;
        this.electricityTax = electricityTax;
        this.region = region;
        calculateTotalPrice();
    }
    
    public ElectricityPrice(LocalDateTime priceDateTime, LocalDate priceDate, Integer hour, 
                           BigDecimal spotPrice, BigDecimal transmissionTariff, BigDecimal systemTariff,
                           BigDecimal electricityTax, String region) {
        this();
        this.priceDateTime = priceDateTime;
        this.priceDate = priceDate;
        this.hour = hour;
        this.spotPrice = spotPrice;
        this.transmissionTariff = transmissionTariff;
        this.systemTariff = systemTariff;
        this.electricityTax = electricityTax;
        this.region = region;
        calculateTotalPrice();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }
    
    private void calculateTotalPrice() {
        this.totalPrice = spotPrice
            .add(transmissionTariff)
            .add(systemTariff)
            .add(electricityTax);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getPriceDateTime() {
        return priceDateTime;
    }
    
    public void setPriceDateTime(LocalDateTime priceDateTime) {
        this.priceDateTime = priceDateTime;
    }
    
    public LocalDate getPriceDate() {
        return priceDate;
    }
    
    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }
    
    public Integer getHour() {
        return hour;
    }
    
    public void setHour(Integer hour) {
        this.hour = hour;
    }
    
    public BigDecimal getSpotPrice() {
        return spotPrice;
    }
    
    public void setSpotPrice(BigDecimal spotPrice) {
        this.spotPrice = spotPrice;
    }
    
    public BigDecimal getTransmissionTariff() {
        return transmissionTariff;
    }
    
    public void setTransmissionTariff(BigDecimal transmissionTariff) {
        this.transmissionTariff = transmissionTariff;
    }
    
    public BigDecimal getSystemTariff() {
        return systemTariff;
    }
    
    public void setSystemTariff(BigDecimal systemTariff) {
        this.systemTariff = systemTariff;
    }
    
    public BigDecimal getElectricityTax() {
        return electricityTax;
    }
    
    public void setElectricityTax(BigDecimal electricityTax) {
        this.electricityTax = electricityTax;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods
    public BigDecimal getTotalPricePerKWh() {
        return totalPrice; // Already in kWh
    }
    
    public BigDecimal getSpotPricePerKWh() {
        return spotPrice; // Already in kWh
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElectricityPrice that = (ElectricityPrice) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(priceDateTime, that.priceDateTime) &&
               Objects.equals(region, that.region);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, priceDateTime, region);
    }
    
    @Override
    public String toString() {
        return "ElectricityPrice{" +
               "id=" + id +
               ", priceDateTime=" + priceDateTime +
               ", spotPrice=" + spotPrice +
               ", totalPrice=" + totalPrice +
               ", region='" + region + '\'' +
               '}';
    }
}