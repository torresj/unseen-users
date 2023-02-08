package com.torresj.unseenusers.mappers;

import com.torresj.unseenusers.dtos.UserDto;
import com.torresj.unseenusers.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "id", expression = "java(null)")
  UserEntity toEntity(UserDto user);

  UserDto toUserDto(UserEntity userEntity);
}
