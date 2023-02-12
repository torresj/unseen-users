package com.torresj.unseenusers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.torresj.unseen.entities")
public class UnseenUsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnseenUsersApplication.class, args);
	}

}
