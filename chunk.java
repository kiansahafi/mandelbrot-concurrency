import java.util.ArrayList;

public class chunk {
    private final ArrayList<pixel> pixels = new ArrayList<>();

    public void add(int x, int y) {
        pixels.add(new pixel(x, y));
    }

    public void appendChunks(chunk otherChunk) {
        for (int i = 0; i < otherChunk.getSize(); i++) {
            pixels.add(otherChunk.getPixel(i));
        }
    }

    public int getSize() {
        return pixels.size();
    }

    public pixel getPixel(int i) {
        return pixels.get(i);
    }
}