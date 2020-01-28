import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Dictionary;

public class NGram {

    TransitionTable table;
    int deepness;

    private List<String> parseLevelSlides(File file) throws IOException
    {
        byte[] content = Files.readAllBytes(Paths.get(file.getPath()));
        int length = 0;
        List<byte[]> bslides = new ArrayList<byte[]>();
        List<String> slides = new ArrayList<String>();
        int y = 0;
        int x = 0;
        while (content[length] != '\n') {
            length += 1;
        }
        int height = content.length / length;
        System.out.println(length);
        System.out.println(height);
        for (byte ascii : content) {
             if (ascii == '\n') {
                x = 0;
                y += 1;
             } else {
                if (bslides.size() <= x) {
                    bslides.add(new byte[height]);
                }
                bslides.get(x)[y] = ascii;
                x += 1;
             }
        }
        for (byte[] bslide : bslides) {
            slides.add(new String(bslide));
        }
        return slides;
    }

    private void getCorpusFiles(File folder, List<List<String>> levels) {
        for (File file: folder.listFiles()) {
            if (file.isDirectory()) {
                getCorpusFiles(file, levels);
            }
            if (file.isFile() && file.getName().endsWith(".txt")) {
                System.out.println(file.getPath());
                try {
                    levels.add(parseLevelSlides(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private TransitionTable buildTable(List<List<String>> levels) {
        TransitionTable table = new TransitionTable(null);
        List<String> slides = new ArrayList<String>();
        for (List<String> level : levels) {
            System.out.print("BUILD TABLE\n");
            slides.clear();
            for (int i = 0; i < deepness + 1; i++) {
                slides.add(level.get(i));
            }
            table.addSlides(slides);
            for (int i = deepness + 1; i < level.size(); i++) {
                slides.remove(0);
                slides.add(level.get(i));
                table.addSlides(slides);
            }
            while (slides.size() > 1) {
                slides.remove(0);
                table.addSlides(slides);
            }
        }
        return table;
    }

    public List<String> generate(int size) {
        List<String> level = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            if (level.size() < deepness) {
                level.add(table.chooseSlide(level, Math.min(deepness, size - i)));
            } else {
                level.add(table.chooseSlide(level.subList(level.size() - deepness, level.size()), Math.min(deepness, size - i)));
            }
        }
        return level;
    }

    public NGram(String directory, int deepness) {
        this.deepness = deepness;
        File folder = new File(directory);
        System.out.println("load corpus files contents");
        List<List<String>> levels = new ArrayList<List<String>>();
        getCorpusFiles(folder, levels);
        table = buildTable(levels);
    }

    public static void main(String[] args) {
        NGram ngram = new NGram("levels/original", 4);
        System.out.print("GENERATE\n");
        List<String> level = ngram.generate(80);
    }
}
