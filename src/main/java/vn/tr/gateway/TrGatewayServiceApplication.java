package vn.tr.gateway;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication(scanBasePackages = {"vn.tr.gateway"})
@EnableDiscoveryClient
public class TrGatewayServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(TrGatewayServiceApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			System.out.println("--- LET'S INSPECT THE BEANS PROVIDED BY SPRING BOOT ---");
			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				// Tìm kiếm bean của bạn
				if (beanName.equalsIgnoreCase("globalErrorHandler")) {
					System.out.println(">>> FOUND IT: " + beanName);
				}
			}
			System.out.println("--- FINISHED INSPECTING BEANS ---");
		};
	}
}
