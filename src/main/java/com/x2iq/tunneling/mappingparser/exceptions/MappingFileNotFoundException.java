package com.x2iq.tunneling.mappingparser.exceptions;

import java.nio.file.Path;

public class MappingFileNotFoundException extends MappingFileException {

  public MappingFileNotFoundException(Path missedFile) {
    super("Mapping file '" + missedFile.toAbsolutePath() + "' not found");
  }

  @Override
  public int getExitCode() {
    return 3;
  }
}
