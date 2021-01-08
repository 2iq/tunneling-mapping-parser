package com.x2iq.tunneling.mappingparser.exceptions;

import java.nio.file.Path;

public class NoMappingInMappingFileException extends MappingFileException {

  public NoMappingInMappingFileException(Path mappingFile) {
    super("Mapping file '" + mappingFile.toAbsolutePath() + "' didn't contain any mapping");
  }

  @Override
  public int getExitCode() {
    return 8;
  }
}
