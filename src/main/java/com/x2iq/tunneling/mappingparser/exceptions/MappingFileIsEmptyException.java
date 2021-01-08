package com.x2iq.tunneling.mappingparser.exceptions;

import java.nio.file.Path;

public class MappingFileIsEmptyException extends MappingFileException {

  public MappingFileIsEmptyException(Path mappingFile) {
    super("Mapping file '" + mappingFile.toAbsolutePath() + "' is empty");
  }

  @Override
  public int getExitCode() {
    return 7;
  }
}
