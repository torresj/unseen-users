package com.torresj.unseenusers.services;

import com.torresj.unseenusers.dtos.PageUser;
import com.torresj.unseenusers.dtos.User;
import com.torresj.unseenusers.dtos.UserRegister;
import com.torresj.unseenusers.entities.AuthProvider;
import com.torresj.unseenusers.entities.Role;
import com.torresj.unseenusers.entities.UserEntity;
import com.torresj.unseenusers.exceptions.UserAlreadyExistsException;
import com.torresj.unseenusers.exceptions.UserNotFoundException;
import com.torresj.unseenusers.mappers.PageMapper;
import com.torresj.unseenusers.mappers.UserMapper;
import com.torresj.unseenusers.repositories.mutations.UserMutationRepository;
import com.torresj.unseenusers.repositories.queries.UserQueryRepository;
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

import java.util.List;
import java.util.Optional;

import static com.torresj.unseenusers.utils.TestUtils.GenerateUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private final String email = "test@test.com";
  private final String password = "test";
  @Mock private UserQueryRepository userQueryRepository;
  @Mock private UserMutationRepository userMutationRepository;
  private PageMapper pageMapper;
  private UserService userService;

  @BeforeEach
  void setUp() {
    UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    pageMapper = new PageMapper(userMapper);
    userService =
        new UserService(userQueryRepository, userMutationRepository, pageMapper, userMapper);
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

    PageUser result = userService.users(0, 10, null, null);

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

    PageUser result = userService.users(0, 10, "filter", null);

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

    PageUser result = userService.users(0, 10, null, Role.USER);

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

    PageUser result = userService.users(0, 10, "filter", Role.USER);

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
    userEntityMock.setId(1l);

    // Mocks
    when(userQueryRepository.findById(userEntityMock.getId()))
        .thenReturn(Optional.of(userEntityMock));

    User user = userService.user(userEntityMock.getId());

    Assertions.assertEquals(email, user.getEmail());
    Assertions.assertEquals(email, user.getName());
    Assertions.assertEquals(userEntityMock.getId(), user.getId());
    Assertions.assertEquals(AuthProvider.UNSEEN, user.getProvider());
    Assertions.assertEquals(Role.USER, user.getRole());
  }

  @Test
  @DisplayName("Get user by ID not found")
  void getUserByIDNotFound() throws UserNotFoundException {

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
    userEntityMock.setId(1l);

    // Mocks
    when(userQueryRepository.findByEmail(email)).thenReturn(Optional.of(userEntityMock));

    User user = userService.user(email);

    Assertions.assertEquals(email, user.getEmail());
    Assertions.assertEquals(email, user.getName());
    Assertions.assertEquals(userEntityMock.getId(), user.getId());
    Assertions.assertEquals(AuthProvider.UNSEEN, user.getProvider());
    Assertions.assertEquals(Role.USER, user.getRole());
  }

  @Test
  @DisplayName("Get user by email not found")
  void getUserByEmailNotFound() throws UserNotFoundException {

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
    userEntityMock.setId(1l);

    // Mocks
    when(userMutationRepository.save(any())).thenReturn(userEntityMock);

    User user = userService.register(new UserRegister(email, email, password));

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
    userEntityMock.setId(1l);

    // Mocks
    when(userQueryRepository.findByEmail(any())).thenReturn(Optional.of(userEntityMock));

    Assertions.assertThrows(
        UserAlreadyExistsException.class,
        () -> userService.register(new UserRegister(email, email, password)),
        "User already exists exception should be thrown");
  }
}
