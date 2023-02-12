package com.torresj.unseenusers.controllers;

import com.torresj.unseen.entities.Role;
import com.torresj.unseenusers.dtos.PageUserDto;
import com.torresj.unseenusers.dtos.UpdateUserDto;
import com.torresj.unseenusers.dtos.UserDto;
import com.torresj.unseenusers.dtos.UserRegisterDto;
import com.torresj.unseenusers.exceptions.UserAlreadyExistsException;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
                  schema = @Schema(implementation = PageUserDto.class))
            })
      })
  @GetMapping
  public ResponseEntity<PageUserDto> users(
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
                  schema = @Schema(implementation = UserDto.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = {@Content(mediaType = "application/json")})
      })
  @GetMapping("/{id}")
  public ResponseEntity<UserDto> user(@Parameter(description = "User id") @PathVariable long id) {
    try {
      log.info("[USERS] Getting user id " + id);

      UserDto user = userService.user(id);

      log.info("[USERS] User " + id + " found");

      return ResponseEntity.ok(user);
    } catch (UserNotFoundException exception) {
      log.error(exception.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
    }
  }

  @Operation(summary = "Get user by email")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Success",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = UserDto.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = {@Content(mediaType = "application/json")})
      })
  @GetMapping("/me")
  public ResponseEntity<UserDto> me(
      @Parameter(description = "User email") @RequestParam String email) {
    try {
      log.info("[USERS] Getting user by email " + email);

      UserDto user = userService.user(email);

      log.info("[USERS] User " + email + " found");

      return ResponseEntity.ok(user);
    } catch (UserNotFoundException exception) {
      log.error(exception.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
    }
  }

  @Operation(summary = "Register Unseen user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created",
            content = {@Content()}),
        @ApiResponse(
            responseCode = "400",
            description = "User already exists",
            content = {@Content()})
      })
  @PostMapping("/register")
  public ResponseEntity register(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Register user data",
              required = true,
              content = @Content(schema = @Schema(implementation = UserRegisterDto.class)))
          @RequestBody
          UserRegisterDto userRegister) {
    try {
      log.info("[USERS] Creating user " + userRegister.email());

      UserDto userCreated = userService.register(userRegister);

      log.info("[USERS] User " + userCreated.getEmail() + " created");

      return ResponseEntity.created(
              ServletUriComponentsBuilder.fromCurrentContextPath()
                  .path("/v1/users/" + userCreated.getId())
                  .buildAndExpand(userCreated)
                  .toUri())
          .build();
    } catch (UserAlreadyExistsException exception) {
      log.error(exception.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
  }

  @Operation(summary = "Update user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated",
            content = {@Content()}),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = {@Content()})
      })
  @PatchMapping("/{id}")
  public ResponseEntity update(
      @Parameter(description = "User id") @PathVariable long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Update user data",
              required = true,
              content = @Content(schema = @Schema(implementation = UpdateUserDto.class)))
          @RequestBody
          UpdateUserDto updateUserDto) {
    try {
      log.info("[USERS] Updating user " + id);

      UserDto userUpdated = userService.update(id, updateUserDto);

      log.info("[USERS] User " + userUpdated.getId() + " updated");

      return ResponseEntity.ok().build();
    } catch (UserNotFoundException exception) {
      log.error(exception.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
    }
  }
}
