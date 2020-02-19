package levelGenerators.ngram;

import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Collections;
import java.lang.StringBuilder;

import levelGenerators.ngram.TransitionTable;

public class NGram {

    static Random rand;
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
                try {
                    levels.add(parseLevelSlides(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void forwardBuilding(List<String> level, TransitionTable table) {
        List<String> slides = new ArrayList<String>();
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

    private void backwardBuilding(List<String> level, TransitionTable table) {
        Collections.reverse(level);
        forwardBuilding(level, table);
    }

    private TransitionTable buildTable(List<List<String>> levels) {
        TransitionTable table = new TransitionTable(null);
        for (List<String> level : levels) {
            forwardBuilding(level, table);
            backwardBuilding(level, table);
        }
        return table;
    }

    public List<String> generate(int size) {
        ArrayList<String> level = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            if (level.size() < deepness) {
                level.add(table.chooseSlide((ArrayList)level.clone(), Math.min(deepness, size - i)));
            } else {
                ArrayList<String> previous = new ArrayList<String>(((ArrayList)level.clone()).subList(level.size() - deepness, level.size()));
                String next = table.chooseSlide(previous, Math.min(deepness, size - i));
                level.add(next);
            }
        }
        return level;
    }

    public NGram(String directory, int deepness, long seed) {
        rand = new Random();
        if (seed >= 0) {
            rand.setSeed(seed);
        }
        this.deepness = deepness;
        File folder = new File(directory);
        System.out.println("load corpus files contents");
        List<List<String>> levels = new ArrayList<List<String>>();
        getCorpusFiles(folder, levels);
        table = buildTable(levels);
    }

    public String format(List<String> level) {
        StringBuilder builder = new StringBuilder("");
        for (int i = 0; i < level.get(0).length(); i++) {
            for (int j = 0; j < level.size(); j++) {
                builder.append(level.get(j).charAt(i));
            }
            builder.append('\n');
        }
        System.out.println("size : " + level.size());
        return builder.toString();
    }

    public static String randomLevel(int length, long seed) {
        NGram ngram = new NGram("levels/ngram", 4, seed);
        System.out.print("GENERATE\n");
        List<String> level = ngram.generate(length);
        System.out.println("size : " + level.size());
        return ngram.format(level);
    }
}
