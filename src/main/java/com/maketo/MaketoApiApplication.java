package com.maketo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MaketoApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaketoApiApplication.class, args);
	}

}
