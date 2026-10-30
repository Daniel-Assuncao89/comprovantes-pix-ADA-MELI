package tech.ada.pix.consulta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// TODO (Aula 3): habilitar cache — @EnableCaching.
public class ConsultaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsultaApplication.class, args);
    }
}
