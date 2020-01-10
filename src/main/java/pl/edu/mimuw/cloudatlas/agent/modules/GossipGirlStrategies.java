package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.util.*;

/**
 round robin with the same frequency for all levels,
 round robin with the frequency exponentially decreasing with level,
 random with the same selection probability for all levels,
 random with the selection probability decreasing exponentially with level.
 */

public class GossipGirlStrategies {
    private int roundRobinSameIt;
    private ArrayList<Pair<String, Integer>> roundRobinExpFreqs;
    private List<String> fullPathComponents;
    private int fullPathLength;
    private EnumeratedDistribution<String> expDist;
    private EnumeratedDistribution<String> uniformDist;

    public GossipGirlStrategies(PathName fullPath) {
        fullPathComponents = fullPath.getComponents();
        fullPathLength = fullPath.getComponents().size();
        initUniformZoneProbabilities();
        initExpZoneProbabilities();
        roundRobinSameIt = 0;
        initRoundRobinExpFreqs();
    }

    private void initExpZoneProbabilities() {
        ArrayList<Pair<String, Double>> zoneProbabilities;
        zoneProbabilities = new ArrayList<>(fullPathLength);

        // TODO check if we decrease in good direction
        for (int i = 0; i < fullPathLength; i++) {
            Pair<String, Double> probPair = new Pair<String, Double>(fullPathComponents.get(i), Math.exp((double) i+1));
            zoneProbabilities.add(probPair);
        }

        uniformDist = new EnumeratedDistribution<String>(zoneProbabilities);
    }

    private void initUniformZoneProbabilities() {
        ArrayList<Pair<String, Double>> zoneProbabilities;
        zoneProbabilities = new ArrayList<>(fullPathLength);

        // TODO good direction
        for (int i = 0; i < fullPathLength; i++) {
            Pair<String, Double> probPair = new Pair<String, Double>(fullPathComponents.get(i), 1.0);
            zoneProbabilities.add(probPair);
        }

        uniformDist = new EnumeratedDistribution<String>(zoneProbabilities);
    }

    private void initRoundRobinExpFreqs() {
        roundRobinExpFreqs = new ArrayList<>();
        for (String component : fullPathComponents) {
            roundRobinExpFreqs.add(new Pair<String, Integer>(component, 0));
        }
    }

    public enum ZoneSelectionStrategy {
        ROUND_ROBIN_SAME_FREQ,
        ROUND_ROBIN_EXP_FREQ,
        RANDOM_UNFIORM,
        RANDOM_DECR_EXP
    }

    private String updateRoundRobinExpFreqs() {
        // TODO good direction
        for (int i = roundRobinExpFreqs.size() - 1; i > 0; i--) {
            Pair<String, Integer> p = roundRobinExpFreqs.get(i);
            Pair<String, Integer> nextP = roundRobinExpFreqs.get(i-1);

            if (2 * p.getSecond() < nextP.getSecond()) {
                roundRobinExpFreqs.add(i, new Pair<String, Integer>(p.getFirst(), p.getSecond() + 1));
                return p.getFirst();
            }
        }

        Pair<String, Integer> rootPath = roundRobinExpFreqs.get(0);
        roundRobinExpFreqs.add(0, new Pair<String, Integer>(rootPath.getFirst(), rootPath.getSecond() + 1));
        return rootPath.getFirst();
    }

    public PathName selectStrategy(ZoneSelectionStrategy selectionStrategy) {
        String selectedPath = null;

        switch(selectionStrategy) {
            case ROUND_ROBIN_SAME_FREQ:
                selectedPath = fullPathComponents.get(roundRobinSameIt);
                roundRobinSameIt = (roundRobinSameIt + 1) % fullPathLength;
                break;
            case ROUND_ROBIN_EXP_FREQ:
                selectedPath = updateRoundRobinExpFreqs();
                break;
            case RANDOM_UNFIORM:
                selectedPath = uniformDist.sample();
                break;
            case RANDOM_DECR_EXP:
                selectedPath = expDist.sample();
                break;
            default:
                throw new UnsupportedOperationException("Such strategy doesn't exist");
        }

        return new PathName(selectedPath);
    }
}
