package agents.geneticAlgorithm;

import java.lang.Thread;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;


public class RealTimeAgent extends AgentBase {

    int currentGeneration = 0;
    float usageLengthRatio = 0.5f;
    int usageLength;
    int currentGenStep = 0;
    long currentGenerationStart;
    Solution choosenSolution = null;
    MarioForwardModel currentModel;


    boolean sliding = false;

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
        starting = model;
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
        slide();
        System.out.println("ok");
    }

    @Override
    public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
        System.out.println("tick : " + model.world.currentTick + "/" + realStep);
        if ((int)(currentGenStep / Solution.granularity) >= usageLength) {
            slide();
        }
        boolean[] action = choosenSolution.getAction(currentGenStep);
        currentModel = model;
        currentModel.advance(action);
        realStep += 1;
        currentGenStep += 1;
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

    synchronized void slideStarting() {
        if (currentModel != null) {
            starting = currentModel.clone();
        }
        int tmpStep = 0;
        for (int i = 0; i < usageLength; i++) {
            for (int j = 0; j < Solution.granularity; j++) {
                starting.advance(choosenSolution.getAction(tmpStep));
                tmpStep += 1;
            }
        }
    }


    synchronized private void slide() {
        sliding = true;
        long duration = System.nanoTime() - currentGenerationStart;
        System.out.println("\nSTART SLIDING");
        System.out.println("elit : " + elit);
        System.out.println("\nSlide after " + currentGeneration + " generations");
        System.out.println("Slide after " + currentGenStep + " steps");
        System.out.println("Slide after "  + ((double)duration / (double)1_000_000_000) + " seconds");
        currentGenerationStart = System.nanoTime();
        currentGenStep = 0;
        // List<MarioForwardModel> snapshots = individus.get(elit).snapshots;
        // if (snapshots.size() >= usageLength) {
        //     if (snapshots.size() == usageLength) {
        //         System.out.println("Mario should die : " + snapshots.size() + "/" + usageLength);
        //     } else {
        //         System.out.println("snapshots : " + snapshots.size() + "/" + usageLength);
        //     }
        //     starting = snapshots.get(usageLength - 1);
        // } else {
        //     System.out.println("Mario should die : " + snapshots.size() + "/" + usageLength);
        // }

        // DEBUG /////
        List<MarioForwardModel> snapshots = individus.get(elit).snapshots;
        System.out.println("snapshots : " + snapshots.size() + "/" + usageLength);
        //////////////

        choosenSolution = clone(individus.get(elit));
        slideStarting();
        List<Solution> nextgen = new ArrayList<Solution>();
        for(int idx = 0; idx < individus.size(); idx++) {
            nextgen.add(slideCrossover(
                individus.get(elit),
                individus.get(idx)));
        }
        currentGeneration = 0;
        individus = nextgen;
        System.out.println("elit : " + elit);
        System.out.println("\nEND SLIDING");
        sliding = false;
    }

    synchronized private void nextGeneration() {
        if (sliding) System.out.println("Selecting while sliding !!!!!!!!!!!!!!!!!!!!");
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

