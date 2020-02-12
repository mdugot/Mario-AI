package agents.geneticAlgorithm;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

public class Agent implements MarioAgent {

    static int population = 50;
    static int generations = 500;
    static float tournamentRatio = 0.15f;
    static int tournamentSize = (int)(population * tournamentRatio);
    static float mutationRate = 0.02f;
    static float crossoverRate = 0.5f;
    static boolean elitism = true;
    static Random random = new Random();

    List<Solution> individus;
    int elit = -1;
    int realStep = 0;
    MarioForwardModel starting = null;

    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        starting = model;
        generateFirstGeneration();
        selectSolution();
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        Solution solution = individus.get(elit);
        boolean[] action = solution.getAction(realStep);
        realStep += 1;
        return action;
    }

    @Override
    public String getAgentName() {
        return "GeneticAlgorithm";
    }

    private List<Integer> shuffleIndividus() {
        List<Integer> randRange = new ArrayList<Integer>();
        for (int n : IntStream.range(0, population).toArray()) {
            randRange.add(n);
        }
        Collections.shuffle(randRange);
        return randRange;
    }

    private void generateFirstGeneration() {
        individus = new ArrayList<Solution>();
        for (int i = 0; i < population; i++) {
            individus.add(new BasicSolution());
        }
    }

    private List<Integer> selectForTournament() {
        List<Integer> result = new ArrayList<Integer>();
        List<Integer> individus = shuffleIndividus();
        if (elitism && elit >= 0) {
            result.add(elit);
        }
        int idx = 0;
        while (result.size() < tournamentSize) {
            int individu = individus.get(idx);
            if (!elitism || individu != elit) {
                result.add(individu);
            }
            idx += 1;
        }
        return result;
    }

    private int runTournament(List<Integer> selection) {
        float bestScore = 0;
        int bestSolution = 0;
        for(int idx : selection) {
            Solution solution = individus.get(idx);
            solution.simulate(starting);
            float score = solution.score();
            if (score > bestScore) {
                bestScore = score;
                bestSolution = idx;
            }
        }
        return bestSolution;
    }

    private void generateNextGeneration() {
        List<Solution> nextgen = new ArrayList<Solution>();
        if (elitism && elit >= 0) {
            nextgen.add(individus.get(elit));
        }
        List<Integer> randomSelection = shuffleIndividus();
        for(int idx : randomSelection) {
            if (random.nextFloat() < crossoverRate) {
                nextgen.add(new BasicSolution(
                    individus.get(elit),
                    individus.get(idx),
                    mutationRate));
                if (nextgen.size() < population) {
                    nextgen.add(new BasicSolution(
                        individus.get(idx),
                        individus.get(elit),
                        mutationRate));
                }
            } else {
                nextgen.add(new BasicSolution(
                    individus.get(elit),
                    mutationRate));
            }
            if (nextgen.size() >= population) {
                break;
            }
        }
        individus = nextgen;
    }

    private void selectSolution() {
        long startTime = System.nanoTime();
        for (int g = 0; g < generations; g++) {
            System.out.println("generation : " + g + "/" + generations);
            List<Integer> selection = selectForTournament();
            elit = runTournament(selection);
            generateNextGeneration();
        }
        long duration = System.nanoTime() - startTime;
        System.out.println("Selection time : "  + ((double)duration / (double)1_000_000_000));
    }
}
