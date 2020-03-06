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
        if (steps <= 1) {
            return true;
        }
        for (TransitionTable table : nextSlides.values()) {
            if (table.canContinueUntil(steps - 1)) {
                return true;
            }
        }
        return false;
    }

    private String getLogRandom() {
        System.out.print("number choices (counts) : ");
        System.out.println(counts.size());
        System.out.print("number choices (next) : ");
        System.out.println(nextSlides.size());
        double totalLogCount = getTotalLogCount();
        double randn = NGram.rand.nextDouble() * totalLogCount;
        double count = 0.0;
        for (HashMap.Entry<String, Integer> entry : counts.entrySet()) {
            System.out.print("choice > ");
            System.out.println(entry.getKey());
            count += Math.log((double)entry.getValue() + 1);
            if (randn <= count) {
                return entry.getKey();
            }
        }
        return new String("--------------XX");
    }

    public String chooseSlide(List<String> previousSlides) {
        if (previousSlides == null || previousSlides.size() == 0) {
            String slide = getLogRandom();
            return slide;
        }
        String fromSlide = previousSlides.remove(0);
        System.out.print("from slide > ");
        System.out.println(fromSlide);
        if (nextSlides.get(fromSlide) == null) {
            return new String("--------------XX");
        }
        return nextSlides.get(fromSlide).chooseSlide(previousSlides);
    }
}
