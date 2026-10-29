package mx.utch.sigsu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SigsuAplicacion {
    public static void main(String[] argumentos) {
        SpringApplication.run(SigsuAplicacion.class, argumentos);
    }
}
