package tech.ada.pix.emissor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // Aula 3: liga o @Async das reações (cada uma roda na sua thread)
public class EmissorApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmissorApplication.class, args);
    }
}
