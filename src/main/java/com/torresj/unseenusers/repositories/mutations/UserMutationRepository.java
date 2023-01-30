package com.torresj.unseenusers.repositories.mutations;

import com.torresj.unseenusers.entities.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface UserMutationRepository extends CrudRepository<UserEntity, Long> {}
