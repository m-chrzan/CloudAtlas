# CloudAtlas

## Running

The agent, fetcher, and interpreter take optional `-Dflagname=flagvalue`
argument. Otherwise those values are set to default ones, as described below.

### Query Signer
    # start rmiregistry
    ./scripts/registry
    # generate keys
    ./scripts/generate_keys.sh
    # run Query Signer
    ./gradlew runQuerySigner
    
Relies on generation of public and private keys by scripts/generate_keys.sh.

Flags:

* java.rmi.server.hostname - RMI registry hostname, default: localhost
* querySignerHostname - query signer public RMI API hostname, default: localhost
* publicKeyFilename - path to public key file, relative to root of project, default: build/tmp/query_signer.pub
* privateKeyFilename - path to private key file, relative to root of project, default: build/tmp/query_signer
    
### Agent

    # start rmiregistry
    ./scripts/registry
    # start agent to bind API
    ./gradlew runAgent

Relies on keys generated during query signer setup.

Flags:

* java.rmi.server.hostname - RMI registry hostname, default: localhost
* freshnessPeriod - data refresh period, default: 60 * 1000
* queryPeriod - query rerun period, default: 5 * 1000
* gossipPeriod - gossiping period, default: 5 * 1000
* hostname - public UDP server hostname, default: hostname
* port - public UDP port, default: 5999
* timeout - UDP server timeout, default: 5 * 1000
* bufsize - UDP message buffer size, default: 512
* zoneSelectionStrategy - zone selection strategy for gossiping, default: RandomUniform
    available options: RoundRobinExp, RoundRobinUniform, RandomExp, RandomUniform
* zonePath - zone pathname of agent, default: /uw/violet07
* publicKeyFilename - path to public key file, relative to root of project, default: build/tmp/query_signer.pub

### Client

    ./gradlew runClient

Exposes a web application on `localhost:8082`.

Relies on a running agent with fetcher.

Flags:

* hostname - agent RMI API hostname, default: localhost
* zonePath - zone pathname of agent, default: /uw/violet07
* querySignerHostname - query signer RMI API hostname, default: localhost

### Fetcher

    ./gradlew runFetcher

Sends system information to an agent.

Relies on a running agent.

Flags:

* hostname - agent RMI API hostname, default: localhost
* zonePath - zone pathname of agent, default: /uw/violet07
* ownAddr - public IP address or domain name of agent/fetcher machine
* fallbackContacts - initialize fallback contacts, default: {}, example:

    -DfallbackContacts=\{\"/uw/violet07\":[192,168,0,11]}

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

The scripts assume that the query signer runs on `rainbow01`. Before first
starting it, run

    ./scripts/generate_keys.sh

to generate keys.

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
