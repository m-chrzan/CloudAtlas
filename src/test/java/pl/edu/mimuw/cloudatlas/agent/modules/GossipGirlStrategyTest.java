package pl.edu.mimuw.cloudatlas.agent.modules;

import org.junit.Test;
import pl.edu.mimuw.cloudatlas.agent.modules.GossipGirlStrategies;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.util.HashMap;

public class GossipGirlStrategyTest {

    @Test
    public void seeHowTheyWork() {
        PathName fullPath = new PathName("/pl/mazowieckie/warszawa/ochota/mimuw");
        GossipGirlStrategies gossipGirlStrategies = new GossipGirlStrategies(fullPath);
        int loopCount = 1000;
        PathName selectedPath;
        HashMap<PathName, Integer> freqs = new HashMap<>();
        freqs.put(new PathName("/pl/mazowieckie/warszawa/ochota/mimuw"), 0);
        freqs.put(new PathName("/pl/mazowieckie/warszawa/ochota"), 0);
        freqs.put(new PathName("/pl/mazowieckie/warszawa"), 0);
        freqs.put(new PathName("/pl/mazowieckie"), 0);
        freqs.put(new PathName("/pl"), 0);

        for (int i = 0; i < loopCount; i++) {
            selectedPath =
                    gossipGirlStrategies.selectStrategy(GossipGirlStrategies.ZoneSelectionStrategy.ROUND_ROBIN_EXP_FREQ);
            freqs.put(selectedPath, freqs.get(selectedPath) + 1);
            System.out.println(selectedPath);
        }
    }
}
