package levelGenerators.ngram;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

public class TransitionTable {
    HashMap<String, TransitionTable> nextSlides;
    HashMap<String, Integer> counts;
    TransitionTable root;

    public TransitionTable(TransitionTable root) {
        nextSlides = new HashMap<String, TransitionTable>();
        counts = new HashMap<String, Integer>();
        if (root == null) {
            this.root = this;
        } else {
            this.root = root;
        }
    }

    public void addSlides(List<String> slides)
    {
        String firstSlide = slides.get(0);
        if (nextSlides.get(firstSlide) == null) {
            nextSlides.put(firstSlide, new TransitionTable(root));
            counts.put(firstSlide, 1);
        } else {
            counts.put(firstSlide, counts.get(firstSlide) + 1);
        }
        if (slides.size() > 1) {
            nextSlides.get(firstSlide).addSlides(slides.subList(1, slides.size()));
        }
    }

    private int getTotalCount() {
        int total = 0;
        for (int count : counts.values()) {
            total += count;
        }
        return total;
    }

    private double getTotalLogCount() {
        double total = 0;
        for (int count : counts.values()) {
            total += Math.log((double)count + 1);
        }
        return total;
    }

    private boolean canContinueUntil(int steps) {
        if (steps < 1) {
            return true;
        }
        for (TransitionTable table : nextSlides.values()) {
            if (table.canContinueUntil(steps - 1)) {
                return true;
            }
        }
        return false;
    }

    private String getRandom(int deepness) {
        List<Integer> randRange = new ArrayList<Integer>();
        for (int n : IntStream.range(0, getTotalCount()).toArray()) {
            randRange.add(n);
        }
        Collections.shuffle(randRange, NGram.rand);
        for (Integer randn : randRange) {
            int count = 0;
            for (HashMap.Entry<String, Integer> entry : counts.entrySet()) {
                count += entry.getValue();
                if (randn < count) {
                    if (root.nextSlides.get(entry.getKey()).canContinueUntil(deepness)) {
                        return entry.getKey();
                    }
                    break;
                }
            }
        }
        System.out.println("deepness : " + deepness);
        System.out.println("counts : " + counts.size());
        System.out.println("slide : " + (String)counts.keySet().toArray()[0]);
        System.out.println("root size : " + root.nextSlides.size());
        System.out.println("next counts : " + root.nextSlides.get(counts.keySet().toArray()[0]).counts.size());
        return null;
    }

    private String getLogRandom(int deepness) {
        while (counts.size() >= 1) {
            double totalLogCount = getTotalLogCount();
            double randn = NGram.rand.nextDouble() * totalLogCount;
            double count = 0.0;
            for (HashMap.Entry<String, Integer> entry : counts.entrySet()) {
                count += Math.log((double)entry.getValue() + 1);
                if (randn <= count) {
                    if (root.nextSlides.get(entry.getKey()).canContinueUntil(deepness)) {
                        return entry.getKey();
                    }
                    // counts.remove(entry.getKey());
                    break;
                }
            }
        }
        return null;
    }

    public String chooseSlide(List<String> previousSlides, int deepness) {
        if (previousSlides == null || previousSlides.size() == 0) {
            String slide = getLogRandom(deepness);
            return slide;
        }
        String fromSlide = previousSlides.remove(0);
        System.out.println(fromSlide);
        return nextSlides.get(fromSlide).chooseSlide(previousSlides, deepness);
    }
}
