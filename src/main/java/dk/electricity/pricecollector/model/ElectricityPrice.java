package dk.electricity.pricecollector.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "electricity_prices", indexes = {
    @Index(name = "idx_price_datetime", columnList = "priceDateTime"),
    @Index(name = "idx_region", columnList = "region")
})
public class ElectricityPrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime priceDateTime;
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal spotPrice; // DKK per MWh
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal transmissionTariff; // DKK per MWh
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal systemTariff; // DKK per MWh
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal electricityTax; // DKK per MWh
    
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
        return totalPrice.divide(BigDecimal.valueOf(1000)); // Convert MWh to kWh
    }
    
    public BigDecimal getSpotPricePerKWh() {
        return spotPrice.divide(BigDecimal.valueOf(1000)); // Convert MWh to kWh
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