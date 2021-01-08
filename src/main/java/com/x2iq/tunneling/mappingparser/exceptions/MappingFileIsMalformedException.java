package com.x2iq.tunneling.mappingparser.exceptions;

import java.nio.file.Path;

public class MappingFileIsMalformedException extends MappingFileException {

  public MappingFileIsMalformedException(Path mappingFile, Throwable cause) {
    super(getErrorMessage(mappingFile), cause);
  }

  private static String getErrorMessage(Path mappingFile) {
    return "Mapping file '" + mappingFile.toAbsolutePath() + "' is malformed";
  }

  @Override
  public int getExitCode() {
    return 6;
  }
}
