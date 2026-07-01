package net.togogo.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "net.togogo")
@EnableFeignClients(basePackages = "net.togogo.client")
public class ClientApplication {

}
