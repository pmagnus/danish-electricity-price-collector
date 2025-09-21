package dk.electricity.pricecollector.controller;

import dk.electricity.pricecollector.model.ElectricityPrice;
import dk.electricity.pricecollector.service.ElectricityPriceService;
import dk.electricity.pricecollector.service.ElprisenLigenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class DashboardController {
    
    @Autowired
    private ElectricityPriceService priceService;
    
    @Autowired
    private ElprisenLigenuService elprisenLigenuService;
    
    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("title", "Dashboard");
        
        // Get summary data for DK1 (West Denmark)
        ElectricityPriceService.PriceSummary summary = priceService.getTodaysSummary("DK1");
        model.addAttribute("summary", summary);
        
        // Get recent prices for chart
        List<ElectricityPrice> recentPrices = priceService.getRecentPrices("DK1", 24);
        model.addAttribute("recentPrices", recentPrices);
        
        return "dashboard";
    }
    
    @GetMapping("/prices")
    public String prices(Model model, @RequestParam(defaultValue = "DK1") String region) {
        model.addAttribute("title", "Electricity Prices");
        model.addAttribute("selectedRegion", region);
        
        // Get today's prices
        List<ElectricityPrice> todaysPrices = priceService.getTodaysPrices(region);
        model.addAttribute("todaysPrices", todaysPrices);
        
        // Get tomorrow's prices if available
        List<ElectricityPrice> tomorrowsPrices = priceService.getTomorrowsPrices(region);
        model.addAttribute("tomorrowsPrices", tomorrowsPrices);
        
        return "prices";
    }
    
    // HTMX endpoints
    
    @GetMapping("/api/current-price")
    public String getCurrentPrice(Model model, @RequestParam(defaultValue = "DK1") String region) {
        ElectricityPrice currentPrice = priceService.getCurrentPrice(region).orElse(null);
        model.addAttribute("currentPrice", currentPrice);
        model.addAttribute("region", region);
        return "fragments/current-price :: current-price-card";
    }
    
    @GetMapping("/api/todays-prices")
    public String getTodaysPrices(Model model, @RequestParam(defaultValue = "DK1") String region) {
        List<ElectricityPrice> todaysPrices = priceService.getTodaysPrices(region);
        model.addAttribute("todaysPrices", todaysPrices);
        model.addAttribute("region", region);
        return "fragments/price-list :: price-list";
    }
    
    @GetMapping("/api/tomorrows-prices")
    public String getTomorrowsPrices(Model model, @RequestParam(defaultValue = "DK1") String region) {
        List<ElectricityPrice> tomorrowsPrices = priceService.getTomorrowsPrices(region);
        model.addAttribute("tomorrowsPrices", tomorrowsPrices);
        model.addAttribute("region", region);
        return "fragments/price-list :: price-list";
    }
    
    @GetMapping("/api/price-summary")
    public String getPriceSummary(Model model, @RequestParam(defaultValue = "DK1") String region) {
        ElectricityPriceService.PriceSummary summary = priceService.getTodaysSummary(region);
        model.addAttribute("summary", summary);
        return "fragments/price-summary :: price-summary";
    }
    
    // JSON API endpoints for HTMX or AJAX requests
    
    @GetMapping("/api/prices/current.json")
    @ResponseBody
    public ElectricityPrice getCurrentPriceJson(@RequestParam(defaultValue = "DK1") String region) {
        return priceService.getCurrentPrice(region).orElse(null);
    }
    
    @GetMapping("/api/prices/today.json")
    @ResponseBody
    public List<ElectricityPrice> getTodaysPricesJson(@RequestParam(defaultValue = "DK1") String region) {
        return priceService.getTodaysPrices(region);
    }
    
    @GetMapping("/api/prices/tomorrow.json")
    @ResponseBody
    public List<ElectricityPrice> getTomorrowsPricesJson(@RequestParam(defaultValue = "DK1") String region) {
        return priceService.getTomorrowsPrices(region);
    }
    
    // Test endpoint to add sample data (for development)
    @GetMapping("/api/test/add-sample-data")
    @ResponseBody
    public String addSampleData() {
        // Add some sample data for testing
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        
        for (int i = 0; i < 24; i++) {
            LocalDateTime priceTime = now.plusHours(i);
            
            // Generate some realistic sample prices for Denmark West (DK1)
            BigDecimal baseSpotPrice = BigDecimal.valueOf(300 + (Math.random() * 400)); // 300-700 DKK/MWh
            BigDecimal transmissionTariff = BigDecimal.valueOf(58.0); // Fixed transmission tariff
            BigDecimal systemTariff = BigDecimal.valueOf(12.5); // Fixed system tariff
            BigDecimal electricityTax = BigDecimal.valueOf(90.0); // Electricity tax
            
            ElectricityPrice price = new ElectricityPrice(
                priceTime, baseSpotPrice, transmissionTariff, systemTariff, electricityTax, "DK1"
            );
            
            if (!priceService.priceExists(priceTime, "DK1")) {
                priceService.savePrice(price);
            }
        }
        
        return "Sample data added successfully!";
    }
    
    // Real price fetching endpoints
    
    @GetMapping("/api/fetch/today")
    @ResponseBody
    public String fetchTodaysPrices() {
        try {
            elprisenLigenuService.fetchAndSaveTodaysPrices();
            return "Today's real electricity prices fetched and saved successfully!";
        } catch (Exception e) {
            return "Error fetching today's prices: " + e.getMessage();
        }
    }
    
    @GetMapping("/api/fetch/tomorrow")
    @ResponseBody
    public String fetchTomorrowsPrices() {
        try {
            elprisenLigenuService.fetchAndSaveTomorrowsPrices();
            return "Tomorrow's real electricity prices fetched and saved successfully!";
        } catch (Exception e) {
            return "Error fetching tomorrow's prices: " + e.getMessage();
        }
    }
    
    @GetMapping("/api/fetch/both")
    @ResponseBody
    public String fetchBothDaysPrices() {
        try {
            elprisenLigenuService.fetchAndSaveTodaysPrices();
            elprisenLigenuService.fetchAndSaveTomorrowsPrices();
            return "Both today's and tomorrow's real electricity prices fetched and saved successfully!";
        } catch (Exception e) {
            return "Error fetching prices: " + e.getMessage();
        }
    }
}
