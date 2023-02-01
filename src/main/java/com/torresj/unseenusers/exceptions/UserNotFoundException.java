package com.torresj.unseenusers.exceptions;

public class UserNotFoundException extends Exception {
  public UserNotFoundException(long id) {
    super("User " + id + " not found");
  }
}
