package com.torresj.unseenusers.mappers;

import com.torresj.unseen.entities.UserEntity;
import com.torresj.unseenusers.dtos.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "id", expression = "java(null)")
  UserEntity toEntity(UserDto user);

  UserDto toUserDto(UserEntity userEntity);
}
