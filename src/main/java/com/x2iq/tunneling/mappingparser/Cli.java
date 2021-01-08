package com.x2iq.tunneling.mappingparser;

import com.x2iq.tunneling.mappingparser.exceptions.MappingFileException;
import com.x2iq.tunneling.mappingparser.model.Mapping;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

@CommandLine.Command(name = "tunneling-mapping-parser")  // TODO name from pom.artifactId
public class Cli implements Callable<Integer> {

  @CommandLine.ArgGroup(multiplicity = "1")
  MainOptions mainOptions;

  @CommandLine.Option(names = {"-p", "--property"})
  Foo propertyName = null;

  @CommandLine.Option(names = {"-f", "--file"}, defaultValue = "mapping.yml", required = true, showDefaultValue = ALWAYS)
  Path mappingFile;

  public static void main(String[] args) {
    System.exit(new Cli().execute(args));
  }

  public int execute(String[] args) {
    return new CommandLine(this)
        .setExecutionExceptionHandler(getExceptionHandler())
        .execute(args);
  }

  @Override
  public Integer call() {
    MappingService mappingService = new MappingService(mappingFile);

    if (mainOptions.printNumberOfMappings) {
      System.out.println(mappingService.numberOfMappings());
    } else if (mainOptions.printSeqOfMappings) {
      System.out.println(mappingService.seqOfMappings());
    } else {
      System.out.println(getWantedMapping(mappingService));
    }

    return 0;
  }

  // TODO move to MappingService?
  private String getWantedMapping(MappingService mappingService) {
    Mapping m = mappingService.getMapping(mainOptions.mappingIndex);

    if (propertyName == null) {
      return "[" + m.acceptPort + " -> " + m.routeTo.ip + ":" + m.routeTo.port +"] (" + m.description + ")";
    }

    switch (propertyName) {
      case description:
        return m.description;
      case acceptPort:
        return String.valueOf(m.acceptPort);
      case targetIp:
      case routeToIp:
        return m.routeTo.ip;
      case targetPort:
      case routeToPort:
        return String.valueOf(m.routeTo.port);
      default:
        throw new RuntimeException("This should never be possible");  // TODO better message
    }
  }

  // TODO naming
  private CommandLine.IExecutionExceptionHandler getExceptionHandler() {
    return (Exception e, CommandLine commandLine, CommandLine.ParseResult parseResult) -> {

      System.err.println(e.getMessage());
      if (e.getCause() != null) {
        System.err.println(e.getCause().getMessage());
      }
      // e.printStackTrace();  // TODO only with debug flag

      if (e instanceof MappingFileException) {
        return ((MappingFileException) e).getExitCode();
      }

      if (e instanceof IllegalArgumentException) {
        return CommandLine.ExitCode.USAGE;
      }

      return CommandLine.ExitCode.SOFTWARE;
    };
  }

  static class MainOptions {
    @CommandLine.Option(names = {"-n", "--number-of-mappings"}, required = true)
    boolean printNumberOfMappings;

    @CommandLine.Option(names = {"-s", "--seq-of-mappings"}, required = true)
    boolean printSeqOfMappings;

    @CommandLine.Option(names = {"-m", "--get-mapping"}, required = true)
    int mappingIndex;
  }

  enum Foo {
    description,
    acceptPort,
    targetIp, routeToIp,
    targetPort, routeToPort
  }
}
