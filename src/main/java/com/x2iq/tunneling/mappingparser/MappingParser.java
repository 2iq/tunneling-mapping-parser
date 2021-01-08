package com.x2iq.tunneling.mappingparser;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.x2iq.tunneling.mappingparser.exceptions.MappingFileIsDirectoryException;
import com.x2iq.tunneling.mappingparser.exceptions.MappingFileIsEmptyException;
import com.x2iq.tunneling.mappingparser.exceptions.MappingFileIsMalformedException;
import com.x2iq.tunneling.mappingparser.exceptions.MappingFileIsNotReadableException;
import com.x2iq.tunneling.mappingparser.exceptions.MappingFileNotFoundException;
import com.x2iq.tunneling.mappingparser.exceptions.NoMappingInMappingFileException;
import com.x2iq.tunneling.mappingparser.model.Mapping;
import com.x2iq.tunneling.mappingparser.model.Mappings;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class MappingParser {

  private final Path mappingFile;

  public MappingParser(Path mappingFile) {
    this.mappingFile = mappingFile;
  }

  public Mapping[] parseFile() {
    validateFile();
    Mappings m = serializeMappingFile();
    validateMappingResult(m);

    return m.mappings;
  }

  private void validateFile() {
    if (!Files.exists(mappingFile)) {  // TODO Files.notExists(mappingFile)?
      throw new MappingFileNotFoundException(mappingFile);
    }

    if (!Files.isReadable(mappingFile)) {
      throw new MappingFileIsNotReadableException(mappingFile);
    }

    if (Files.isDirectory(mappingFile)) {
      throw new MappingFileIsDirectoryException(mappingFile);
    }
  }

  private Mappings serializeMappingFile() {
    YamlReader yaml = new YamlReader(new InputStreamReader(yamlMappings()));

    try {
      return yaml.read(Mappings.class);
    } catch (YamlException e) {
      throw new MappingFileIsMalformedException(mappingFile, e);
    }
  }

  private InputStream yamlMappings() {
    try {
      return Files.newInputStream(mappingFile);
    } catch (IOException e) {
      throw new RuntimeException("Error reading mapping file '" + mappingFile.toAbsolutePath() + "'", e);
    }
  }

  private void validateMappingResult(Mappings m) {
    if (m == null) {
      throw new MappingFileIsEmptyException(mappingFile);
    }

//      if (m.mappings == null || m.mappings.length == 0) {  // TODO necessary?
    if (m.mappings.length == 0) {
      throw new NoMappingInMappingFileException(mappingFile);
    }
  }
}
