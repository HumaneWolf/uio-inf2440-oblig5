import java.util.Arrays;

public class Oblig5Old {
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
            new Oblig5Old(i);

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

    public Oblig5Old(int run) {
        NPunkter17 pre = new NPunkter17(n);
        x = new int[n];
        y = new int[n];
        pre.fyllArrayer(x, y);

        IntList seqRes = new IntList();
        IntList parRes = new IntList();

        // Do sequential tests
        System.out.println("Starting sequential");
        long startTime = System.nanoTime();
        seq(seqRes);
        seqTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Sequential time: " + seqTiming[run] + "ms.");

        int len = seqRes.len;
        for (int i = 0; i < len; i++) {
            System.out.println(seqRes.get(i));
        }
        System.out.println("Antall: " + seqRes.len);

        new TegnUt(this, seqRes);

        if (true) return; //Temp breakpoint.

        // Do parallel tests
        System.out.println("Starting Parallel");
        startTime = System.nanoTime();
        par(parRes);
        parTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Parallel time: " + parTiming[run] + "ms.");
    }

    void seq(IntList res) {
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

        boolean[] pointFound = new boolean[n];

        IntList upperPoints = new IntList();
        IntList lowerPoints = new IntList();

        sortPoints(rightMost, leftMost, upperPoints, lowerPoints);

        seqRecurse(rightMost, leftMost, res, upperPoints, pointFound);
        seqRecurse(leftMost, rightMost, res, lowerPoints, pointFound);
    }

    void seqRecurse(int p1, int p2, IntList res, IntList points, boolean[] found) {
        int extremePoint = p1;
        int extremeDistance = 1;
        int a = getA(y[p1], y[p2]);
        int b = getB(x[p1], x[p2]);
        int c = getC(x[p1], y[p1], x[p2], y[p2]);
        int tempDist;
        int point;

        // Search for a point.
        for (int i = 0; i < points.len; i++) {
            point = points.get(i);

            if (i == p1 || i == p2) continue;

            tempDist = getDistanceFromLine(a, b, c, x[i], y[i]);
            if (tempDist <= 0 && tempDist < extremeDistance) {
                extremePoint = i;
                extremeDistance = tempDist;
            } else if (tempDist <= 0 && tempDist == extremeDistance && !found[i]) {
                extremePoint = i;
                extremeDistance = tempDist;
            }
        }

        // If we found a point.
        if (extremePoint != p1 && !found[extremePoint]) {
            System.out.println("For " + p1 + " and " + p2 + " found " + extremePoint + " at " + extremeDistance);

            found[extremePoint] = true;

            seqRecurse(p1, extremePoint, res, points, found);
            seqRecurse(extremePoint, p2, res, points, found);
        } else {
            // Add point to hull.
            res.add(p2);
            System.out.println("For " + p1 + " and " + p2 + " found nothing");
        }
    }

    void par(IntList res) {
        //
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

    private void sortPoints(int p1, int p2, IntList upper, IntList lower) {
        int a = getA(y[p1], y[p2]);
        int b = getB(x[p1], x[p2]);
        int c = getC(x[p1], y[p1], x[p2], y[p2]);

        for (int i = 0; i < x.length; i++) {
            if (i != p1 && i != p2 && getDistanceFromLine(a, b, c, x[i], y[i]) <= 0) {
                upper.add(i);
            }

            if (i != p1 && i != p2 && getDistanceFromLine(a, b, c, x[i], y[i]) >= 0) {
                lower.add(i);
            }
        }
    }
}
