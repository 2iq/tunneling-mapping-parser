package com.x2iq.tunneling.mappingparser.exceptions;

import java.nio.file.Path;

public class MappingFileIsDirectoryException extends MappingFileException {

  public MappingFileIsDirectoryException(Path mappingFile) {
    super("Given mapping file '" + mappingFile.toAbsolutePath() + "' is directory");
  }

  @Override
  public int getExitCode() {
    return 5;
  }
}
