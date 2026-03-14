package com.jordi.kakebot;

import org.springframework.boot.SpringApplication;

public class TestKakebotApplication {

	public static void main(String[] args) {
		SpringApplication.from(KakebotApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
