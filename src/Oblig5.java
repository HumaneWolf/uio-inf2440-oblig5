import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;

public class Oblig5 {

    static int k = 0;
    static int n = 0;

    static int MAX_X;
    static int MAX_Y;

    int[] x;
    int[] y;

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

    private Oblig5(int run) {
        NPunkter17 np = new NPunkter17(n);

        x = new int[n];
        y = new int[n];
        np.fyllArrayer(x, y);

        // Do sequential tests
        System.out.println("Starting sequential");
        long startTime = System.nanoTime();
        IntList seqRes = seq();
        seqTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("SEQ time: " + seqTiming[run] + "ms.");
        System.out.println("SEQ points: " + seqRes.len);

        // Do parallel tests
        System.out.println("Starting Parallel");
        startTime = System.nanoTime();
        IntList parRes = par();
        parTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("PAR time: " + parTiming[run] + "ms.");
        System.out.println("PAR points: " + seqRes.len);

        //new TegnUt(this, parRes);
    }

    private IntList seq() {
        IntList hull = new IntList();
        IntList upper = new IntList();
        IntList lower = new IntList();

        // Find the X extremes.
        int maxX = 0, minX = 0;
        for (int i = 1; i < x.length; i++) {
            if (x[maxX] < x[i]) {
                maxX = i;
            }
            if (x[minX] > x[i]) {
                minX = i;
            }
        }

        // Split points into two lists.
        for (int i = 0; i < x.length; i++) {
            if (i == maxX || i == minX) continue;

            if (getDistanceFromLine(i, maxX, minX) <= 0) {
                upper.add(i);
            }
            if (getDistanceFromLine(i, maxX, minX) >= 0) {
                lower.add(i);
            }
        }

        // Recursive calls
        seqRec(
                maxX,
                minX,
                getHighestDistance(maxX, minX, upper),
                upper,
                hull
        );
        hull.add(minX);

        seqRec(
                minX,
                maxX,
                getHighestDistance(minX, maxX, lower),
                lower,
                hull
        );

        hull.add(maxX);

        return hull;
    }

    private void seqRec(int p1, int p2, int p3, IntList list, IntList hull) {
        IntList outerPoints = getOuterPoints(p1, p3, list);
        int point = getHighestDistance(p1, p3, outerPoints);
        if (point != -1) {
            seqRec(
                    p1, p3, point,
                    outerPoints,
                    hull
            );
        }

        hull.add(p3);

        outerPoints = getOuterPoints(p3, p2, list);
        point = getHighestDistance(p3, p2, outerPoints);
        if (point != -1) {
            seqRec(
                    p3, p2, point,
                    outerPoints,
                    hull
            );
        }
    }

    private int getDistanceFromLine(int p3, int p1, int p2) {
        int a = y[p1] - y[p2];
        int b = x[p2] - x[p1];
        int c = y[p2] * x[p1] - y[p1] * x[p2];
        return a * x[p3] + b * y[p3] + c;
    }

    private int getHighestDistance(int p1, int p2, IntList list) {
        int extremePoint = -1;
        int extremeDistance = 1;

        int point;
        int tempDist;

        for (int i = 0; i < list.len; i++) {
            point = list.get(i);

            tempDist = getDistanceFromLine(point, p1, p2);
            if (tempDist < extremeDistance) {
                if (tempDist == 0 && !isBetweenOuterPoints(point, p1, p2))
                    continue;

                extremePoint = point;
                extremeDistance = tempDist;
            }
        }

        return extremePoint;
    }

    private IntList getOuterPoints(int p1, int p2, IntList list) {
        IntList outer = new IntList();
        int point;

        for (int i = 0; i < list.len; i++) {
            point = list.get(i);

            if (point == p1 || point == p2) continue;

            if (getDistanceFromLine(point, p1, p2) <= 0) {
                outer.add(point);
            }
        }

        return outer;
    }

    private boolean isBetweenOuterPoints(int p3, int p1, int p2){
        double distanceAB = Math.sqrt( Math.pow((x[p2] - x[p1]), 2) + Math.pow((y[p2] - y[p1]), 2) );
        double distanceAC = Math.sqrt( Math.pow((x[p3] - x[p1]), 2) + Math.pow((y[p3] - y[p1]), 2) );
        double distanceBC = Math.sqrt( Math.pow((x[p3] - x[p2]), 2) + Math.pow((y[p3] - y[p2]), 2) );

        if (distanceAB > distanceAC && distanceAB > distanceBC) {
            return true;
        }
        return false;
    }

    private IntList par() {
        IntList hull = new IntList();
        IntList[] localHulls = new IntList[k];
        IntList potentialHull = new IntList();
        Thread[] threads = new Thread[k];

        CyclicBarrier cb = new CyclicBarrier(k);

        // Run algorithm for the split version of the list.
        for (int i = 0; i < k; i++) {
            localHulls[i] = new IntList();
            threads[i] = new Thread(new Worker(i, localHulls[i]));
            threads[i].start();
        }

        // Wait for the threads to finish and combine local hulls.
        for (int i = 0; i < k; i++) {
            try {
                threads[i].join();

                potentialHull.append(localHulls[i]);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Find global hull.
        int point;
        // Find the X extremes.
        int maxX = potentialHull.get(0), minX = 0;
        for (int i = 1; i < potentialHull.len; i++) {
            point = potentialHull.get(i);

            if (x[maxX] < x[point]) {
                maxX = point;
            }
            if (x[minX] > x[point]) {
                minX = point;
            }
        }

        // Split points into two lists.
        IntList upper = new IntList();
        IntList lower = new IntList();
        for (int i = 0; i < potentialHull.len; i++) {
            point = potentialHull.get(i);

            if (point == maxX || point == minX) continue;

            if (getDistanceFromLine(point, maxX, minX) <= 0) {
                upper.add(point);
            }
            if (getDistanceFromLine(point, maxX, minX) >= 0) {
                lower.add(point);
            }
        }

        // Recursive calls
        seqRec(
                maxX,
                minX,
                getHighestDistance(maxX, minX, upper),
                upper,
                hull
        );
        hull.add(minX);

        seqRec(
                minX,
                maxX,
                getHighestDistance(minX, maxX, lower),
                lower,
                hull
        );

        hull.add(maxX);

        return hull;
    }

    private class Worker implements Runnable {
        int id;
        IntList hull;

        Worker(int id, IntList hull) {
            this.id = id;
            this.hull = hull;
        }

        @Override
        public void run() {
            // Find max and min X in the local points.
            int maxX = 0, minX = 0;
            for (int i = id; i < x.length; i += k) {
                if (x[maxX] < x[i]) {
                    maxX = i;
                }
                if (x[minX] > x[i]) {
                    minX = i;
                }
            }

            // Split into upper and lower lists.
            IntList upper = new IntList();
            IntList lower = new IntList();
            for (int i = id; i < x.length; i += k) {
                if (i == maxX || i == minX) continue;

                if (getDistanceFromLine(i, maxX, minX) <= 0) {
                    upper.add(i);
                }
                if (getDistanceFromLine(i, maxX, minX) >= 0) {
                    lower.add(i);
                }
            }

            // Recursive calls
            seqRec(
                    maxX,
                    minX,
                    getHighestDistance(maxX, minX, upper),
                    upper,
                    hull
            );
            hull.add(minX);

            seqRec(
                    minX,
                    maxX,
                    getHighestDistance(minX, maxX, lower),
                    lower,
                    hull
            );

            hull.add(maxX);
        }
    }
}
