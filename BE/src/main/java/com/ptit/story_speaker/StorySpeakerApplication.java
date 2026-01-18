package com.ptit.story_speaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StorySpeakerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StorySpeakerApplication.class, args);
	}

}
