package com.torresj.unseenusers.services;

import com.torresj.unseen.entities.*;
import com.torresj.unseen.repositories.mutations.*;
import com.torresj.unseen.repositories.queries.*;
import com.torresj.unseenusers.dtos.PageUserDto;
import com.torresj.unseenusers.dtos.UpdateUserDto;
import com.torresj.unseenusers.dtos.UserDto;
import com.torresj.unseenusers.dtos.UserRegisterDto;
import com.torresj.unseenusers.exceptions.UserAlreadyExistsException;
import com.torresj.unseenusers.exceptions.UserNotFoundException;
import com.torresj.unseenusers.mappers.PageMapper;
import com.torresj.unseenusers.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
  private final UserQueryRepository userQueryRepository;
  private final UserMutationRepository userMutationRepository;
  private final UserGroupRelationMutationRepository userGroupRelationMutationRepository;
  private final UserGroupRelationQueryRepository userGroupRelationQueryRepository;
  private final PairQueryRepository pairQueryRepository;
  private final PairMutationRepository pairMutationRepository;
  private final GroupQueryRepository groupQueryRepository;
  private final GroupMutationRepository groupMutationRepository;
  private final IterationQueryRepository iterationQueryRepository;
  private final IterationMutationRepository iterationMutationRepository;
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
        updateUserDto.getName() == null || updateUserDto.getName().isBlank()
            ? userEntity.getName()
            : updateUserDto.getName());
    userEntity.setPassword(
        updateUserDto.getPassword() == null || updateUserDto.getPassword().isBlank()
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

  public void delete(long id) {
    log.debug("[USER SERVICE] deleting user " + id);
    userMutationRepository.deleteById(id);

    log.debug("[USER SERVICE] deleting any relation between groups and user " + id);
    userGroupRelationMutationRepository.deleteAll(
        userGroupRelationQueryRepository.findByUserId(id));

    log.debug("[USER SERVICE] updating any pair that contains user " + id);
    pairQueryRepository
        .findByGiftingUserIdOrGiftedUserId(id, id)
        .forEach(
            pairEntity -> {
              if (pairEntity.getGiftingUserId() == id) {
                pairMutationRepository.save(
                    PairEntity.builder()
                        .id(pairEntity.getId())
                        .iterationId(pairEntity.getIterationId())
                        .giftingUserId(-1L)
                        .giftedUserId(pairEntity.getGiftedUserId())
                        .createAt(pairEntity.getCreateAt())
                        .build());
              }
              if (pairEntity.getGiftedUserId() == id) {
                pairMutationRepository.save(
                    PairEntity.builder()
                        .id(pairEntity.getId())
                        .iterationId(pairEntity.getIterationId())
                        .giftingUserId(pairEntity.getGiftingUserId())
                        .giftedUserId(-1L)
                        .createAt(pairEntity.getCreateAt())
                        .build());
              }
            });

    log.debug("[USER SERVICE] updating any group that is own by user " + id);
    groupQueryRepository
        .findByOwner(id)
        .forEach(
            groupEntity -> {
              List<UserEntity> groupUsers =
                  userGroupRelationQueryRepository.findByGroupId(groupEntity.getId()).stream()
                      .map(
                          userGroupRelationEntity ->
                              userQueryRepository.findById(userGroupRelationEntity.getUserId()))
                      .filter(Optional::isPresent)
                      .map(Optional::get)
                      .toList();
              if (!groupUsers.isEmpty()) {
                groupEntity.setOwner(groupUsers.get(0).getId());
                groupMutationRepository.save(groupEntity);
              } else {
                groupMutationRepository.delete(groupEntity);
                List<IterationEntity> iterations =
                    iterationQueryRepository.findByGroupId(groupEntity.getId());
                iterationMutationRepository.deleteAll(iterations);
                iterations.forEach(
                    iterationEntity ->
                        pairMutationRepository.deleteAll(
                            pairQueryRepository.findByIterationId(iterationEntity.getId())));
              }
            });
  }
}
