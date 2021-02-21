package agents.geneticAlgorithm;

import java.lang.Thread;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;
import engine.helper.GameStatus;


public class RealTimeAgent extends AgentBase {

    int currentGeneration = 0;
    float usageLengthRatio = 0.5f;
    int usageLength;
    long currentGenerationStart;
    Solution choosenSolution = null;
    MarioForwardModel currentGenStarting;


    public RealTimeAgent() {
        super(
            50,     // population
            200,    // first generations
            0.15f,  // tournementRatio
            0.02f,  // mutation rate
            0.5f,   // crossover rate
            true,   // elitism
            20,     // ticks
            5,      // seconds
            3       // granularity
        );
        usageLength = (int)(Solution.length * usageLengthRatio);
        System.out.println("Solution length : " + Solution.length);
        System.out.println("Usage length : " + usageLength);
        currentGenerationStart = System.nanoTime();
    }


    @Override
    public void initialize(MarioForwardModel model, MarioTimer timer) {
        new Thread(() -> {
            while (true) {
                infiniteSelection();
            }
        }).start();
        starting = model.clone();
        currentGenStarting = null;
        generateFirstGeneration();
        long startTime = System.nanoTime();
        while (currentGeneration < generations) {
            try {
                System.out.println("wait first solution");
                Thread.sleep(300);
            } catch(InterruptedException e)
            {
            }
        }
        long duration = System.nanoTime() - startTime;
        System.out.println("Selection time : "  + ((double)duration / (double)1_000_000_000));
        slide(model);
        System.out.println("ok");
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        if (choosenSolution.getStep(
            currentGenStarting.world.currentTick,
            model.world.currentTick) >= usageLength)
        {
            slide(model);
            return getActions(model, timer);
        }
        boolean[] action = choosenSolution.getAction(
            currentGenStarting.world.currentTick,
            model.world.currentTick);
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

    protected Solution slideCrossover(Solution parent1, Solution parent2) {
        return new BasicSolution(
            parent1,
            parent2,
            mutationRate,
            usageLength);
    }

    protected Solution clone(Solution parent) {
        return new BasicSolution(
            parent,
            0.0f);
    }

    synchronized void slideStarting(MarioForwardModel current) {
        currentGenStarting = starting.clone();
        starting = current.clone();
        while (choosenSolution.getStep(
                currentGenStarting.world.currentTick,
                starting.world.currentTick
        ) < usageLength) {
            boolean[] actions = choosenSolution.getAction(
                currentGenStarting.world.currentTick,
                starting.world.currentTick);
            if (actions == null) {
                return;
            }
            if (starting.getGameStatus() != GameStatus.RUNNING) {
                return;
            }
            starting.advance(actions);
        }
    }


    synchronized private void slide(MarioForwardModel current) {
        long duration = System.nanoTime() - currentGenerationStart;
        currentGenerationStart = System.nanoTime();
        // DEBUG /////
        // individus.get(elit).simulate(starting, true);
        // individus.get(elit).simulate(current, true);
        // individus.get(elit).simulate(current.clone(), true);
        // List<MarioForwardModel> snapshots = individus.get(elit).snapshots;
        //////////////


        choosenSolution = clone(individus.get(elit));
        slideStarting(current);
        List<Solution> nextgen = new ArrayList<Solution>();
        for(int idx = 0; idx < individus.size(); idx++) {
            nextgen.add(slideCrossover(
                individus.get(elit),
                individus.get(idx)));
        }
        currentGeneration = 0;
        individus = nextgen;
    }

    synchronized private void nextGeneration() {
        List<Integer> selection = selectForTournament();
        generateNextGeneration();
        elit = runTournament(selection);
        currentGeneration += 1;
    }

    private void infiniteSelection() {
        while(true) {
            nextGeneration();
            try {
                Thread.sleep(1);
            } catch(InterruptedException e)
            {
            }
        }
    }
}

