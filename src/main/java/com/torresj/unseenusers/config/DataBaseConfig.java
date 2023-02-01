package com.torresj.unseenusers.config;

import com.torresj.unseenusers.entities.AuthProvider;
import com.torresj.unseenusers.entities.Role;
import com.torresj.unseenusers.entities.UserEntity;
import com.torresj.unseenusers.repositories.mutations.UserMutationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("local")
@Log
@RequiredArgsConstructor
public class DataBaseConfig {

  private final UserMutationRepository userMutationRepository;

  @Bean
  public void initDataBase() {
    log.info("Init Local Data Base");

    // Test user
    userMutationRepository.saveAll(List.of(
        new UserEntity(
            null,
            null,
            null,
            "test@test.com",
            "test",
            Role.ADMIN,
            null,
            "test",
            null,
            0,
            true,
            AuthProvider.UNSEEN,
            0),
        new UserEntity(
            null,
            null,
            null,
            "test2@test.com",
            "test2",
            Role.USER,
            null,
            "test2",
            null,
            0,
            true,
            AuthProvider.UNSEEN,
            0)
    ));

  }
}
