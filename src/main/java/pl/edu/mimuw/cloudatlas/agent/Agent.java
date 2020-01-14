package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class Agent {

    public static void main(String[] args) {
        AgentConfig agentConfig = new AgentConfig();

        agentConfig.runModulesAsThreads();

        EventBus eventBus = new EventBus(agentConfig.getExecutors());
        agentConfig.runRegistry(eventBus);
        agentConfig.startNonModuleThreads(eventBus);

        String zonePath = System.getProperty("zone_path");
        String selectionStrategy = System.getProperty("Gossip.zone_strategy");
        Long queryPeriod = Long.getLong("query_period");
        Long gossipPeriod = Long.getLong("gossip_period");
        Long freshnessPeriod = Long.getLong("freshness_period");

        HierarchyConfig hierarchyConfig = new HierarchyConfig(eventBus, zonePath, selectionStrategy);
        hierarchyConfig.startQueries(queryPeriod);
        hierarchyConfig.startGossip(gossipPeriod, zonePath);
        hierarchyConfig.startCleaningGossips(freshnessPeriod);
    }
}
