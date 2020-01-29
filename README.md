# CloudAtlas

## Running

The agent, fetcher, and interpreter take an optional `-Dhostname=<agent's external hostname`
argument, which defaults to `localhost`.

### Query Signer
    # start rmiregistry
    ./scripts/registry
    # generate keys
    ./scripts/generate_keys.sh
    # run Query Signer
    ./gradlew runQuerySigner
    
### API Agent

    # start rmiregistry
    ./scripts/registry
    # start agent to bind API
    ./gradlew runAgent

Relies on keys generated during query signer setup.

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

## Demo setup

The `scripts/` directory contains a bundle of scripts that can be helpful when
setting up a simple hierarchy.

### Envrionment Variables

The scripts assume that there are 5 machines with hostnames `rainbow01`, ...,
`rainbow05`. Each machine should have the following environment variables
available:

* `NODE_NUMBER`: ranging from `01` to `05`
* `ZONE`: a level-1 zone name (e.g. `uw`)

For example, if `rainbow01` corresponds to node `/uw/rainbow01` in the
hierarchy, it should have `NODE_NUMBER=01` and `ZONE=uw`.

### RMI registry

Each node should have an RMI registry running, started with

    ./scripts/registry

### Query signer

The scripts assume that the query signer runs on `rainbow01`. Before starting
it, run

    ./scripts/generate_keys.sh

If `rainbow01` has ssh access to the other machines, the public key can then be
distributed with

    ./scripts/copyKey.sh

Finally, start the query signer on `rainbow01` with

    ./scripts/runQuerySigner.sh

### Agent and fetcher

On each machine, the agent and fetcher components should be started with

    ./scripts/runAgent.sh
    ./scripts/runFetcher.sh

### Client (optional)

A client connected to the local agent can be optionally started with

    ./scripts/runClient.sh
