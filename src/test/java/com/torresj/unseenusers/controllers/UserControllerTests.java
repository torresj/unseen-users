package com.torresj.unseenusers.controllers;

import static com.torresj.unseenusers.utils.EntityGenerator.GenerateGroup;
import static com.torresj.unseenusers.utils.EntityGenerator.GenerateUser;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.unseen.entities.*;
import com.torresj.unseen.repositories.mutations.GroupMutationRepository;
import com.torresj.unseen.repositories.mutations.UserGroupRelationMutationRepository;
import com.torresj.unseen.repositories.mutations.UserMutationRepository;
import com.torresj.unseen.repositories.queries.UserQueryRepository;
import com.torresj.unseenusers.dtos.PageUserDto;
import com.torresj.unseenusers.dtos.UpdateUserDto;
import com.torresj.unseenusers.dtos.UserDto;
import com.torresj.unseenusers.dtos.UserRegisterDto;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

  private final String email = "test@test.com";
  private final String password = "test";

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private MockMvc mockMvc;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired private UserMutationRepository userMutationRepository;
  @Autowired private UserQueryRepository userQueryRepository;
  @Autowired private GroupMutationRepository groupMutationRepository;
  @Autowired private UserGroupRelationMutationRepository userGroupRelationMutationRepository;

  @BeforeEach
  public void init() {
    userMutationRepository.deleteAll();
    groupMutationRepository.deleteAll();
  }

  @Test
  @DisplayName("Get users")
  void getUsers() throws Exception {
    // Create a valid user in DB
    userMutationRepository.save(
        GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));
    userMutationRepository.save(
        GenerateUser(email + 2, password, Role.ADMIN, AuthProvider.UNSEEN, true));

    // Get
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/v1/users?page=0&elements=10")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    // Result
    var content = result.andReturn().getResponse().getContentAsString();
    PageUserDto page = objectMapper.readValue(content, PageUserDto.class);

    Assertions.assertEquals(2, page.getPageInfo().getTotalElements());
    Assertions.assertEquals(1, page.getPageInfo().getTotalPages());
    Assertions.assertEquals(0, page.getPageInfo().getPage());
    Assertions.assertEquals(10, page.getPageInfo().getElements());
    Assertions.assertEquals(2, page.getContent().size());
    Assertions.assertEquals(email, page.getContent().get(1).getEmail());
    Assertions.assertEquals(email + 2, page.getContent().get(0).getEmail());
  }

  @Test
  @DisplayName("Get user by ID")
  void getUserByID() throws Exception {
    // Create a valid user in DB
    UserEntity userEntity =
        userMutationRepository.save(
            GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true));

    // Get
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/v1/users/" + userEntity.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    // Result
    var content = result.andReturn().getResponse().getContentAsString();
    UserDto user = objectMapper.readValue(content, UserDto.class);

    Assertions.assertEquals(email, user.getEmail());
    Assertions.assertEquals(email, user.getName());
    Assertions.assertEquals(userEntity.getId(), user.getId());
    Assertions.assertEquals(AuthProvider.UNSEEN, user.getProvider());
    Assertions.assertEquals(Role.USER, user.getRole());
    Assertions.assertEquals(1, user.getNumLogins());
    Assertions.assertNotNull(user.getCreateAt());
    Assertions.assertNotNull(user.getUpdateAt());
    Assertions.assertNotNull(user.getLastConnection());
    Assertions.assertNull(user.getPhotoUrl());
  }

  @Test
  @DisplayName("Get user by ID that not exists")
  void getUserNotFound() throws Exception {

    // Get
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/v1/users/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("User 1 not found", error);
  }

  @Test
  @DisplayName("Get user logged (by email)")
  void getUserByEmail() throws Exception {
    // Create a valid user in DB
    UserEntity userEntity =
        userMutationRepository.save(
            GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true));

    // Get
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/v1/users/me?email=" + email)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    // Result
    var content = result.andReturn().getResponse().getContentAsString();
    UserDto user = objectMapper.readValue(content, UserDto.class);

    Assertions.assertEquals(email, user.getEmail());
    Assertions.assertEquals(email, user.getName());
    Assertions.assertEquals(userEntity.getId(), user.getId());
    Assertions.assertEquals(AuthProvider.UNSEEN, user.getProvider());
    Assertions.assertEquals(Role.USER, user.getRole());
    Assertions.assertEquals(1, user.getNumLogins());
    Assertions.assertNotNull(user.getCreateAt());
    Assertions.assertNotNull(user.getUpdateAt());
    Assertions.assertNotNull(user.getLastConnection());
    Assertions.assertNull(user.getPhotoUrl());
  }

  @Test
  @DisplayName("Get user logged (by email) that not exists")
  void getUserByEmailNotFound() throws Exception {

    // Get
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/v1/users/me?email=" + email)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("User " + email + " not found", error);
  }

  @Test
  @DisplayName("Register new user")
  void registerUser() throws Exception {
    // user to register
    UserRegisterDto userRegister = new UserRegisterDto(email, email, password);

    // Register
    var result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/v1/users/register")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRegister)))
            .andExpect(status().isCreated());

    // Result
    String location = result.andReturn().getResponse().getHeader("location");
    Optional<UserEntity> user = userQueryRepository.findByEmail(email);

    Assertions.assertTrue(user.isPresent());
    Assertions.assertEquals(email, user.get().getEmail());
    Assertions.assertEquals(password, user.get().getPassword());
    Assertions.assertEquals(email, user.get().getName());
    Assertions.assertEquals(AuthProvider.UNSEEN, user.get().getProvider());
    Assertions.assertEquals(Role.USER, user.get().getRole());
    Assertions.assertEquals(0, user.get().getNumLogins());
    Assertions.assertNotNull(user.get().getCreateAt());
    Assertions.assertNotNull(user.get().getUpdateAt());
    Assertions.assertNull(user.get().getLastConnection());
    Assertions.assertNull(user.get().getPhotoUrl());
    Assertions.assertNotNull(location);
    Assertions.assertTrue(location.contains("/v1/users/" + user.get().getId()));
  }

  @Test
  @DisplayName("Register user already exists")
  void registerUserAlreadyExists() throws Exception {
    // Create a valid user in DB
    userMutationRepository.save(
        GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true));

    // user to register
    UserRegisterDto userRegister = new UserRegisterDto(email, email, password);

    // Register
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/v1/users/register")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegister)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Update user")
  void updateUser() throws Exception {
    // Create a valid user in DB
    UserEntity userEntity =
        userMutationRepository.save(
            GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, false));

    // Update user data
    UpdateUserDto updateUserDto = new UpdateUserDto(email + "2", password + "2", true, Role.ADMIN);

    // Register
    mockMvc
        .perform(
            MockMvcRequestBuilders.patch("/v1/users/" + userEntity.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDto)))
        .andExpect(status().isOk());

    Optional<UserEntity> user = userQueryRepository.findByEmail(email);

    Assertions.assertTrue(user.isPresent());
    Assertions.assertEquals(email, user.get().getEmail());
    Assertions.assertEquals(email + "2", user.get().getName());
    Assertions.assertEquals(password + "2", user.get().getPassword());
    Assertions.assertEquals(AuthProvider.UNSEEN, user.get().getProvider());
    Assertions.assertEquals(Role.ADMIN, user.get().getRole());
    Assertions.assertEquals(1, user.get().getNumLogins());
    Assertions.assertNotNull(user.get().getCreateAt());
    Assertions.assertNotNull(user.get().getUpdateAt());
    Assertions.assertNotNull(user.get().getLastConnection());
    Assertions.assertNull(user.get().getPhotoUrl());
  }

  @Test
  @DisplayName("Update user that not exists")
  void updateUserNotExists() throws Exception {

    // Update user data
    UpdateUserDto updateUserDto = new UpdateUserDto(email, password, true, Role.ADMIN);

    // Register
    mockMvc
        .perform(
            MockMvcRequestBuilders.patch("/v1/users/" + new Random().nextInt())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Delete user that not exists")
  void deleteUserNotExists() throws Exception {

    // Register
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete("/v1/users/" + new Random().nextInt())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Delete user that is owner of a group without more users")
  void deleteUserWithOwnerGroup() throws Exception {

    UserEntity userEntity =
        userMutationRepository.save(
            GenerateUser(email, password, Role.USER, AuthProvider.UNSEEN, true));
    GroupEntity groupEntity =
        groupMutationRepository.save(GenerateGroup("Group", "test", userEntity.getId(), true));

    userGroupRelationMutationRepository.save(
        UserGroupRelationEntity.builder()
            .userId(userEntity.getId())
            .groupId(groupEntity.getId())
            .build());

    // Register
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete("/v1/users/" + userEntity.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    Assertions.assertFalse(userMutationRepository.findById(userEntity.getId()).isPresent());
    Assertions.assertFalse(groupMutationRepository.findById(groupEntity.getId()).isPresent());
  }
}
