public class Oblig5 {

    static int k = 0;
    static int n = 0;

    static int MAX_X;
    static int MAX_Y;

    int[] x;
    int[] y;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("SYNTAX: java Oblig5 [antall punkter] [antall trader]");
            return;
        }

        n = Integer.parseInt(args[0]);
        k = Integer.parseInt(args[1]);

        MAX_X = Math.max(10,(int) Math.sqrt(n) * 3); // Same as in NPunkter.
        MAX_Y = MAX_X;

        new Oblig5();
    }

    private Oblig5() {
        NPunkter17 np = new NPunkter17(n);

        x = new int[n];
        y = new int[n];
        np.fyllArrayer(x, y);

        IntList seqRes = seq();

        for (int i = 0; i < seqRes.len; i++) {
            System.out.println(seqRes.get(i));
        }

        System.out.println("Points: " + seqRes.len);

        //new TegnUt(this, seqRes);
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

        if(distanceAB > distanceAC && distanceAB > distanceBC){
            return true;
        }
        return false;
    }
}
