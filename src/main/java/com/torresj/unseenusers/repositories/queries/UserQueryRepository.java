package com.torresj.unseenusers.repositories.queries;

import com.torresj.unseenusers.entities.Role;
import com.torresj.unseenusers.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserQueryRepository extends CrudRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);
  Page<UserEntity> findAll(Pageable pageable);
  Page<UserEntity> findByEmailContainingIgnoreCase(String email, Pageable pageable);
  Page<UserEntity> findByEmailContainingIgnoreCaseAndRole(String email, Role role,Pageable pageable);
  Page<UserEntity> findByRole(Role role,Pageable pageable);
}
