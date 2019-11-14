# CloudAtlas

## Running

### API Agent

    # start rmiregistry
    ./scripts/registry
    # start agent to bind API
    ./gradlew runAgent

### Client

    ./gradlew runClient

### Tests

    ./gradlew test
    # test individual components
    ./gradlew test --tests AgentTest
    ./gradlew test --tests InterpreterTests.fileTest13

Generates an HTML test report at `build/reports/tests/test/index.html`.
