package com.x2iq.tunneling.mappingparser;

import com.x2iq.tunneling.mappingparser.model.Mapping;

import java.nio.file.Path;

public class MappingService {

  private final Mapping[] mappings;

  public MappingService(Path mappingFile) {
    mappings = new MappingParser(mappingFile).parseFile();
  }

  public int numberOfMappings() {
    return mappings.length;
  }

  public Mapping getMapping(int index) {
    if (index < 0) {
      throw new IllegalArgumentException("Mapping number can not be negative - current value: " + index);
    }

    if (index >= mappings.length) {
      throw new IllegalArgumentException("You are trying to read mapping no. " + (index + 1) + " (index " + index + "). " +
          "But only " + mappings.length + " mappings are present.");
    }

    return mappings[index];
  }

  public String seqForMappings() {
    StringBuilder builder = new StringBuilder("0");

    for (int i = 1; i < numberOfMappings(); i++) {
      builder.append(" ");
      builder.append(i);
    }

    return builder.toString();
  }
}
