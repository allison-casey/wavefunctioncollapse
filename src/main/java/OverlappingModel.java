import java.util.*;

class OverlappingModel extends Model {
    int N;
    byte[][] patterns;
    int ground;
    List<Integer[]> colors;

    OverlappingModel(int[] data,
                            int dataWidth,
                            int dataHeight,
                            int N,
                            int width,
                            int height,
                            boolean periodicInput,
                            boolean periodicOutput,
                            int symmetry,
                            int ground) {
        super(width, height);
        this.N = N;
        this.periodic = periodicInput;

        int SMX = dataWidth, SMY = dataHeight;
        int[][] sample = new int[SMX][SMY];

        this.colors = new ArrayList<Integer[]>();

        for (int y = 0; y < SMY; y++)
            for (int x = 0; x < SMX; x++) {
                int indexPixel = (y * dataWidth + x) * 4;
                Integer[] color = {
                    data[indexPixel],
                    data[indexPixel + 1],
                    data[indexPixel + 2],
                    data[indexPixel + 3]
                };

                // Arrays.equals(arr1, arr2)
                for (Integer[] c : this.colors) {
                    if (!Arrays.equals(c, color))
                        i++;
                }

                if (i == this.colors.size())
                    colors.add(color);
                sample[x][y] = i;
            }
        int C = this.colors.size();
        int W = Math.pow(C, this.N * this.N);
   }


    protected boolean OnBoundary(int x, int y) {
        return false;
    }
}
