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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        
        // Try to get today's prices first, fall back to tomorrow's if empty
        List<ElectricityPrice> displayPrices = priceService.getTodaysPrices("DK1");
        String pricesPeriod = "Today";
        
        if (displayPrices.isEmpty()) {
            displayPrices = priceService.getTomorrowsPrices("DK1");
            pricesPeriod = "Tomorrow";
        }
        
        model.addAttribute("todaysPrices", displayPrices);
        model.addAttribute("pricesPeriod", pricesPeriod);
        
        // Create a list for all 24 hours (null for missing hours)
        List<ElectricityPrice> hourlyPricesList = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hourlyPricesList.add(null);
        }
        for (ElectricityPrice price : displayPrices) {
            int hour = price.getHour(); // Use the hour column directly
            if (hour >= 0 && hour < 24) {
                hourlyPricesList.set(hour, price);
            }
        }
        model.addAttribute("hourlyPricesList", hourlyPricesList);
        
        // Also create a map for template compatibility
        Map<Integer, ElectricityPrice> hourlyPrices = new HashMap<>();
        for (ElectricityPrice price : displayPrices) {
            int hour = price.getHour();
            hourlyPrices.put(hour, price);
        }
        model.addAttribute("hourlyPrices", hourlyPrices);
        
        // Find the 8 lowest spot prices for green gradient highlighting
        List<ElectricityPrice> sortedBySpotPrice = displayPrices.stream()
            .sorted((a, b) -> a.getSpotPrice().compareTo(b.getSpotPrice()))
            .collect(Collectors.toList());
        
        // Create ranking for all hours (1 = lowest spot price, 24 = highest)
        Map<Integer, Integer> spotPriceRanks = new HashMap<>();
        Map<Integer, String> rowClasses = new HashMap<>();
        
        for (int i = 0; i < sortedBySpotPrice.size(); i++) {
            int hour = sortedBySpotPrice.get(i).getHour();
            int rank = i + 1; // rank 1-24, where 1 is lowest
            spotPriceRanks.put(hour, rank);
            
            // Green background for 8 lowest prices only
            if (i < 8) {
                String bgClass;
                if (i == 0) bgClass = "bg-green-300"; // lowest price
                else if (i == 1) bgClass = "bg-green-200"; // 2nd lowest
                else if (i <= 3) bgClass = "bg-green-100"; // 3rd-4th lowest
                else bgClass = "bg-green-50"; // 5th-8th lowest
                
                rowClasses.put(hour, bgClass);
            }
        }
        
        model.addAttribute("spotPriceRanks", spotPriceRanks);
        model.addAttribute("rowClasses", rowClasses);
        
        // Add current hour for highlighting
        model.addAttribute("currentHour", LocalDateTime.now().getHour());
        
        return "dashboard";
    }
    
    @GetMapping("/tomorrow")
    public String tomorrow(Model model) {
        model.addAttribute("title", "Tomorrow");
        
        // Get summary data for DK1 (West Denmark) - tomorrow's data
        ElectricityPriceService.PriceSummary summary = priceService.getTomorrowsSummary("DK1");
        model.addAttribute("summary", summary);
        
        // Get recent prices for chart (still use recent for context)
        List<ElectricityPrice> recentPrices = priceService.getRecentPrices("DK1", 24);
        model.addAttribute("recentPrices", recentPrices);
        
        // Get tomorrow's prices
        List<ElectricityPrice> displayPrices = priceService.getTomorrowsPrices("DK1");
        String pricesPeriod = "Tomorrow";
        
        // If no tomorrow prices, try today as fallback
        if (displayPrices.isEmpty()) {
            displayPrices = priceService.getTodaysPrices("DK1");
            pricesPeriod = "Today (Tomorrow not available)";
        }
        
        model.addAttribute("todaysPrices", displayPrices); // Keep same attribute name for template compatibility
        model.addAttribute("pricesPeriod", pricesPeriod);
        
        // Create a list for all 24 hours (null for missing hours)
        List<ElectricityPrice> hourlyPricesList = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hourlyPricesList.add(null);
        }
        for (ElectricityPrice price : displayPrices) {
            int hour = price.getHour(); // Use the hour column directly
            if (hour >= 0 && hour < 24) {
                hourlyPricesList.set(hour, price);
            }
        }
        model.addAttribute("hourlyPricesList", hourlyPricesList);
        
        // Also create a map for template compatibility
        Map<Integer, ElectricityPrice> hourlyPrices = new HashMap<>();
        for (ElectricityPrice price : displayPrices) {
            int hour = price.getHour();
            hourlyPrices.put(hour, price);
        }
        model.addAttribute("hourlyPrices", hourlyPrices);
        
        // Create ranking for all hours (1 = lowest spot price, 24 = highest)
        Map<Integer, Integer> spotPriceRanks = new HashMap<>();
        Map<Integer, String> rowClasses = new HashMap<>();
        
        if (!displayPrices.isEmpty()) {
            List<ElectricityPrice> sortedBySpotPrice = displayPrices.stream()
                .sorted((a, b) -> a.getSpotPrice().compareTo(b.getSpotPrice()))
                .collect(Collectors.toList());
            
            for (int i = 0; i < sortedBySpotPrice.size(); i++) {
                int hour = sortedBySpotPrice.get(i).getHour();
                int rank = i + 1; // rank 1-24, where 1 is lowest
                spotPriceRanks.put(hour, rank);
                
                // Green background for 8 lowest prices only
                if (i < 8) {
                    String bgClass;
                    if (i == 0) bgClass = "bg-green-300"; // lowest price
                    else if (i == 1) bgClass = "bg-green-200"; // 2nd lowest
                    else if (i <= 3) bgClass = "bg-green-100"; // 3rd-4th lowest
                    else bgClass = "bg-green-50"; // 5th-8th lowest
                    
                    rowClasses.put(hour, bgClass);
                }
            }
        }
        
        model.addAttribute("spotPriceRanks", spotPriceRanks);
        model.addAttribute("rowClasses", rowClasses);
        
        // Add current hour for highlighting (even though it's tomorrow data)
        model.addAttribute("currentHour", LocalDateTime.now().getHour());
        
        return "dashboard"; // Reuse the same template
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
    
    @GetMapping("/api/fetch/force-refresh")
    @ResponseBody
    public String forceRefreshTodaysPrices() {
        try {
            // Delete today's existing prices
            priceService.deletePricesForDate(LocalDateTime.now().toLocalDate(), "DK1");
            priceService.deletePricesForDate(LocalDateTime.now().toLocalDate(), "DK2");
            
            // Fetch fresh data
            elprisenLigenuService.fetchAndSaveTodaysPrices();
            
            return "Today's electricity prices force-refreshed successfully! All 24 hours should now be available.";
        } catch (Exception e) {
            return "Error force-refreshing prices: " + e.getMessage();
        }
    }
    
    // Debug endpoint to check hourly prices
    @GetMapping("/api/debug/hourly-prices")
    @ResponseBody
    public Map<String, Object> debugHourlyPrices() {
        // Get tomorrow's prices
        List<ElectricityPrice> displayPrices = priceService.getTomorrowsPrices("DK1");
        
        // Create hourly map
        Map<Integer, ElectricityPrice> hourlyPrices = new HashMap<>();
        for (ElectricityPrice price : displayPrices) {
            int hour = price.getPriceDateTime().getHour();
            hourlyPrices.put(hour, price);
        }
        
        Map<String, Object> debug = new HashMap<>();
        debug.put("displayPricesCount", displayPrices.size());
        debug.put("hourlyPricesKeys", hourlyPrices.keySet());
        debug.put("currentHour", LocalDateTime.now().getHour());
        
        return debug;
    }
    
    @GetMapping("/test-prices")
    public String testPrices(Model model) {
        // Use the same logic as the main dashboard
        List<ElectricityPrice> displayPrices = priceService.getTodaysPrices("DK1");
        String pricesPeriod = "Today";
        
        if (displayPrices.isEmpty()) {
            displayPrices = priceService.getTomorrowsPrices("DK1");
            pricesPeriod = "Tomorrow";
        }
        
        // Create hourly map
        Map<Integer, ElectricityPrice> hourlyPrices = new HashMap<>();
        for (ElectricityPrice price : displayPrices) {
            int hour = price.getPriceDateTime().getHour();
            hourlyPrices.put(hour, price);
        }
        
        // Create list like the main dashboard
        List<ElectricityPrice> hourlyPricesList = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hourlyPricesList.add(null);
        }
        for (ElectricityPrice price : displayPrices) {
            int hour = price.getHour();
            if (hour >= 0 && hour < 24) {
                hourlyPricesList.set(hour, price);
            }
        }
        
        model.addAttribute("displayPrices", displayPrices);
        model.addAttribute("hourlyPrices", hourlyPrices);
        model.addAttribute("hourlyPricesList", hourlyPricesList);
        model.addAttribute("pricesPeriod", pricesPeriod);
        model.addAttribute("currentHour", LocalDateTime.now().getHour());
        
        return "test-prices"; // will create this template
    }
    
    @GetMapping("/api/debug/timezone")
    @ResponseBody
    public Map<String, String> debugTimezone() {
        Map<String, String> debug = new HashMap<>();
        
        // Test what happens with timezone conversion
        String testTimestamp = "2025-09-21T00:00:00+02:00";
        try {
            java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(testTimestamp);
            java.time.LocalDateTime ldt = odt.toLocalDateTime();
            
            debug.put("originalTimestamp", testTimestamp);
            debug.put("offsetDateTime", odt.toString());
            debug.put("localDateTime", ldt.toString());
            debug.put("systemTimeZone", java.time.ZoneId.systemDefault().toString());
            debug.put("currentTime", java.time.LocalDateTime.now().toString());
        } catch (Exception e) {
            debug.put("error", e.getMessage());
        }
        
        return debug;
    }
}
