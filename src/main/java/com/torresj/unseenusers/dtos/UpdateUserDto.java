package com.torresj.unseenusers.dtos;

import com.torresj.unseenusers.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class UpdateUserDto {
  private String name;
  private String password;
  private boolean validated;
  private Role role;
}
