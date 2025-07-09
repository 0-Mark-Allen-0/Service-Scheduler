package com.example.SchedulerW4;

//import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class SchedulerW4Application {

	public static void main(String[] args) {

//		//-----------------------------------------------------------------------------------------
//		//.env File Loader SETUP:
//
//		// Load .env variables manually
//		Dotenv dotenv = Dotenv.load();
//
//		// Set as system properties
//		dotenv.entries().forEach(entry ->
//				System.setProperty(entry.getKey(), entry.getValue())
//		);
//		//-----------------------------------------------------------------------------------------

		SpringApplication.run(SchedulerW4Application.class, args);
	}

}
