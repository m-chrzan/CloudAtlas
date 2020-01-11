package pl.edu.mimuw.cloudatlas.agent;

public class Agent {

    public static void main(String[] args) {
        AgentConfig agentConfig = new AgentConfig();

        agentConfig.runModulesAsThreads();
        agentConfig.runRegistry();

        HierarchyConfig hierarchyConfig = new HierarchyConfig(agentConfig.getEventBus());
        hierarchyConfig.initZones();
        // TODO: make query period confiurable with config file and from tests
        Long queryPeriod = Long.getLong("query_period");
        hierarchyConfig.startQueries(queryPeriod);
        Long gossipPeriod = Long.getLong("gossip_period");
        hierarchyConfig.startGossip(gossipPeriod);
    }
}
