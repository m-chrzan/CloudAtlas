# CloudAtlas

## Running

### API Agent

    # start rmiregistry
    ./scripts/registry
    # start agent to bind API
    ./gradlew runAgent

### Client

    ./gradlew runClient

Relies on a running agent.

Exposes a web application on `localhost:8080`.

### Fetcher

    ./gradlew runFetcher

Sends system information to an agent.

### Interpreter

    ./gradlew runInterpreter
    # to suppress Gradle's output
    ./gradlew runInterpreter --quiet

Reads queries from standard input and outputs results to standard output, using
a hard-coded test hierarchy.

### Tests

    ./gradlew test
    # test individual components
    ./gradlew test --tests AgentTest
    ./gradlew test --tests InterpreterTests.fileTest13

Generates an HTML test report at `build/reports/tests/test/index.html`.
