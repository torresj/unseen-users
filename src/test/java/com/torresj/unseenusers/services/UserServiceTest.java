package com.torresj.unseenusers.services;

import static com.torresj.unseenusers.utils.EntityGenerator.GenerateGroup;
import static com.torresj.unseenusers.utils.EntityGenerator.GenerateIteration;
import static com.torresj.unseenusers.utils.EntityGenerator.GeneratePair;
import static com.torresj.unseenusers.utils.EntityGenerator.GenerateUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private final String email = "test@test.com";
  private final String password = "test";
  @Mock private UserQueryRepository userQueryRepository;
  @Mock private UserMutationRepository userMutationRepository;
  @Mock private UserGroupRelationMutationRepository userGroupRelationMutationRepository;
  @Mock private UserGroupRelationQueryRepository userGroupRelationQueryRepository;
  @Mock private PairQueryRepository pairQueryRepository;
  @Mock private PairMutationRepository pairMutationRepository;
  @Mock private GroupQueryRepository groupQueryRepository;
  @Mock private GroupMutationRepository groupMutationRepository;
  @Mock private IterationQueryRepository iterationQueryRepository;
  @Mock private IterationMutationRepository iterationMutationRepository;
  private UserService userService;

  @BeforeEach
  void setUp() {
    UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    PageMapper pageMapper = new PageMapper(userMapper);
    userService =
        new UserService(
            userQueryRepository,
            userMutationRepository,
            userGroupRelationMutationRepository,
            userGroupRelationQueryRepository,
            pairQueryRepository,
            pairMutationRepository,
            groupQueryRepository,
            groupMutationRepository,
            iterationQueryRepository,
            iterationMutationRepository,
            pageMapper,
            userMapper);
  }

  @Test
  @DisplayName("Get users without filters")
  void getUsersNoFilters() {
    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true);
    UserEntity userEntityMock2 =
        GenerateUser(email + 2, password, Role.USER, AuthProvider.UNSEEN, true);

    // Mocks
    when(userQueryRepository.findAll(any()))
        .thenReturn(
            new PageImpl<>(List.of(userEntityMock, userEntityMock2), Pageable.ofSize(2), 2));

    PageUserDto result = userService.users(0, 10, null, null);

    Assertions.assertEquals(2, result.getPageInfo().getTotalElements());
    Assertions.assertEquals(1, result.getPageInfo().getTotalPages());
    Assertions.assertEquals(0, result.getPageInfo().getPage());
    Assertions.assertEquals(2, result.getPageInfo().getElements());
    Assertions.assertEquals(2, result.getContent().size());
    Assertions.assertEquals(email, result.getContent().get(0).getEmail());
    Assertions.assertEquals(email + 2, result.getContent().get(1).getEmail());
  }

  @Test
  @DisplayName("Get users with filter")
  void getUsersWithFilters() {
    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true);

    // Mocks
    when(userQueryRepository.findByEmailContainingIgnoreCase(any(), any()))
        .thenReturn(new PageImpl<>(List.of(userEntityMock), Pageable.ofSize(1), 1));

    PageUserDto result = userService.users(0, 10, "filter", null);

    Assertions.assertEquals(1, result.getPageInfo().getTotalElements());
    Assertions.assertEquals(1, result.getPageInfo().getTotalPages());
    Assertions.assertEquals(0, result.getPageInfo().getPage());
    Assertions.assertEquals(1, result.getPageInfo().getElements());
    Assertions.assertEquals(1, result.getContent().size());
    Assertions.assertEquals(email, result.getContent().get(0).getEmail());
  }

  @Test
  @DisplayName("Get users with role")
  void getUsersWithRole() {
    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true);

    // Mocks
    when(userQueryRepository.findByRole(any(), any()))
        .thenReturn(new PageImpl<>(List.of(userEntityMock), Pageable.ofSize(1), 1));

    PageUserDto result = userService.users(0, 10, null, Role.USER);

    Assertions.assertEquals(1, result.getPageInfo().getTotalElements());
    Assertions.assertEquals(1, result.getPageInfo().getTotalPages());
    Assertions.assertEquals(0, result.getPageInfo().getPage());
    Assertions.assertEquals(1, result.getPageInfo().getElements());
    Assertions.assertEquals(1, result.getContent().size());
    Assertions.assertEquals(email, result.getContent().get(0).getEmail());
  }

  @Test
  @DisplayName("Get users with filter and role")
  void getUsersWithRoleAndFilter() {
    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true);

    // Mocks
    when(userQueryRepository.findByEmailContainingIgnoreCaseAndRole(any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(userEntityMock), Pageable.ofSize(1), 1));

    PageUserDto result = userService.users(0, 10, "filter", Role.USER);

    Assertions.assertEquals(1, result.getPageInfo().getTotalElements());
    Assertions.assertEquals(1, result.getPageInfo().getTotalPages());
    Assertions.assertEquals(0, result.getPageInfo().getPage());
    Assertions.assertEquals(1, result.getPageInfo().getElements());
    Assertions.assertEquals(1, result.getContent().size());
    Assertions.assertEquals(email, result.getContent().get(0).getEmail());
  }

  @Test
  @DisplayName("Get user by ID")
  void getUserByID() throws UserNotFoundException {
    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true);
    userEntityMock.setId(1L);

    // Mocks
    when(userQueryRepository.findById(userEntityMock.getId()))
        .thenReturn(Optional.of(userEntityMock));

    UserDto user = userService.user(userEntityMock.getId());

    Assertions.assertEquals(email, user.getEmail());
    Assertions.assertEquals(email, user.getName());
    Assertions.assertEquals(userEntityMock.getId(), user.getId());
    Assertions.assertEquals(AuthProvider.UNSEEN, user.getProvider());
    Assertions.assertEquals(Role.USER, user.getRole());
  }

  @Test
  @DisplayName("Get user by ID not found")
  void getUserByIDNotFound() {

    // Mocks
    when(userQueryRepository.findById(any())).thenReturn(Optional.empty());

    Assertions.assertThrows(
        UserNotFoundException.class,
        () -> userService.user(1),
        "User not found exception should be thrown");
  }

  @Test
  @DisplayName("Get user by email")
  void getUserByEmail() throws UserNotFoundException {
    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true);
    userEntityMock.setId(1L);

    // Mocks
    when(userQueryRepository.findByEmail(email)).thenReturn(Optional.of(userEntityMock));

    UserDto user = userService.user(email);

    Assertions.assertEquals(email, user.getEmail());
    Assertions.assertEquals(email, user.getName());
    Assertions.assertEquals(userEntityMock.getId(), user.getId());
    Assertions.assertEquals(AuthProvider.UNSEEN, user.getProvider());
    Assertions.assertEquals(Role.USER, user.getRole());
  }

  @Test
  @DisplayName("Get user by email not found")
  void getUserByEmailNotFound() {

    // Mocks
    when(userQueryRepository.findByEmail(any())).thenReturn(Optional.empty());

    Assertions.assertThrows(
        UserNotFoundException.class,
        () -> userService.user(""),
        "User not found exception should be thrown");
  }

  @Test
  @DisplayName("Register user")
  void registerUser() throws UserAlreadyExistsException {

    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true);
    userEntityMock.setId(1L);

    // Mocks
    when(userMutationRepository.save(any())).thenReturn(userEntityMock);

    UserDto user = userService.register(new UserRegisterDto(email, email, password));

    Assertions.assertEquals(email, user.getEmail());
    Assertions.assertEquals(email, user.getName());
    Assertions.assertEquals(userEntityMock.getId(), user.getId());
    Assertions.assertEquals(AuthProvider.UNSEEN, user.getProvider());
    Assertions.assertEquals(Role.USER, user.getRole());
  }

  @Test
  @DisplayName("Register user that already exists")
  void registerUserAlreadyExists() {

    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true);
    userEntityMock.setId(1L);

    // Mocks
    when(userQueryRepository.findByEmail(any())).thenReturn(Optional.of(userEntityMock));

    Assertions.assertThrows(
        UserAlreadyExistsException.class,
        () -> userService.register(new UserRegisterDto(email, email, password)),
        "User already exists exception should be thrown");
  }

  @Test
  @DisplayName("Update user")
  void updateUser() throws UserNotFoundException {

    UserEntity userEntityMock =
        GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, false);
    UserEntity userEntityMock2 =
        GenerateUser(email + "2", password + "2", Role.ADMIN, AuthProvider.UNSEEN, true);
    userEntityMock.setId(1L);
    userEntityMock2.setId(1L);

    // Mocks
    when(userQueryRepository.findById(any())).thenReturn(Optional.of(userEntityMock));
    when(userMutationRepository.save(any())).thenReturn(userEntityMock2);

    UserDto user =
        userService.update(1L, new UpdateUserDto(email + "2", password + "2", true, Role.ADMIN));

    Assertions.assertEquals(email + "2", user.getEmail());
    Assertions.assertEquals(email + "2", user.getName());
    Assertions.assertEquals(userEntityMock.getId(), user.getId());
    Assertions.assertEquals(AuthProvider.UNSEEN, user.getProvider());
    Assertions.assertEquals(Role.ADMIN, user.getRole());
    Assertions.assertTrue(user.isValidated());
  }

  @Test
  @DisplayName("Update user that not exists")
  void updateUserThatNotExists() {

    // Mocks
    when(userQueryRepository.findById(any())).thenReturn(Optional.empty());

    Assertions.assertThrows(
        UserNotFoundException.class,
        () -> userService.update(1L, new UpdateUserDto(email, password, true, Role.ADMIN)),
        "User not found exception should be thrown");
  }

  @Test
  @DisplayName("Delete user")
  void deleteUser()throws UserNotFoundException {
    UserEntity userEntityMock = GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true);
    UserEntity userEntityMock2 =
        GenerateUser("test2@test.com", password, Role.USER, AuthProvider.UNSEEN, true);
    GroupEntity groupEntityMock = GenerateGroup("test", "test", userEntityMock.getId(), false);
    IterationEntity iterationEntityMock = GenerateIteration(groupEntityMock.getId());
    PairEntity pairEntityMock =
        GeneratePair(iterationEntityMock.getId(), userEntityMock.getId(), userEntityMock2.getId());

    UserGroupRelationEntity userGroupRelation =
        UserGroupRelationEntity.builder()
            .groupId(groupEntityMock.getId())
            .userId(userEntityMock.getId())
            .build();

    // Mocks
    when(userQueryRepository.findById(userEntityMock.getId()))
            .thenReturn(Optional.of(userEntityMock));
    when(userGroupRelationQueryRepository.findByUserId(userEntityMock.getId()))
        .thenReturn(List.of(userGroupRelation));
    when(pairQueryRepository.findByGiftingUserIdOrGiftedUserId(
            userEntityMock.getId(), userEntityMock.getId()))
        .thenReturn(List.of(pairEntityMock));
    when(groupQueryRepository.findByOwner(userEntityMock.getId()))
        .thenReturn(Collections.emptyList());

    userService.delete(userEntityMock.getId());
  }
}
