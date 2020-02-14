package agents.geneticAlgorithm;

import java.util.Random;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

public abstract class AgentBase implements MarioAgent {

    int population = 50;
    int generations = 500;
    float tournamentRatio = 0.15f;
    int tournamentSize = (int)(population * tournamentRatio);
    float mutationRate = 0.02f;
    float crossoverRate = 0.5f;
    boolean elitism = true;
    Random random = new Random();
    int elit = -1;
    int realStep = 0;
    MarioForwardModel starting = null;

    List<Solution> individus;

    public AgentBase() {
        super();
    }

    public AgentBase(
        int population,
        int generations,
        float tournamentRatio,
        float mutationRate,
        float crossoverRate,
        boolean elitism,
        int ticks,
        int seconds,
        int granularity
    ) {
        this();
        this.population = population;
        this.generations = generations;
        this.tournamentRatio = tournamentRatio;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.elitism = elitism;
        this.tournamentSize = (int)(this.population * this.tournamentRatio);
        System.out.println("(original) Solution length : " + Solution.length);
        Solution.ticks = ticks;
        Solution.seconds = seconds;
        Solution.granularity = granularity;
        Solution.length = (Solution.seconds * Solution.ticks) / Solution.granularity;
    }

    protected List<Integer> shuffleIndividus() {
        List<Integer> randRange = new ArrayList<Integer>();
        for (int n : IntStream.range(0, population).toArray()) {
            randRange.add(n);
        }
        Collections.shuffle(randRange);
        return randRange;
    }

    protected List<Integer> selectForTournament() {
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


    protected void generateFirstGeneration() {
        individus = new ArrayList<Solution>();
        for (int i = 0; i < population; i++) {
            individus.add(randomSolution());
        }
    }

    protected int runTournament(List<Integer> selection) {
        float bestScore = -10000;
        int bestSolution = 0;
        boolean has_solution = false;
        for(int idx : selection) {
            Solution solution = individus.get(idx);
            solution.simulate(starting);
            float score = solution.score();
            if (score > bestScore) {
                has_solution = true;
                bestScore = score;
                bestSolution = idx;
            }
        }
        if (!has_solution) {
            System.out.println("NO SOLUTION");
        }
        return bestSolution;
    }

    protected void generateNextGeneration() {
        List<Solution> nextgen = new ArrayList<Solution>();
        if (elitism && elit >= 0) {
            nextgen.add(individus.get(elit));
        }
        List<Integer> randomSelection = shuffleIndividus();
        for(int idx : randomSelection) {
            if (random.nextFloat() < crossoverRate && elit >= 0) {
                nextgen.add(crossover(
                    individus.get(elit),
                    individus.get(idx)));
                if (nextgen.size() < population) {
                    nextgen.add(crossover(
                        individus.get(idx),
                        individus.get(elit)));
                }
            } else {
                nextgen.add(offspring(individus.get(idx)));
            }
            if (nextgen.size() >= population) {
                break;
            }
        }
        individus = nextgen;
    }

    protected abstract Solution randomSolution();
    protected abstract Solution offspring(Solution parent);
    protected abstract Solution crossover(Solution parent1, Solution parent2);
}
