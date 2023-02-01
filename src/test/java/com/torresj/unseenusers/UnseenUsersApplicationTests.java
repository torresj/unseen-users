package com.torresj.unseenusers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torresj.unseenusers.dtos.PageUser;
import com.torresj.unseenusers.dtos.User;
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
    PageUser page = objectMapper.readValue(content, PageUser.class);

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
    User user = objectMapper.readValue(content, User.class);

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
                MockMvcRequestBuilders.get("/v1/users/1")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

    var error = result.andReturn().getResponse().getErrorMessage();

    Assertions.assertEquals("User 1 not found", error);
  }
}
