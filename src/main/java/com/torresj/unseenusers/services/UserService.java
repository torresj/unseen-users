package com.torresj.unseenusers.services;

import com.torresj.unseenusers.dtos.PageUserDto;
import com.torresj.unseenusers.dtos.UpdateUserDto;
import com.torresj.unseenusers.dtos.UserDto;
import com.torresj.unseenusers.dtos.UserRegisterDto;
import com.torresj.unseenusers.entities.AuthProvider;
import com.torresj.unseenusers.entities.Role;
import com.torresj.unseenusers.entities.UserEntity;
import com.torresj.unseenusers.exceptions.UserAlreadyExistsException;
import com.torresj.unseenusers.exceptions.UserNotFoundException;
import com.torresj.unseenusers.mappers.PageMapper;
import com.torresj.unseenusers.mappers.UserMapper;
import com.torresj.unseenusers.repositories.mutations.UserMutationRepository;
import com.torresj.unseenusers.repositories.queries.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
  private final UserQueryRepository userQueryRepository;
  private final UserMutationRepository userMutationRepository;
  private final PageMapper pageMapper;
  private final UserMapper userMapper;

  public PageUserDto users(int page, int elements, String filter, Role role) {
    log.debug("[USER SERVICE] Getting users");

    // Create pageRequest
    var pageRequest = PageRequest.of(page, elements, Sort.by("createAt").descending());

    // Create Page
    Page<UserEntity> pageFromDB;

    // Check filters
    if (filter != null && role != null) {
      pageFromDB =
          userQueryRepository.findByEmailContainingIgnoreCaseAndRole(filter, role, pageRequest);
    } else if (filter == null && role != null) {
      pageFromDB = userQueryRepository.findByRole(role, pageRequest);
    } else if (filter != null) {
      pageFromDB = userQueryRepository.findByEmailContainingIgnoreCase(filter, pageRequest);
    } else {
      pageFromDB = userQueryRepository.findAll(pageRequest);
    }

    var result = pageMapper.toPageUser(pageFromDB);
    log.debug("[USER SERVICE] Users: " + result);

    return result;
  }

  public UserDto user(long id) throws UserNotFoundException {
    log.debug("[USER SERVICE] Getting user " + id);

    // Finding user in DB
    UserEntity userEntity =
        userQueryRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

    // Mapping user
    UserDto user = userMapper.toUserDto(userEntity);

    log.debug("[USER SERVICE] User found: " + user);

    return user;
  }

  public UserDto user(String email) throws UserNotFoundException {
    log.debug("[USER SERVICE] Getting user " + email);

    // Finding user in DB
    UserEntity userEntity =
        userQueryRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

    // Mapping user
    UserDto user = userMapper.toUserDto(userEntity);

    log.debug("[USER SERVICE] User found: " + user);

    return user;
  }

  public UserDto register(UserRegisterDto userRegister) throws UserAlreadyExistsException {
    log.debug("[USER SERVICE] Saving user " + userRegister.email());

    // Finding user in DB
    if (userQueryRepository.findByEmail(userRegister.email()).isPresent())
      throw new UserAlreadyExistsException(userRegister.email());

    // Creating user Entity
    UserEntity userEntity =
        UserEntity.builder()
            .email(userRegister.email())
            .name(userRegister.name())
            .password(userRegister.password())
            .role(Role.USER)
            .provider(AuthProvider.UNSEEN)
            .build();

    // Saving entity
    UserEntity userEntityFromDB = userMutationRepository.save(userEntity);

    // Mapping to User
    UserDto user = userMapper.toUserDto(userEntityFromDB);

    log.debug("[USER SERVICE] User created: " + user);

    return user;
  }

  public UserDto update(long id, UpdateUserDto updateUserDto) throws UserNotFoundException {
    log.debug("[USER SERVICE] Updating user " + id + " " + updateUserDto);

    // Finding user in DB
    UserEntity userEntity =
        userQueryRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

    // Updating user
    userEntity.setName(
        updateUserDto.getName().isBlank() ? userEntity.getName() : updateUserDto.getName());
    userEntity.setPassword(
        updateUserDto.getPassword().isBlank()
            ? userEntity.getPassword()
            : updateUserDto.getPassword());
    userEntity.setRole(
        updateUserDto.getRole() == null ? userEntity.getRole() : updateUserDto.getRole());
    userEntity.setValidated(userEntity.isValidated() || updateUserDto.isValidated());

    // Saving entity
    UserEntity userEntityFromDB = userMutationRepository.save(userEntity);

    // Mapping to User
    UserDto user = userMapper.toUserDto(userEntityFromDB);

    log.debug("[USER SERVICE] User updated: " + user);

    return user;
  }
}
