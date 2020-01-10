package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.util.ArrayList;

/**
 round robin with the same frequency for all levels,
 round robin with the frequency exponentially decreasing with level,
 random with the same selection probability for all levels,
 random with the selection probability decreasing exponentially with level.
 */

public class GossipGirlStrategies {

    public enum ZoneSelectionStrategy {
        ROUND_ROBIN_SAME_FREQ,
        ROUND_ROBIN_EXP_FREQ,
        RANDOM_UNFIORM,
        RANDOM_DECR_EXP
    }

    public PathName selectStrategy(PathName fullPath, ZoneSelectionStrategy selectionStrategy) {
        PathName selectedPath;
        ArrayList<PathName>
        int fullPathLength = fullPath.getComponents().size();

        switch(selectionStrategy) {
            case (ZoneSelectionStrategy.ROUND_ROBIN_SAME_FREQ):

                break;
            case (ZoneSelectionStrategy.ROUND_ROBIN_EXP_FREQ):

                break;
            case (ZoneSelectionStrategy.RANDOM_UNFIORM):
                ArrayList<Pair<PathName, Double>> zoneProbabilities = new ArrayList<>(fullPathLength);
                EnumeratedDistribution dist = new EnumeratedDistribution();
                for (int i = 1; i < fullPathLength; i++) {
                    zoneProbabilities.add(fullPath.);
                }
                break;
            case (ZoneSelectionStrategy.RANDOM_DECR_EXP):
                break;
            default:
                throw new UnsupportedOperationException("Such strategy doesn't exist");
        }

        return selectedPath;
    }



}
