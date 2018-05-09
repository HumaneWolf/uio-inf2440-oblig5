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
        k = Integer.parseInt(args[1]);

        MAX_X = Math.max(10,(int) Math.sqrt(n) * 3); // Same as in NPunkter.
        MAX_Y = MAX_X;

        for (int i = 0; i < runs; i++) {
            new Oblig5(i);

            if (true) return; //Temp breakpoint.
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

        x = new int[n];
        y = new int[n];

        pre.fyllArrayer(x, y);

        IntList seqRes = new IntList();
        IntList parRes = new IntList();

        // Do sequential tests
        System.out.println("Starting sequential");
        long startTime = System.nanoTime();
        seq(x, y, seqRes);
        seqTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Sequential time: " + seqTiming[run] + "ms.");

        int len = seqRes.len;
        for (int i = 0; i < len; i++) {
            System.out.println(seqRes.get(i));
        }

        new TegnUt(this, seqRes);

        if (true) return; //Temp breakpoint.

        // Do parallel tests
        System.out.println("Starting Parallel");
        startTime = System.nanoTime();
        par(x, y, parRes);
        parTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Parallel time: " + parTiming[run] + "ms.");
    }

    void seq(int[] x, int[] y, IntList res) {
        int leftMost = 0;
        int rightMost = 0;

        for (int i = 1; i < x.length; i++) {
            if (x[i] < x[leftMost]) {
                leftMost = i;
            }
            if (x[i] > x[rightMost]) {
                rightMost = i;
            }
        }
        if (leftMost == rightMost) {
            System.out.println("Leftmost cannot be equal rightmost.");
            System.exit(0);
        }

        res.add(leftMost);
        seqRecurse(leftMost, rightMost, x, y, res);
        //seqRecurse(rightMost, leftMost, x, y, res); // TODO: Fix overflow error.
    }

    void seqRecurse(int p1, int p2, int[] x, int[] y, IntList res) {
        int extremePoint = p1;
        int extremeDistance = 1;
        int a = getA(y[p1], y[p2]);
        int b = getB(x[p1], x[p2]);
        int c = getC(x[p1], y[p1], x[p2], y[p2]);
        int tempDist;

        // Search for a point.
        for (int i = 0; i < x.length; i++) {
            if (i == p1 || i == p2) continue;

            tempDist = getDistanceFromLine(a, b, c, x[i], y[i]);
            if (tempDist <= 0 && tempDist < extremeDistance) {
                extremePoint = i;
                extremeDistance = tempDist;
            }
        }

        // If we found a point.
        if (extremePoint != p1) {
            seqRecurse(p1, extremePoint, x, y, res);
            seqRecurse(extremePoint, p2, x, y, res);
        }

        // Add second point.
        res.add(p2);
    }

    void par(int[] x, int[] y, IntList res) {

    }

    /*
     *  Helper functions to handle the distance from line equation.
     */

    /**
     * Get the A part of the equation defining a line.
     * @param y1 Y coord of point 1.
     * @param y2 Y coord of point 2.
     * @return A.
     */
    int getA(int y1, int y2) {
        return y1 - y2;
    }

    /**
     * Get the B prt of the equation defining a line.
     * @param x1 X coord of point 1.
     * @param x2 X coord of point 2.
     * @return B.
     */
    int getB(int x1, int x2) {
        return x2 - x1;
    }

    /**
     * Get the C part of the equation defining a line.
     * @param x1 X coord of point 1.
     * @param y1 Y coord of point 1.
     * @param x2 X coord of point 2.
     * @param y2 Y coord of point 2-
     * @return The C part of the equation.
     */
    int getC(int x1, int y1, int x2, int y2) {
        return (y2 * x1) - (y1 * x2);
    }

    /**
     * Get the relative distance between a point and a line.
     * Also check the getA, getB, and getC methods.
     * @param a the A variable defining the line.
     * @param b the B variable defining the line.
     * @param c the C variable defining the line.
     * @param x The X coord of the point.
     * @param y the Y coord of the point.
     * @return The relative distance from the line.
     */
    int getDistanceFromLine(int a, int b, int c, int x, int y) {
        return (a * x) + (b * y) + c;
    }
}
