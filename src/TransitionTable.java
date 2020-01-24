import java.util.List;
import java.util.HashMap;
import java.util.Random;

public class TransitionTable {
    HashMap<byte[], TransitionTable> nextSlides;
    HashMap<byte[], Integer> counts;
    Random random = new Random();

    public TransitionTable() {
        nextSlides = new HashMap<byte[], TransitionTable>();
        counts = new HashMap<byte[], Integer>();
    }

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

    private byte[] getRandom() {
        System.out.print("count ");
        System.out.print(getTotalCount());
        System.out.print("\n");
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
        System.out.print("choose slide\n");
        System.out.print("size : ");
        System.out.print(previousSlides.size());
        System.out.print("\n");
        if (previousSlides == null || previousSlides.size() == 0) {
            System.out.print("return random\n");
            return getRandom();
        }
        byte[] fromSlide = previousSlides.remove(0);
        System.out.print(new String(fromSlide));
        System.out.print("\n");
        return nextSlides.get(fromSlide).chooseSlide(previousSlides);
    }
}
