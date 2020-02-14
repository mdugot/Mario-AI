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
    static Random random = new Random();

    private byte[] chromosome;
    private List<MarioForwardModel> snapshots;
    private int last = -1;

    public Solution() {
        chromosome = new byte[length];
        snapshots = new ArrayList<MarioForwardModel>();
        for (int i = 0; i < length; i++) {
            chromosome[i] = getRandomAction();
            snapshots.add(null);
        }
    }

    public Solution(Solution parent, float mutationRate) {
        this();
        for (int i = 0; i < length; i++) {
            if (inMutationWindow(i) && random.nextFloat() < mutationRate) {
                chromosome[i] = getMutation();
            } else {
                chromosome[i] = parent.chromosome[i];
            }
        }
    }

    public Solution(Solution parent1, Solution parent2, float mutationRate) {
        this();
        int crossover = getCrossoverPoint();
        for (int i = 0; i < length; i++) {
            if (inMutationWindow(i) && random.nextFloat() < mutationRate) {
                chromosome[i] = getMutation();
            } else {
                if (i < crossover) {
                    chromosome[i] = parent1.chromosome[i];
                } else {
                    chromosome[i] = parent2.chromosome[i];
                }
            }
        }
    }

    public boolean[] getAction(int realStep) {
        int step = realStep / granularity;
        if (step >= chromosome.length) {
            System.out.println("all actions used");
            return actions.get(0);
        }
        return actions.get(chromosome[step]);
    }

    public void simulate(MarioForwardModel starting) {
        MarioForwardModel model = starting.clone();
        snapshots.clear();
        for (int i = 0; i < length; i++) {
            last = i;
            for (int j = 0; j < granularity; j++) {
                model.advance(actions.get(chromosome[i]));
                GameStatus status = model.getGameStatus();
                if (status != GameStatus.RUNNING) {
                    snapshots.add(model.clone());
                    return;
                }
            }
            snapshots.add(model.clone());
        }
    }

    public float score() {
        MarioForwardModel model = snapshots.get(last);
        float score = 0;
        if (model.getGameStatus() == GameStatus.WIN) {
            score += 1024;
        }
        score += model.getKillsByStomp() * 12;
        score += model.getKillsByFire() * 4;
        score += model.getKillsByShell() * 17;
        score += model.getMarioFloatPos()[0];
        score += (300 - model.getMarioFloatPos()[1]) /10;
        score += model.getMarioMode() * 32;
        score += model.getNumCollectedCoins() * 16;
        score += model.getNumCollectedMushrooms() * 58;
        score += model.getNumCollectedFireflower() * 64;
        return score;
    }

    protected abstract byte getRandomAction();
    protected abstract byte getMutation();
    protected abstract int getCrossoverPoint();
    protected abstract boolean inMutationWindow(int index);
}
