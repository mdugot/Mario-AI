import java.util.List;
import java.util.HashMap;
import java.util.Random;

public class TransitionTable {
    HashMap<byte[], TransitionTable> nextSlides;
    HashMap<byte[], Integer> counts;
    Random random = new Random();

    public void addSlides(List<byte[]> slides)
    {
        byte[] firstSlide = slides.get(0);
        if (nextSlides.get(firstSlide) == null) {
            nextSlides.put(firstSlide, new TransitionTable());
            counts.put(firstSlide, 1);
        } else {
            counts.put(firstSlide, counts.get(firstSlide) + 1);
        }
        if (slides.size() > 1) {
            slides.remove(0);
            nextSlides.get(firstSlide).addSlides(slides);
        }
    }

    private int getTotalCount() {
        int total = 0;
        for (int count : counts.values()) {
            total += count;
        }
        return total;
    }

    private byte[] getRandom() {
        int count = 0;
        int randn = random.nextInt(getTotalCount());
        for (HashMap.Entry<byte[], Integer> entry : counts.entrySet()) {
            count += entry.getValue();
            if (randn < count) {
                return entry.getKey();
            }
        }
        return new byte[0];
    }

    public byte[] chooseSlide(List<byte[]> previousSlides) {
        if (previousSlides == null || previousSlides.size() == 0) {
            return getRandom();
        }
        byte[] fromSlide = previousSlides.remove(0);
        return nextSlides.get(fromSlide).chooseSlide(previousSlides);
    }
}
