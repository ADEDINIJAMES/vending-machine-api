package com.tumpet.vending_machine_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@ComponentScan(basePackages = "com.tumpet.vending_machine_api")
public class VendingMachineApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(VendingMachineApiApplication.class, args);
	}

}
