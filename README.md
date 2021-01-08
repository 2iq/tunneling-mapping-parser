# tunneling-mapping-parser

This is a small CLI tool that can parse yml file which represents mappings for tunneling.
This is a component of _tunneling_ product.

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
Main arguments are `-n`, `-s` and `-m`.

Each argument is documented in detail below.
All examples below are for example file above.

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
This is mainly for iterating rules in the shell/bash loop.
This is the same output as `seq 0 X-1` command (where X is number-of-mappings).

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

This is an example of usage in a bash script:

```shell
#!/bin/bash

for i in `tunneling-mapping-parser -s`; do
  echo `tunneling-mapping-parser -m ${i}`
done

# output:
# forward psql (1234 -> 1.3.3.7:5432)
# forward https (443 -> 4.8.15.16:443)
```

### `-m, --get-mapping <mappingIndex>`

Prints human-readable summary of wanted mapping.
You need to pass the index and not the number of a rule you want.
For example above the value `0` would return the first rule and the value `1` would return the second rule.
You can also request a single mapping parameter (machine-readable) by using an additional `-p` parameter.

Examples:

```shell
tunneling-mapping-parser -m 0
# output:
# forward psql (1234 -> 1.3.3.7:5432)
```

```shell
tunneling-mapping-parser --get-mapping 1
# output:
# forward https (443 -> 4.8.15.16:443)
```

### `-p, --property <propertyName>`

This argument can only be used along with `-m`.
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

Mapping file to be parsed.
This argument is optional.
If this argument is omitted then `mapping.yml` is used.

Examples:

```shell
tunneling-mapping-parser -m 0 -f mapping.yml
# output:
# forward psql (1234 -> 1.3.3.7:5432)
```

```shell
tunneling-mapping-parser -m 0 --file /tmp/mapping.yml
# output:
# some other mapping rule (1337 -> 4.8.15.16:2342)
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
