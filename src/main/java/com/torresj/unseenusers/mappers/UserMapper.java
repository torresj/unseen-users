package com.torresj.unseenusers.mappers;

import com.torresj.unseenusers.dtos.User;
import com.torresj.unseenusers.entities.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserEntity toEntity(User user);

  User toUserDto(UserEntity userEntity);
}
