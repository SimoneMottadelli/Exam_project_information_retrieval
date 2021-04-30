package com.unimib.it.ir.irproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class IrprojectApplication {

	public static void main(String[] args) {
		Indexing.execute();
		SpringApplication.run(IrprojectApplication.class, args);
	}
	
}