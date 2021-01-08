package com.x2iq.tunneling.mappingparser.exceptions;

import java.nio.file.Path;

public class MappingFileIsNotReadableException extends MappingFileException {

  public MappingFileIsNotReadableException(Path mappingFile) {
    super("You don't have read permission for '" + mappingFile.toAbsolutePath() + "' mapping file");
  }

  @Override
  public int getExitCode() {
    return 4;
  }
}
