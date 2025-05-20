import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import javax.imageio.ImageIO;

public class Chunking implements Runnable {
    private ArrayList<chunk> chunk_array;
    private final String schedulingPolicy;
    private final String string_num_threads;
    private int num_threads;
    private final int chunkSize;
    private final String chunkMethod;
    private final int height = 720;
    private final int width = 1280;
    private final BufferedImage mandelbrotImage; 
    private long startTime;
    private double timeElapsed;
    private long endTime;
    private final int numberOfIterations;
    private final String[] colours = {"#E3170A", "#F75C03", "#FAA613", "#F3DE2C", "#F0F66E", "#DB00B6", "#a100f2", "#3772ff"};
    private double zoom = 4.0;
    private double offset_y = 0.0;
    private double offset_x = 0.0;

    public Chunking(int numberOfIterations, String schedulingPolicy, String string_num_threads, 
                       int chunkSize, String chunkMethod) {
        this.schedulingPolicy = schedulingPolicy;
        this.string_num_threads = string_num_threads;
        this.chunkMethod = chunkMethod;
        this.chunkSize = chunkSize;
        this.numberOfIterations = numberOfIterations;
        this.mandelbrotImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        if(string_num_threads.equals("True Sequential")) {
            // Sequential code
        } else {
            this.num_threads = Integer.parseInt(string_num_threads);
            chunk_array = new ArrayList<chunk>();
        }
    }

    @Override
    public void run() {
        startTime = System.nanoTime();
        
        if (string_num_threads.equals("True Sequential")) {
            Sequential();
            System.out.println("Ran True Sequential");
        } else {
            createChunks(chunkMethod);
            processChunksInParallel();
        }
        
        try {
            // Save the image to a file
            File outputFile = new File("mandelbrot.png");
            ImageIO.write(mandelbrotImage, "png", outputFile);
            System.out.println("Image saved to: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }
    
    private void processChunksInParallel() {
        ExecutorService executor = Executors.newFixedThreadPool(num_threads);
        List<Future<?>> futures = new ArrayList<>();
        
        for (int i = 0; i < chunk_array.size(); i++) {
            final int index = i;
            futures.add(executor.submit(() -> processChunk(chunk_array.get(index))));
        }
        
        boolean running = true;
        while (running) {
            int doneTasks = 0;
            for (Future<?> future : futures) {
                if (future.isDone()) {
                    doneTasks++;
                }
            }
            
            if (doneTasks == futures.size()) {
                running = false;
            }
            
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
        endTime = System.nanoTime();
        timeElapsed = (TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS) / 1000.000);
        System.out.println("Processing completed in: " + timeElapsed + "s");
    }
    
    private void processChunk(chunk currentChunk) {
        for (int j = 0; j < currentChunk.getSize(); j++) {
            pixel p = currentChunk.getPixel(j);
            int x = p.getX();
            int y = p.getY();
            
            double c_re = ((x - width / 2.0) * zoom / width) + offset_x;
            double c_im = ((y - height / 2.0) * zoom / width) + offset_y;
            double zx = 0, zy = 0;
            int iteration = 0;
            
            while (zx * zx + zy * zy <= 4 && iteration < numberOfIterations) {
                double zx_new = zx * zx - zy * zy + c_re;
                zy = 2 * zx * zy + c_im;
                zx = zx_new;
                iteration++;
            }
            
            int color;
            if (iteration < numberOfIterations) {
                // Choose color based on iteration count
                color = getColorForIteration(iteration);
            } else {
                color = Color.BLACK.getRGB();
            }
            
            synchronized (mandelbrotImage) {
                mandelbrotImage.setRGB(x, y, color);
            }
        }
    }
    
    private int getColorForIteration(int iteration) {
        String hexColor;
        if (iteration < numberOfIterations/1000) {
            hexColor = colours[0];
        } else if (iteration < numberOfIterations/500) {
            hexColor = colours[1];
        } else if (iteration < numberOfIterations/300) {
            hexColor = colours[2];
        } else if (iteration < numberOfIterations/200) {
            hexColor = colours[3];
        } else if (iteration < numberOfIterations/100) {
            hexColor = colours[4];
        } else if (iteration < numberOfIterations/75) {
            hexColor = colours[5];
        } else if (iteration < numberOfIterations/50) {
            hexColor = colours[6];
        } else {
            hexColor = colours[7];
        }
        return Color.decode(hexColor).getRGB();
    }

    public void Sequential() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double c_re = ((j - width / 2.0) * zoom / width) + offset_x;
                double c_im = ((i - height / 2.0) * zoom / width) + offset_y;
                double x = 0, y = 0;
                int iteration = 0;

                while (x * x + y * y <= 4 && iteration < numberOfIterations) {
                    double x_new = x * x - y * y + c_re;
                    y = 2 * x * y + c_im;
                    x = x_new;
                    iteration++;
                }

                int color;
                if (iteration < numberOfIterations) {
                    color = getColorForIteration(iteration);
                } else {
                    color = Color.BLACK.getRGB();
                }
                
                mandelbrotImage.setRGB(j, i, color);
            }
        }
    }

    public void createChunks(String type) {
        switch (schedulingPolicy.toLowerCase()) {
            case "static-block":
                blockChunking(type);
                break;
            case "static-cyclic":
                cyclicChunking(type, chunkSize);
                break;
            case "dynamic":
                dynamicChunking(type, chunkSize);
                break;
            case "guided":
                guidedChunking(type);
                break;
        }
    }
    
    private chunk chunkType(String type, int currentPos) {
        chunk currentChunk = new chunk();
        if (type.equals("by Row")) {
            for (int j = 0; j < width; j++) {
                currentChunk.add(j, currentPos);
            }
        } else {
            for (int i = 0; i < height; i++) {
                currentChunk.add(currentPos, i);
            }
        }
        return currentChunk;
    }

    private void blockChunking(String type) {
        int length_1 = 0, length_2 = 0;
        for (int c = 0; c < num_threads; c++) {
            chunk_array.add(new chunk());
        }
        if (type.equals("by Row")) {
            length_1 = height;
            length_2 = width;
        } else {
            length_1 = width;
            length_2 = height;
        }
        for (int k = 0; k < num_threads; k++) {
            for (int i = 0; i < length_1 / num_threads; i++) {
                for (int j = 0; j < length_2; j++) {
                    if (length_1 == height) {
                        chunk_array.get(k).add(j, i + ((height / num_threads) * k));
                    } else {
                        chunk_array.get(k).add(i + ((width / num_threads) * k), j);
                    }
                }
            }
        }
    }

    private void cyclicChunking(String type, int chunkSize) {
        int currentCore = 0;
        int max;
        if (type.equals("by Row")) max = height; else max = width;
        for (int c = 0; c < num_threads; c++) {
            chunk_array.add(new chunk());
        }
        for (int i = 0; i < max; i++) {
            for (int j = 0; j < chunkSize; j++) {
                chunk_array.get(currentCore).appendChunks(chunkType(type, i));
                i++;
                if (i >= max) break;
            }
            currentCore++;
            if (currentCore >= num_threads) {
                currentCore = 0;
            }
            i--;
        }
    }

    private void dynamicChunking(String type, int chunkSize) {
        int max;
        int index = 0;
        int count = 0;
        int chunkArrayPos = 0;
        if (type.equals("by Row")) max = height; else max = width;
        while (index < max) {
            while (count < chunkSize) {
                if (count == 0) {
                    chunk_array.add(new chunk());
                }
                chunk_array.get(chunkArrayPos).appendChunks(chunkType(type, index));
                index++;
                count++;
                if (index >= max) break;
            }
            chunkArrayPos++;
            count = 0;
        }
    }

    private void guidedChunking(String type) {
        int max;
        int index = 0;
        int chunkSize = 30;
        int count = 0;
        int i = 0;
        int chunkArrayPos = 0;
        if (type.equals("by Row")) max = height; else max = width;
        while (index < max) {
            while (count < chunkSize) {
                if (count == 0) {
                    chunk_array.add(new chunk());
                }
                chunk_array.get(chunkArrayPos).appendChunks(chunkType(type, index));
                index++;
                count++;
                if (index >= max) break;
            }
            i++;
            if (i >= num_threads && chunkSize > 1) {
                chunkSize--;
                i = 0;
            }
            chunkArrayPos++;
            count = 0;
        }
    }
}