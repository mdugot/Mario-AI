package agents.geneticAlgorithm;

public class BasicSolution extends Solution {

    protected int getCrossoverPoint() {
        return random.nextInt() % length;
    }

    protected boolean inMutationWindow(int index) {
        return true;
    }

    protected byte getMutation() {
        return (byte)Math.abs(random.nextInt() % actions.size());
    }

    protected byte getRandomAction() {
        // return (byte)Math.abs(random.nextInt() % actions.size());
        if (Math.abs(random.nextInt() % 2) == 0) {
            return 1;
        }
        return 2;
    }

    public BasicSolution() {
        super();
    }

    public BasicSolution(Solution parent, float mutationRate) {
        super(parent, mutationRate);
    }

    public BasicSolution(Solution parent1, Solution parent2, float mutationRate) {
        super(parent1, parent2, mutationRate);
    }
}
