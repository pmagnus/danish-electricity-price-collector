package dk.electricity.pricecollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DanishElectricityPriceCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DanishElectricityPriceCollectorApplication.class, args);
    }
}