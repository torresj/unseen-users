package com.torresj.unseenusers.controllers;

import com.torresj.unseenusers.dtos.PageUser;
import com.torresj.unseenusers.dtos.User;
import com.torresj.unseenusers.entities.Role;
import com.torresj.unseenusers.exceptions.UserNotFoundException;
import com.torresj.unseenusers.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(summary = "Get users")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Success",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PageUser.class))
            })
      })
  @GetMapping
  public ResponseEntity<PageUser> users(
      @Parameter(description = "Number of page") @RequestParam int page,
      @Parameter(description = "Number of elements per page") @RequestParam int elements,
      @Parameter(description = "Filter to find by email") @RequestParam(required = false)
          String filter,
      @Parameter(description = "Role") @RequestParam(required = false) Role role) {

    log.info(
        "[USERS] Getting users for page "
            + page
            + " elements "
            + elements
            + " filter "
            + filter
            + " role "
            + role);

    int elementsPerPage = elements > 20 || elements < 1 ? 20 : elements;

    var result = userService.users(page, elementsPerPage, filter, role);

    return ResponseEntity.ok(result);
  }

  @Operation(summary = "Get user by id")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Success",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = User.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = {@Content(mediaType = "application/json")})
      })
  @GetMapping("/{id}")
  public ResponseEntity<User> user(@Parameter(description = "User id") @PathVariable long id) {
    try {
      log.info("[USERS] Getting user id " + id);

      User user = userService.user(id);

      log.info("[USERS] User " + id + " found");

      return ResponseEntity.ok(user);
    } catch (UserNotFoundException exception) {
      log.error(exception.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
    }
  }
}
