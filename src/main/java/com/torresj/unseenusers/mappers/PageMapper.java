package com.torresj.unseenusers.mappers;

import com.torresj.unseenusers.dtos.PageInfo;
import com.torresj.unseenusers.dtos.PageUser;
import com.torresj.unseenusers.entities.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PageMapper {
  private final UserMapper userMapper;

  public PageUser toPageUser(Page<UserEntity> page) {
    return PageUser.builder()
        .content(page.getContent().stream().map(userMapper::toUserDto).collect(Collectors.toList()))
        .pageInfo(
            PageInfo.builder()
                .page(page.getPageable().getPageNumber())
                .elements(page.getPageable().getPageSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .build())
        .build();
  }
}
