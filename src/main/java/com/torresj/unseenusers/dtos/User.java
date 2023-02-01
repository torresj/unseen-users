package com.torresj.unseenusers.dtos;

import com.torresj.unseenusers.entities.AuthProvider;
import com.torresj.unseenusers.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
  private long id;
  private String email;
  private String name;
  private String photoUrl;
  private long numLogins;
  private boolean validated;
  private AuthProvider provider;
  private Role role;
  private LocalDateTime createAt;
  private LocalDateTime updateAt;
  private LocalDateTime lastConnection;
}
