package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.messages.UpdateAttributesMessage;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class Agent {

    private static void addZoneAndChildren(ZMI zmi, PathName pathName, EventBus eventBus) {
        try {
            UpdateAttributesMessage message = new UpdateAttributesMessage("", 0, pathName.toString(), zmi.getAttributes());
            eventBus.addMessage(message);
            for (ZMI son : zmi.getSons()) {
                addZoneAndChildren(son, pathName.levelDown(son.getAttributes().getOrNull("name").toString()), eventBus);
            }
        } catch (Exception e) {
            System.out.println("ERROR: failed to add zone");
        }
    }

    public static void initZones(EventBus eventBus) {
        try {
            ZMI root = Main.createTestHierarchy2();
            addZoneAndChildren(root, new PathName(""), eventBus);
            System.out.println("Initialized with test hierarchy");
        } catch (Exception e) {
            System.out.println("ERROR: failed to create test hierarchy");
        }
    }

    public static void main(String[] args) {
        AgentConfig agentConfig = new AgentConfig();

        agentConfig.runModulesAsThreads();

        EventBus eventBus = new EventBus(agentConfig.getExecutors());
        agentConfig.runRegistry(eventBus);
        agentConfig.startNonModuleThreads(eventBus);

        // initZones(eventBus);

        // TODO: make query period confiurable with config file and from tests

        String zonePath = System.getProperty("zone_path");
        String selectionStrategy = System.getProperty("Gossip.zone_strategy");
        Long queryPeriod = Long.getLong("query_period");
        Long gossipPeriod = Long.getLong("gossip_period");
        Long freshnessPeriod = Long.getLong("freshness_period");

        HierarchyConfig hierarchyConfig = new HierarchyConfig(eventBus, zonePath, selectionStrategy);
        hierarchyConfig.startQueries(queryPeriod);
        hierarchyConfig.startGossip(gossipPeriod, zonePath);
        // TODO: should this be different than ZMI freshness period?
        hierarchyConfig.startCleaningGossips(freshnessPeriod);
    }
}
