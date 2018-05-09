import java.util.Arrays;

public class Oblig5 {
    int[] x;
    int[] y;

    static int n;
    static int k;

    static int MAX_X;
    static int MAX_Y;

    // Timings
    private static final int runs = 7;
    private static final int medianIndex = 4;
    private static double[] seqTiming = new double[runs];
    private static double[] parTiming = new double[runs];

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("SYNTAX: java Oblig5 [antall punkter] [antall trader]");
            return;
        }

        n = Integer.parseInt(args[0]);
        n = Integer.parseInt(args[1]);

        MAX_X = Math.max(10,(int) Math.sqrt(n) * 3); // Same as in NPunkter.
        MAX_Y = MAX_X;

        for (int i = 0; i < runs; i++) {
            new Oblig5(i);
        }

        Arrays.sort(seqTiming);
        Arrays.sort(parTiming);

        System.out.printf("Sequential median : %.3f\n", seqTiming[medianIndex]);
        System.out.printf(
                "Parallel median: %.3f Speedup from sequential: %.3f\n",
                parTiming[medianIndex], (seqTiming[medianIndex] / parTiming[medianIndex])
        );
        System.out.println("\nn = " + n);
    }

    public Oblig5(int run) {
        NPunkter17 pre = new NPunkter17(n);

        int[] x = new int[n];
        int[] y = new int[n];

        pre.fyllArrayer(x, y);

        int[] seqX = x.clone();
        int[] seqY = y.clone();

        int[] parX = x.clone();
        int[] parY = y.clone();

        IntList seqResX = new IntList();
        IntList seqResY = new IntList();
        IntList parResX = new IntList();
        IntList parResY = new IntList();

        // Do sequential tests
        System.out.println("Starting sequential");
        long startTime = System.nanoTime();
        seq(seqX, seqY, seqResX, seqResY);
        seqTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Sequential time: " + seqTiming[run] + "ms.");

        // Do parallel tests
        System.out.println("Starting Parallel");
        startTime = System.nanoTime();
        par(parX, parY, parResX, parResY);
        parTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Parallel time: " + parTiming[run] + "ms.");
    }

    void seq(int[] x, int[] y, IntList resX, IntList resY) {

    }

    void par(int[] x, int[] y, IntList resX, IntList resY) {

    }
}
