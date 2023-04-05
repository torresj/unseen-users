package com.torresj.unseenusers.utils;

import com.torresj.unseen.entities.*;
import java.time.LocalDateTime;
import java.util.Random;

public class EntityGenerator {
  public static UserEntity GenerateUser(
      String email, String password, Role role, AuthProvider provider, boolean validated) {
    return new UserEntity(
        new Random().nextLong(),
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

  public static GroupEntity GenerateGroup(String name, String code, long owner, boolean completed) {
    return new GroupEntity(
        new Random().nextLong(),
        LocalDateTime.now(),
        LocalDateTime.now(),
        name,
        code,
        owner,
        completed);
  }

  public static IterationEntity GenerateIteration(long groupId) {
    return new IterationEntity(
        new Random().nextLong(),
        groupId,
        LocalDateTime.now(),
        LocalDateTime.now().plusDays(1),
        true,
        true,
        "",
        "");
  }

  public static PairEntity GeneratePair(long iterationId, long giftingUserId, long giftedUserId) {
    return new PairEntity(
        new Random().nextLong(), iterationId, giftingUserId, giftedUserId, LocalDateTime.now());
  }
}
