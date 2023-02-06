package com.torresj.unseenusers.exceptions;

public class UserAlreadyExistsException extends Exception {
  public UserAlreadyExistsException(String email) {
    super("User " + email + " already exists");
  }
}
