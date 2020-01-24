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

    private List<byte[]> parseLevelSlides(File file) throws IOException
    {
        byte[] content = Files.readAllBytes(Paths.get(file.getPath()));
        int length = 0;
        List<byte[]> slides = new ArrayList<byte[]>();
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
                if (slides.size() <= x) {
                    slides.add(new byte[height]);
                }
                slides.get(x)[y] = ascii;
                x += 1;
             }
        }
        return slides;
    }

    private void getCorpusFiles(File folder, List<List<byte[]>> levels) {
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

    private TransitionTable buildTable(List<List<byte[]>> levels) {
        TransitionTable table = new TransitionTable();
        List<byte[]> slides = new ArrayList<byte[]>();
        for (List<byte[]> level : levels) {
            slides.clear();
            for (int i = 0; i < deepness + 1; i++) {
                slides.add(level.get(i));
            }
            table.addSlides(slides);
            for (int i = deepness + 1; i < levels.size(); i++) {
                slides.remove(0);
                slides.add(level.get(i));
                table.addSlides(slides);
            }
        }
        return table;
    }

    public List<byte[]> generate(int size) {
        List<byte[]> level = new ArrayList<byte[]>();
        for (int i = 0; i < size; i++) {
            if (level.size() < deepness) {
                level.add(table.chooseSlide(level));
                System.out.print("-----\n");
            } else {
                level.add(table.chooseSlide(level.subList(level.size() - deepness, level.size())));
                System.out.print("-----\n");
            }
        }
        return level;
    }

    public NGram(String directory, int deepness) {
        this.deepness = deepness;
        File folder = new File(directory);
        System.out.println("load corpus files contents");
        List<List<byte[]>> levels = new ArrayList<List<byte[]>>();
        getCorpusFiles(folder, levels);
        table = buildTable(levels);
    }

    public static void main(String[] args) {
        NGram ngram = new NGram("levels/original", 2);
        List<byte[]> level = ngram.generate(30);
    }
}
