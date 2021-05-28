# tunneling-mapping-parser

The tunneling-mapping-parser is a small CLI tool that can parse a YAML file representing mappings for tunneling.

## General overview

This tool can parse mapping file for tunneling of connections.
Below you see a sample mapping file that can be parsed by this tool:

```yaml
mappings:
  - description: forward psql
    acceptPort: 1234
    routeTo:
      ip: 1.3.3.7
      port: 5432
  - description: forward https
    acceptPort: 443
    routeTo:
      ip: 4.8.15.16
      port: 443
```

In the file, we have two tunneling rules.
With this tool, you have different operations you can archive.

## mapping file

The structure of the mapping file is summarized here:

```yaml
mapping:
  - description: <string>
    acceptPort: <int>
    routeTo:
      ip: <string>
      port: <int>
```

You can have as many rules as you want in the `mapping` array.
All fields are mandatory, and you can't leave out any of the fields.
It's also not possible to add any other fields.
If your file is multi-document YAML, then the only first document is read.

### `description`

The `description` is a string.
You can put anything you want to this string.
The purpose of this field is to describe the rule.
There is no validation or any additional logic based on this field.

### `acceptPort`

The `acceptPort` is an integer.
Valid values are between `1` and `65535`.
However, there is no validation, and any (32-bit) integer will work.
This value represents the port on the source server.
The `acceptPort` value should be unique because it's impossible to assign the same port twice.
The parser doesn't check the uniqueness of values inside of the file.

### `routeTo`

The `routeTo` is an object that contains coordinates of the target server.

### `routeTo.ip`

The `ip` is a string.
Valid content is the IPv4 address.
The value represents the IP address of the target server.
It's not possible to use the hostname in this field (and currently, there is no way to use a hostname for the target server at all).
There is no check if the value is a valid IP address.

### `routeTo.port`

The `port` is int.
Valid values are between `1` and `65535`.
Same as for `acceptPort`, there is no validation if values are correct.
This value represents the port on the target server.

## Usage

This is general usage:

```text
Usage: tunneling-mapping-parser (-n | -s | (-m=<mappingIndex> [-p=<propertyName>])) [-f=<mappingFile>]
  -n, --number-of-mappings
  -s, --seq-of-mappings
  -m, --get-mapping=<mappingIndex>
  -p, --property=<propertyName>
  -f, --file=<mappingFile>
```

You need to pass exactly one main argument.
The main arguments are `-n`, `-s` and `-m`.

Detailed documentation for each argument is below.
All examples below are considering to have mapping file with follows content:

```yaml
mappings:
  - description: forward psql
    acceptPort: 1234
    routeTo:
      ip: 1.3.3.7
      port: 5432
  - description: forward https
    acceptPort: 443
    routeTo:
      ip: 4.8.15.16
      port: 443
```

### `-n, --number-of-mappings`

Prints amount of mapping rules.

Examples:

```shell
tunneling-mapping-parser -n
# output:
# 2
```

```shell
tunneling-mapping-parser --number-of-mappings
# output:
# 2
```

### `-s, --seq-of-mappings`

Prints sequence for the number of rules in the mapping file.
The primary purpose is to iterate rules in the shell/bash loop.
The output is the same as the `seq 0 X-1` command (where X is number-of-mappings).

Examples:

```shell
tunneling-mapping-parser -s
# output:
# 0 1
```

```shell
tunneling-mapping-parser --seq-of-mappings
# output:
# 0 1
```

An example of usage in a bash script:

```shell
#!/bin/bash

for i in `tunneling-mapping-parser -s`; do
  echo `tunneling-mapping-parser -m ${i}`
done

# output:
# [1234 -> 1.3.3.7:5432] (forward psql)
# [443 -> 4.8.15.16:443] (forward https)
```

### `-m, --get-mapping <mappingIndex>`

Prints human-readable summary of wanted mapping.
You need to pass the index and not the number of a rule you want.
The value `0` returns the first rule, the value `1` returns the second rule, and so on.
You can also request a single mapping parameter (machine-readable) using an additional `-p` parameter.

Examples:

```shell
tunneling-mapping-parser -m 0
# output:
# [1234 -> 1.3.3.7:5432] (forward psql)
```

```shell
tunneling-mapping-parser --get-mapping 1
# output:
# [443 -> 4.8.15.16:443] (forward https)
```

### `-p, --property <propertyName>`

This argument can only be used together with `-m`.
Possible values for `propertyName` are:

- `description` - prints description of wanted rule
- `acceptPort` - prints post that is open on host
- `targetIp` - Same as`routeToIp`. Prints IP where requests are forwarded to.
- `targetPort` - Same as `routeToPort`. Prints port where requests are forwarded to.

Examples:

```shell
tunneling-mapping-parser -m 0 --property description
# output:
# forward psql
```

```shell
tunneling-mapping-parser -m 0 -p acceptPort
# output:
# 1234
```

```shell
tunneling-mapping-parser -m 0 -p targetIp
# output:
# 1.3.3.7
```

```shell
tunneling-mapping-parser -m 0 -p routeToIp
# output:
# 1.3.3.7
```

```shell
tunneling-mapping-parser -m 0 -p targetPort
# output:
# 5432
```

```shell
tunneling-mapping-parser -m 0 -p routeToPort
# output:
# 5432
```

### `-f, --file <mappingFile>`

Specifies alternative mapping file.
This argument is optional.
If this argument is omitted, then `mapping.yml` is used.

Examples:

```shell
tunneling-mapping-parser -m 0 -f mapping.yml
# output:
# [1234 -> 1.3.3.7:5432] (forward psql)
```

```shell
tunneling-mapping-parser -m 0 --file /tmp/mapping.yml
# output:
# [1337 -> 4.8.15.16:2345] (some other mapping rule)
```

## Building

Simple build.

```bash
mvn clean package
```

Native build.

```bash
mvn clean package -Pnative
```
