import java.util.concurrent.TimeUnit;

public class main {
    public static void main(String[] args) {
        long startTime;
        double timeElapsed;
        long endTime;

        int numberOfIterations = 10000;
        String schedulingPolicy = "Static-cyclic"; //options: "Static-block", "Static-Cyclic", "Dynamic", "Guided"
        String stringNumberOfThreads = "1";
        int chunkSize = 720; // chunk size between 1 and 1280, this will be the number of rows or columns in each chunk
        String chunkMethod = "by Row"; // "by Row", "by Column"


        if (args.length >= 1) numberOfIterations = Integer.parseInt(args[0]);
        if (args.length >= 2) schedulingPolicy = args[1];
        if (args.length >= 3) stringNumberOfThreads = args[2];
        if (args.length >= 4) chunkSize = Integer.parseInt(args[3]);
        if (args.length >= 5) chunkMethod = args[4];
           
        
        startTime = System.nanoTime();
        
        Chunking t1 = new Chunking(numberOfIterations,
                                   schedulingPolicy,
                                   stringNumberOfThreads, 
                                   chunkSize,
                                   chunkMethod);
        Thread th = new Thread(t1);
        th.setDaemon(false);
        th.start();
        
        try {
            th.join();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        endTime = System.nanoTime();
        timeElapsed = (TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS)/1000.000);
        
        System.out.println("Number of iterations selected: " + numberOfIterations);
        System.out.println("Scheduling policy selected: " + schedulingPolicy);
        System.out.println("Number of threads selected: " + stringNumberOfThreads);
        System.out.println("Chunk size selected: " + chunkSize);
        System.out.println("Chunking method selected: " + chunkMethod);
        System.out.println("Total time taken: " + timeElapsed + "s");
    }
}