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

public class Agent extends AgentBase {

    public Agent(
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
        super(
            population,
            generations,
            tournamentRatio,
            mutationRate,
            crossoverRate,
            elitism,
            ticks,
            seconds,
            granularity
        );
    }

    public Agent() {
        super();
    }


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

    protected Solution randomSolution() {
        return new BasicSolution();
    }

    protected Solution offspring(Solution parent) {
        return new BasicSolution(
            parent,
            mutationRate);
    }

    protected Solution crossover(Solution parent1, Solution parent2) {
        return new BasicSolution(
            parent1,
            parent2,
            mutationRate);
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
