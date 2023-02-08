package com.torresj.unseenusers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.unseenusers.dtos.PageUserDto;
import com.torresj.unseenusers.dtos.UserDto;
import com.torresj.unseenusers.dtos.UserRegisterDto;
import com.torresj.unseenusers.entities.AuthProvider;
import com.torresj.unseenusers.entities.Role;
import com.torresj.unseenusers.entities.UserEntity;
import com.torresj.unseenusers.repositories.mutations.UserMutationRepository;
import com.torresj.unseenusers.repositories.queries.UserQueryRepository;
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

import java.util.Optional;

import static com.torresj.unseenusers.utils.TestUtils.GenerateUser;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UnseenUsersApplicationTests {

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

  @BeforeEach
  public void init() {
    userMutationRepository.deleteAll();
  }

  @Test
  @DisplayName("Get users")
  void getUsers() throws Exception {
    // Create a valid user in DB
    UserEntity user =
        userMutationRepository.save(
            GenerateUser(email, password, Role.ADMIN, AuthProvider.UNSEEN, true));
    UserEntity user2 =
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
    Assertions.assertEquals(email, user.get().getName());
    Assertions.assertEquals(AuthProvider.UNSEEN, user.get().getProvider());
    Assertions.assertEquals(Role.USER, user.get().getRole());
    Assertions.assertEquals(0, user.get().getNumLogins());
    Assertions.assertNotNull(user.get().getCreateAt());
    Assertions.assertNotNull(user.get().getUpdateAt());
    Assertions.assertNull(user.get().getLastConnection());
    Assertions.assertNull(user.get().getPhotoUrl());
    Assertions.assertTrue(location.contains("/v1/users/" + user.get().getId()));
  }

  @Test
  @DisplayName("Register user already exists")
  void registerUserAlreadyExists() throws Exception {
    // Create a valid user in DB
    UserEntity userEntity =
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
}
