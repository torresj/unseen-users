package com.torresj.unseenusers.utils;

import com.torresj.unseenusers.entities.AuthProvider;
import com.torresj.unseenusers.entities.Role;
import com.torresj.unseenusers.entities.UserEntity;

import java.time.LocalDateTime;

public class TestUtils {
  public static UserEntity GenerateUser(
      String email, String password, Role role, AuthProvider provider, boolean validated) {
    return new UserEntity(
        null,
        LocalDateTime.now(),
        LocalDateTime.now(),
        email,
        password,
        role,
        LocalDateTime.now(),
        email,
        null,
        1,
        validated,
        provider,
        123456789);
  }
}
