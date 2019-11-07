import java.lang.Math;
import java.util.Random;

class StackEntry {
    private int x;
    private int y;

    public StackEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getFirst() {
        return this.x;
    }

    public int getSecond() {
        return this.y;
    }
}

abstract class Model {
    protected boolean[][] wave;
    protected int[][][] propagator;
    int[][][] compatible;
    protected int[] observed;

    StackEntry[] stack;
    int stacksize;

    protected Random random;
    protected int FMX, FMY, T;
    protected boolean periodic;
    protected double[] weights;
    double[] weightLogWeights;

    int[] sumsOfOnes;
    double sumOfWeights, sumOfWeightLogWeights, startingEntropy;
    double[] sumsOfWeights, sumsOfWeightLogWeights, entropies;


    protected Model(int width, int height) {
        this.FMX = width;
        this.FMY = height;
    }

    protected abstract boolean OnBoundary(int x, int y);
    // public abstract System.Drawing.Bitmap Graphics() <- C#

    protected static int[] DX = {-1, 0, 1, 0};
    protected static int[] DY = {0, 1, 0, -1};
    static int[] oppposite = {2, 3, 0, 1};

    static int randomIndice(double[] arr, double r) {
        double sum = 0;
        double x = 0;
        int i;

        for (i = 0; i < arr.length; i++)
            sum += arr[i];

        i = 0;
        double rr = r * sum;
        while (i < arr.length) {
            x += arr[i];
            if (rr <= x) return i;
            i++;
        }

        return 0;

    }

    void Init() {
        this.wave = new boolean[this.FMX * this.FMY][];
        this.compatible = new int[this.wave.length][][];
        for (int i = 0; i < wave.length; i++) {
            this.wave[i] = new boolean[this.T];
            this.compatible[i] = new int[this.T][];
            for (int t = 0; t < this.T; t++) this.compatible[i][t] = new int[4];
        }

        this.weightLogWeights = new double[this.T];
        this.sumOfWeights = 0;
        this.sumOfWeightLogWeights = 0;

        for (int t = 0; t < this.T; t++) {
            this.weightLogWeights[t] = this.weights[t] * Math.log(this.weights[t]);
            this.sumOfWeights += this.weights[t];
            this.sumOfWeightLogWeights += this.weightLogWeights[t];
        }

        this.startingEntropy = Math.log(this.sumOfWeights)
            - this.sumOfWeightLogWeights / this.sumOfWeights;

        this.sumsOfOnes = new int[this.FMX * this.FMY];
        this.sumsOfWeights = new double[this.FMX * this.FMY];
        this.sumsOfWeightLogWeights = new double[this.FMX * this.FMY];
        this.entropies = new double[this.FMX * this.FMY];
        this.stack = new StackEntry[this.wave.length * this.T];
        this.stacksize = 0;
    }

    Boolean Observe() {
        double min = 1e3;
        int argmin = -1;

        for (int i = 0; i < this.wave.length; i++) {
            if (this.OnBoundary(i % this.FMX, i / this.FMX)) continue;

            int amount = this.sumsOfOnes[i];
            if (amount == 0) return false;

            double entropy = this.entropies[i];
            if (amount > 1 && entropy <= min) {
                double noise = 1e-6 * this.random.nextDouble();
                if (entropy  + noise < min) {
                    min = entropy + noise;
                    argmin = i;
                }
            }
        }

        if (argmin == -1) {
            this.observed = new int[this.FMX * this.FMY];
            for (int i = 0; i < this.wave.length; i++)
                for (int t = 0; t < this.T; t++)
                    if (this.wave[i][t]) {
                        this.observed[i] = t; break;
                    }
            return true;
        }

        double[] distribution = new double[this.T];
        for (int t = 0; t < this.T; t++)
            distribution[t] = this.wave[argmin][t] ? this.weights[t] : 0;
        int r = this.randomIndice(distribution, this.random.nextDouble());

        boolean[] w = this.wave[argmin];
        for (int t = 0; t < this.T; t++)
            if (w[t] != (t == r))
                this.Ban(argmin, t);


        return null;
    }

    protected void Ban(int i, int t) {
        this.wave[i][t] = false;

        int[] comp = this.compatible[i][t];
        for(int d = 0; d < 4; d++) comp[d] = 0;
        this.stack[this.stacksize] = new StackEntry(i, t);
        this.stacksize++;

        this.sumsOfOnes[i] -= 1;
        this.sumsOfWeights[i] -= this.weights[t];
        this.sumsOfWeightLogWeights[i] -= this.weightLogWeights[t];

        double sum = this.sumsOfWeights[i];
        this.entropies[i] = Math.log(sum) - this.sumsOfWeightLogWeights[i] / sum;
    }

    protected void Propagate() {
        while (this.stacksize > 0) {
            StackEntry e1 = this.stack[this.stacksize - 1];
            this.stacksize--;

            int i1 = e1.getFirst();
            int x1 = i1 % this.FMX;
            int y1 = i1 / this.FMX;

            for (int d = 0; d < 4; d++) {
                int dx = this.DX[d], dy = this.DY[d];
                int x2 = x1 + dx, y2 = y1 + dx;

                if (this.OnBoundary(x2, y2)) continue;

                if (x2 < 0) x2 += this.FMX;
                else if (x2 >= this.FMX) x2 -= this.FMX;
                if (y2 < 0) y2 += this.FMY;
                else if (y2 >= this.FMY) y2 -= this.FMY;

                int i2 = x2 + y2 * this.FMX;
                int[] p = this.propagator[d][e1.getSecond()];
                int[][] compat = this.compatible[i2];

                for (int l = 0; l < p.length; l++) {
                    int t2 = p[l];
                    int[] comp = compat[t2];

                    comp[d]--;
                    if (comp[2] == 0) this.Ban(i2, t2);
                }
            }
        }
    }

    public boolean Run(int seed, int limit) {
        if (this.wave == null) this.Init();

        this.Clear();
        this.random = new Random(seed);

        for (int l = 0; l < limit || limit == 0; l++) {
            Boolean result = this.Observe();
            if (result != null) return (boolean) result;
            this.Propagate();
        }

        return true;
    }



    protected void Clear() {
        for (int i = 0; i < this.wave.length; i++) {
            for (int t = 0; t < this.T; t++) {
                this.wave[i][t] = true;
                for (int d =0; d < 4; d++)
                    this.compatible[i][t][d] =
                        this.propagator[this.oppposite[d]][t].length;
            }

            this.sumsOfOnes[i] = this.weights.length;
            this.sumsOfWeights[i] = this.sumOfWeights;
            this.sumsOfWeightLogWeights[i] = this.sumOfWeightLogWeights;
            this.entropies[i] = this.startingEntropy;
        }
    }
}
