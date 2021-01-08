package com.x2iq.tunneling.mappingparser

import io.github.joke.spockoutputcapture.CapturedOutput
import io.github.joke.spockoutputcapture.OutputCapture
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.Files.setPosixFilePermissions
import static java.nio.file.attribute.PosixFilePermissions.fromString

class MappingParserTest extends Specification {

  @OutputCapture
  private CapturedOutput console

  def 'should work with valid arguments'(String cliArgs, String res) {
    setup:
    createDefaultMappingFile('''\
        mappings:
          - description: first description  # some comment
            acceptPort: 1
            routeTo:
              ip: 1.1.1.1
              port: 1
          - description: 'second description'
            acceptPort: 20
            routeTo:
              ip: 2.2.2.2
              port: 21
    ''')

    when:
    int exitCode = runWithArgs(cliArgs)

    then:
    exitCode == 0
    console.lines == [res]

    where:
    cliArgs                                  || res
    '-s'                                     || '0 1'
    '--seq-for-mappings'                     || '0 1'
    '-n'                                     || '2'
    '--number-of-mappings'                   || '2'
    '-m 0'                                   || '[1 -> 1.1.1.1:1] (first description)'
    '-m0'                                    || '[1 -> 1.1.1.1:1] (first description)'
    '--get-mapping 0'                        || '[1 -> 1.1.1.1:1] (first description)'
    '-m 1'                                   || '[20 -> 2.2.2.2:21] (second description)'
    '--get-mapping 1'                        || '[20 -> 2.2.2.2:21] (second description)'
    '-m=1'                                   || '[20 -> 2.2.2.2:21] (second description)'
    '--get-mapping=1'                        || '[20 -> 2.2.2.2:21] (second description)'

    '-m 1 -p description'                    || 'second description'
    '-m 1 -p acceptPort'                     || '20'
    '-m 1 -p targetIp'                       || '2.2.2.2'
    '-m 1 -p targetPort'                     || '21'
    '-m 1 -p routeToIp'                      || '2.2.2.2'
    '-m 1 -p routeToPort'                    || '21'

    '-m 1 --property description'            || 'second description'
    '-m 1 --property acceptPort'             || '20'
    '-m 1 --property targetIp'               || '2.2.2.2'
    '-m 1 --property targetPort'             || '21'
    '-m 1 --property routeToIp'              || '2.2.2.2'
    '-m 1 --property routeToPort'            || '21'

    '--get-mapping 1 -p description'         || 'second description'
    '--get-mapping 1 -p acceptPort'          || '20'
    '--get-mapping 1 -p targetIp'            || '2.2.2.2'
    '--get-mapping 1 -p targetPort'          || '21'
    '--get-mapping 1 -p routeToIp'           || '2.2.2.2'
    '--get-mapping 1 -p routeToPort'         || '21'

    '--get-mapping 1 --property description' || 'second description'
    '--get-mapping 1 --property acceptPort'  || '20'
    '--get-mapping 1 --property targetIp'    || '2.2.2.2'
    '--get-mapping 1 --property targetPort'  || '21'
    '--get-mapping 1 --property routeToIp'   || '2.2.2.2'
    '--get-mapping 1 --property routeToPort' || '21'
  }

  def 'should fail with invalid arguments'(String cliArgs) {
    setup:
    createDefaultMappingFile('''\
        mappings:
          - description: valid mapping file
            acceptPort: 1
            routeTo:
              ip: 1.1.1.1
              port: 1
    ''')

    when:
    int exitCode = runWithArgs(cliArgs)

    then:
    exitCode == 2
    console.lines.size() > 1

    where:
    cliArgs         | _
    ''              | _
    '-s -n'         | _
    '-s -m 1'       | _
    '-m'            | _
    '--get-mapping' | _
  }

  def 'should check the number of passed property'(String cliArgs, int expectedExitCode, List<String> expectedConsoleOutput) {
    setup:
    createDefaultMappingFile('''\
        mappings:
          - description: forward rule Nr 1
            acceptPort: 1
            routeTo:
              ip: 1.1.1.1
              port: 1
          - description: forward rule Nr 2
            acceptPort: 2
            routeTo:
              ip: 2.2.2.2
              port: 2
    ''')

    when:
    int exitCode = runWithArgs(cliArgs)

    then:
    exitCode == expectedExitCode
    console.lines == expectedConsoleOutput

    where:
    cliArgs || expectedExitCode | expectedConsoleOutput
    '-m -2' || 2                | ['Mapping number can not be negative - current value: -2']
    '-m -1' || 2                | ['Mapping number can not be negative - current value: -1']
    '-m 0'  || 0                | ['[1 -> 1.1.1.1:1] (forward rule Nr 1)']
    '-m 1'  || 0                | ['[2 -> 2.2.2.2:2] (forward rule Nr 2)']
    '-m 2'  || 2                | ['You are trying to read mapping no. 3 (index 2). But only 2 mappings are present.']
    '-m 3'  || 2                | ['You are trying to read mapping no. 4 (index 3). But only 2 mappings are present.']
  }

  def 'should read from passed file'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings:
          - description: valid mapping file
            acceptPort: 1234
            routeTo:
              ip: 1.2.3.4
              port: 5678
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 0
    console.lines == ['[1234 -> 1.2.3.4:5678] (valid mapping file)']
  }

  def 'should read from first document in multidocument yaml file'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings:
          - description: yaml document 1
            acceptPort: 1234
            routeTo:
              ip: 1.2.3.4
              port: 5678
        ---
        mappings:
          - description: yaml document 2
            acceptPort: 4321
            routeTo:
              ip: 4.3.2.1
              port: 8765
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 0
    console.lines == ['[1234 -> 1.2.3.4:5678] (yaml document 1)']
  }

  def 'should fail if passed mapping file did not exists'() {
    given:
    String currentPath = Paths.get('', 'not-existent-file.yml').toAbsolutePath()

    when:
    int exitCode = runWithArgs('-f not-existent-file.yml -m 0')

    then:
    exitCode == 3
    console.lines.size() == 1
    console.lines[0] == "Mapping file '${currentPath}' not found"
  }

  def 'should fail if passed mapping file is empty'() {
    setup:
    Path mappingFile = createTempMappingFile('')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 7
    console.lines.size() == 1
    console.lines[0] == "Mapping file '${mappingFile}' is empty"
  }

  @Ignore('docker that runs under root user can read the file even without permission')
  def 'should fail if mapping file has not enough permission to be read'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings:
          - description: not readable mapping file
            acceptPort: 1234
            routeTo:
              ip: 1.2.3.4
              port: 5678
    ''')

    setPosixFilePermissions(mappingFile, fromString("---------"))

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 4
    console.lines[0] == "You don't have read permission for '${mappingFile}' mapping file"
  }

  def 'should fail if mapping file is directory'() {
    setup:
    Path mappingFile = Files.createTempDirectory('')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 5
    console.lines[0] == "Given mapping file '${mappingFile}' is directory"
  }

  def 'should fail if mapping file has additional properties'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings:
          - description: with additional property
            acceptPort: 1234
            routeTo:
              ip: 1.2.3.4
              port: 5678
            additional: foo
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 6
    console.lines.size() == 2
    console.lines[0] == "Mapping file '${mappingFile}' is malformed"
  }

  def 'should fail if mappings array is object'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings:
          description: this is not array element
          acceptPort: 1234
          routeTo:
            ip: 1.2.3.4
            port: 5678
          additional: foo
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 6
    console.lines.size() == 2
    console.lines[0] == "Mapping file '${mappingFile}' is malformed"
  }

  def 'should fail if mappings array is empty'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings: []
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 8
    console.lines.size() == 1
    console.lines[0] == "Mapping file '${mappingFile}' didn't contain any mapping"
  }

  def 'should fail if mappings array is null'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings: ~
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 6
    console.lines.size() == 2
    console.lines[0] == "Mapping file '${mappingFile}' is malformed"
  }

  def 'should fail if description is missing'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings:
          acceptPort: 1234
          routeTo:
            ip: 1.2.3.4
            port: 5678
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 6
    console.lines.size() == 2
    console.lines[0] == "Mapping file '${mappingFile}' is malformed"
  }

  def 'should fail if acceptPort is missing'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings:
          description: acceptPort is missing
          routeTo:
            ip: 1.2.3.4
            port: 5678
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 6
    console.lines.size() == 2
    console.lines[0] == "Mapping file '${mappingFile}' is malformed"
  }

  def 'should fail if routeTo is missing'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings:
          description: routeTo is missing
          acceptPort: 1234
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 6
    console.lines.size() == 2
    console.lines[0] == "Mapping file '${mappingFile}' is malformed"
  }

  def 'should fail if routeTo.ip is missing'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings:
          description: routeTo.ip is missing
          acceptPort: 1234
          routeTo:
            port: 5678
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 6
    console.lines.size() == 2
    console.lines[0] == "Mapping file '${mappingFile}' is malformed"
  }

  def 'should fail if routeTo.port is missing'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings:
          description: routeTo.port is missing
          acceptPort: 1234
          routeTo:
            ip: 1.2.3.4
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 6
    console.lines.size() == 2
    console.lines[0] == "Mapping file '${mappingFile}' is malformed"
  }

  def 'should fail if routeTo is string'() {
    setup:
    Path mappingFile = createTempMappingFile('''\
        mappings:
          description: is missing
          acceptPort: 1234
          routeTo: some string
    ''')

    when:
    int exitCode = runWithArgs("-f ${mappingFile} -m 0")

    then:
    exitCode == 6
    console.lines.size() == 2
    console.lines[0] == "Mapping file '${mappingFile}' is malformed"
  }

  private Path createDefaultMappingFile(String content) {
    File defaultMappingFile = new File('mapping.yml')
    defaultMappingFile.createNewFile()
    defaultMappingFile.deleteOnExit()

    fillMappingFile(defaultMappingFile, content)
  }

  private Path createTempMappingFile(String content) {
    File mappingFile = File.createTempFile('mapping', '.yml')
    mappingFile.deleteOnExit()

    fillMappingFile(mappingFile, content)
  }

  private Path fillMappingFile(File mappingFile, String content) {
    mappingFile.text = content.stripIndent()

    return mappingFile.toPath().toAbsolutePath()
  }

  private int runWithArgs(String cliArgs) {
    new Cli().execute(asStringArray(cliArgs))
  }

  private String[] asStringArray(String cliArgs) {
    cliArgs.split(' ')
  }
}

// TODOs:
// - test native-image
