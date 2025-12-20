package com.apress.crm.customer;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class CustomerApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(CustomerApplication.class)
				.bannerMode(Banner.Mode.CONSOLE)
				.logStartupInfo(true)
				.run(args);
	}

}
