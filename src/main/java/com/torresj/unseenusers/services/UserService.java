package com.torresj.unseenusers.services;

import com.torresj.unseenusers.dtos.PageUser;
import com.torresj.unseenusers.dtos.User;
import com.torresj.unseenusers.entities.Role;
import com.torresj.unseenusers.entities.UserEntity;
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

  public PageUser users(int page, int elements, String filter, Role role) {
    log.debug("[USER SERVICE] Getting users");

    // Create pageRequest
    var pageRequest = PageRequest.of(page, elements, Sort.by("createAt").descending());

    // Create Page
    Page<UserEntity> pageFromDB = null;

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

  public User user(long id) throws UserNotFoundException {
    log.debug("[USER SERVICE] Getting user " + id);

    // Finding user in DB
    UserEntity userEntity =
        userQueryRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

    // Mapping user
    User user = userMapper.toUserDto(userEntity);

    log.debug("[USER SERVICE] User found: " + user);

    return user;
  }
}
