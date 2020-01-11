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

        for (int i = 0; i < fullPathLength; i++) {
            Pair<String, Double> probPair = new Pair<String, Double>(fullPathComponents.get(i), Math.exp((double) i+1));
            zoneProbabilities.add(probPair);
        }

        expDist = new EnumeratedDistribution<String>(zoneProbabilities);
    }

    private void initUniformZoneProbabilities() {
        ArrayList<Pair<String, Double>> zoneProbabilities;
        zoneProbabilities = new ArrayList<>(fullPathLength);
        Double uniformProb = 1.0/fullPathLength;

        for (int i = 0; i < fullPathLength; i++) {
            Pair<String, Double> probPair = new Pair<String, Double>(fullPathComponents.get(i), uniformProb);
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
        RANDOM_DECR_EXP;

        public static ZoneSelectionStrategy stringToStrategy(String strategyString) throws Exception {
            switch (strategyString) {
                case "round_robin":
                    return ROUND_ROBIN_SAME_FREQ;
                case "round_robin_exp":
                    return ROUND_ROBIN_EXP_FREQ;
                case "random":
                    return RANDOM_UNFIORM;
                case "random_exp":
                    return RANDOM_DECR_EXP;
                default:
                    throw new Exception("Invalid strategy string");
            }
        }
    }

    private String updateRoundRobinExpFreqs() {
        for (int i = 0; i < roundRobinExpFreqs.size() - 1; i++) {
            Pair<String, Integer> p = roundRobinExpFreqs.get(i);
            Pair<String, Integer> nextP = roundRobinExpFreqs.get(i+1);

            if (2 * p.getSecond() < nextP.getSecond()) {
                roundRobinExpFreqs.set(i, new Pair<String, Integer>(p.getFirst(), p.getSecond() + 1));
                return p.getFirst();
            }
        }

        Pair<String, Integer> rootPath = roundRobinExpFreqs.get(roundRobinExpFreqs.size() - 1);
        roundRobinExpFreqs.set(
                roundRobinExpFreqs.size() - 1,
                new Pair<String, Integer>(rootPath.getFirst(), rootPath.getSecond() + 1));
        return rootPath.getFirst();
    }

    private String formNewPath(String selectedPath) {
        String newPath = "";
        for (String pathEl : fullPathComponents) {
            newPath = newPath.concat("/" + pathEl);
            if (pathEl.equals(selectedPath))
                break;
        }
        return newPath;
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

        return new PathName(formNewPath(selectedPath));
    }
}
