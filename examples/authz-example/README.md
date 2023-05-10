[![slack](https://img.shields.io/badge/slack-Aserto%20Community-brightgreen)](https://asertocommunity.slack.com)


## 0. prerequisites

Ensure you are in the `examples/authz-example` directory.

```bash
cd examples/authz-example
```

## 1. building the examples

To build the example package, execute the following commands:

```bash
mvn clean package
```

## 2. setup environment

## 2.1 using [topaz](https://topaz.sh)

### 2.1.1 install and configure [topaz](https://topaz.sh)

* Install [topaz](https://github.com/aserto-dev/topaz#installation)
* Configure topaz to use the `todo` policy

```bash
topaz configure -d -s -r ghcr.io/aserto-policies/policy-todo:v2 todo
```

* Download topaz directory data

```bash
topaz stop 
wget https://raw.githubusercontent.com/aserto-dev/topaz/main/pkg/testing/assets/eds-citadel.db -O ~/.config/topaz/db/directory.db
```

* Start topaz

```bash
topaz start

```

* Validate if topaz is running

```bash
topaz status
```

### 2.1.2 copy the `examples/assets/.env.topaz-authorizer.example` to the `examples` directory

```bash
cp assets/.env.topaz-authorizer.example .env
```

## 3. Running the example

To run the examples, execute:

```bash
java -jar target/examples-1.0.0-shaded.jar
```