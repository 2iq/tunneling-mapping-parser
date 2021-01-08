package com.x2iq.tunneling.mappingparser.exceptions;

public abstract class MappingFileException extends RuntimeException {

  public MappingFileException(String message) {
    super(message);
  }

  public MappingFileException(String message, Throwable cause) {
    super(message, cause);
  }

  public abstract int getExitCode();
}
