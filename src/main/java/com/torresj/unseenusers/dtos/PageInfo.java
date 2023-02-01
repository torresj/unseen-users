package com.torresj.unseenusers.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PageInfo {
  private int page;
  private int elements;
  private int totalPages;
  private long totalElements;
  private boolean isLastPage;
}
