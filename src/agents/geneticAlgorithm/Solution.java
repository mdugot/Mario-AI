package agents.geneticAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import engine.core.MarioForwardModel;
import engine.helper.GameStatus;

public abstract class Solution {
    static List<boolean[]> actions = new ArrayList<boolean[]>(List.of(
        //LEFT RIGHT DOWN RUN JUMP
        //do nothing
        // (new boolean[]{false, false, false, false, false}),
        //right run
        (new boolean[]{false, true, false, true, false}),
        //right jump and run
        (new boolean[]{false, true, false, true, true}),
        // right
        //(new boolean[]{false, true, false, false, false}),
        // right jump
        (new boolean[]{false, true, false, false, true}),
        //left
        //(new boolean[]{true, false, false, false, false}),
        //left run
        (new boolean[]{true, false, false, true, false}),
        //left jump
        (new boolean[]{true, false, false, false, true}),
        //left jump and run
        (new boolean[]{true, false, false, true, true})
    ));

    public static int ticks = 20;
    public static int seconds = 5;
    public static int granularity = 5;
    public static int length = (seconds * ticks) / granularity;
    static Random random = AgentBase.random;

    private byte[] chromosome;
    public List<MarioForwardModel> snapshots;

    public Solution() {
        chromosome = new byte[length];
        snapshots = new ArrayList<MarioForwardModel>();
        for (int i = 0; i < length; i++) {
            chromosome[i] = getRandomAction();
        }
    }

    public Solution(Solution parent, float mutationRate) {
        this(parent, mutationRate, 0);
    }

    public Solution(Solution parent, float mutationRate, int slide) {
        this();
        for (int i = 0; i < length; i++) {
            int j = i + slide;
            if ((inMutationWindow(i) && random.nextFloat() < mutationRate) ||
                j >= length) {
                chromosome[i] = getMutation();
            } else {
                chromosome[i] = parent.chromosome[j];
            }
        }
    }

    public Solution(Solution parent1, Solution parent2, float mutationRate) {
        this(parent1, parent2, mutationRate, 0);
    }

    public Solution(Solution parent1, Solution parent2, float mutationRate, int slide) {
        this();
        int crossover = getCrossoverPoint(slide);
        for (int i = 0; i < length; i++) {
            int j = i + slide;
            if ((inMutationWindow(i) && random.nextFloat() < mutationRate) ||
                j >= length) {
                chromosome[i] = getMutation();
            } else {
                if (j < crossover) {
                    chromosome[i] = parent1.chromosome[j];
                } else {
                    chromosome[i] = parent2.chromosome[j];
                }
            }
        }
    }

    public int getStep(int startStep, int realStep) {
        return (realStep - startStep) / granularity;
    }

    public boolean[] getAction(int startStep, int realStep) {
        int step = getStep(startStep, realStep);
        if (step >= chromosome.length) {
            return null;
        }
        return actions.get(chromosome[step]);
    }

    public void simulate(MarioForwardModel starting) {
        simulate(starting, false);
    }

    public void simulate(MarioForwardModel starting, boolean verbose) {
        MarioForwardModel model = starting.clone();
        snapshots.clear();
        int currentStep = 0;
        int lastStep = 0;
        while (true) {
            boolean[] actions = getAction(starting.world.currentTick, model.world.currentTick);
            if (actions == null) {
                return;
            }
            lastStep = getStep(starting.world.currentTick, model.world.currentTick);
            model.advance(actions);
            GameStatus status = model.getGameStatus();
            if (status != GameStatus.RUNNING) {
                if (verbose)
                    System.out.println("Mario should die");
                snapshots.add(model.clone());
                return;
            }
            currentStep = getStep(starting.world.currentTick, model.world.currentTick);
            if (currentStep > lastStep) {
                snapshots.add(model.clone());
            }
        }
    }

    public float score() {
        MarioForwardModel model = snapshots.get(snapshots.size() - 1);
        float score = 0;
        if (model.getGameStatus() == GameStatus.WIN) {
            score += 1024;
        } else if (model.getGameStatus() != GameStatus.RUNNING) {
            score -= 1024;
        }
        score += model.getMarioFloatPos()[0] / 3;
        score += model.getKillsByStomp() * 12;
        score += model.getKillsByFire() * 4;
        score += model.getKillsByShell() * 17;
        score += (300 - model.getMarioFloatPos()[1]) / 10;
        score += model.getMarioMode() * 32;
        score += model.getNumCollectedCoins() * 16;
        score += model.getNumCollectedMushrooms() * 58;
        score += model.getNumCollectedFireflower() * 64;
        return score;
    }

    protected abstract byte getRandomAction();
    protected abstract byte getMutation();
    protected abstract int getCrossoverPoint(int from);
    protected abstract boolean inMutationWindow(int index);

    protected int getCrossoverPoint() {return getCrossoverPoint(0);}
}
