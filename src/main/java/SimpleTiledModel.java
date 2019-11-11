import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.*;

public class SimpleTiledModel extends Model {
  List<Color[]> tiles;
  List<String> tilenames;
  int tilesize;
  boolean black;

  /**
   *
   * @param tilesize
   * @param tiles
   * @param neighbors
   * @param tileData
   * @param path
   * @param subsetName
   * @param width
   * @param height
   * @param periodic
   * @param black
   */
  public SimpleTiledModel(
    int tilesize,
    HashMap<String, String>[] tiles,
    HashMap<String, String>[] neighbors,
    HashMap<String, String> subsets,
    HashMap<String, BufferedImage> tileData,
    String path,
    String subsetName,
    int width,
    int height,
    boolean periodic,
    boolean black,
    boolean unique
  ) {
    super(width, height);
    this.periodic = periodic;
    this.black = black;
    this.tilesize = tilesize;

    List<String> subset = null;

    //  C# Subset initilization if applicable
    //    if (subsetName && data.subsets && !!data.subsets[subsetName]) {
    //        subset = data.subsets[subsetName];
    //    }
    Function<BiFunction<Integer, Integer, Color>, Color[]> tile =
      (BiFunction<Integer, Integer, Color> f) -> {
        Color[] result = new Color[this.tilesize * this.tilesize];
        for (int y = 0; y < this.tilesize; y++) for (int x = 0; x <
          this.tilesize; x++) result[x + y * tilesize] = f.apply(x, y);
        return result;
      };

    Function<Color[], Color[]> rotate =
      (Color[] array) -> tile.apply(
        (Integer x, Integer y) -> array[this.tilesize -
            1 -
            y +
            x *
            this.tilesize]
      );

    this.tiles = new ArrayList<Color[]>();
    this.tilenames = new ArrayList<String>();
    List<Double> tempStationary = new ArrayList<Double>();

    List<Integer[]> action = new ArrayList<Integer[]>();
    HashMap<String, Integer> firstOccurrence = new HashMap<String, Integer>();

    for (HashMap<String, String> xtile : tiles) {
      String tilename = xtile.get("name");

      Function<Integer, Integer> a, b;
      int cardinality;

      String sym = xtile.getOrDefault("symmetry", "X");

      // if (subset != null && !subset.Contains(tilename)) continue;
      switch (sym) {
        case "L":
          cardinality = 4;
          a = (Integer i) -> (i + 1) % 4;
          b = (Integer i) -> (i % 2) == 0 ? i + 1 : i - 1;
          break;
        case "T":
          cardinality = 4;
          a = (Integer i) -> (i + 1) % 4;
          b = (Integer i) -> (i % 2) == 0 ? i : 4 - i;
          break;
        case "I":
          cardinality = 2;
          a = (Integer i) -> 1 - i;
          b = (Integer i) -> i;
          break;
        case "\\":
          cardinality = 2;
          a = (Integer i) -> 1 - i;
          b = (Integer i) -> 1 - i;
          break;
        default:
          cardinality = 1;
          a = (Integer i) -> i;
          b = (Integer i) -> i;
          break;
      }

      this.T = action.size();
      firstOccurrence.put(tilename, this.T);

      int[][] map = new int[cardinality][];
      for (int t = 0; t < cardinality; t++) {
        map[t] = new int[8];

        map[t][0] = t;
        map[t][1] = a.apply(t);
        map[t][2] = a.apply(a.apply(t));
        map[t][3] = a.apply(a.apply(a.apply(t)));
        map[t][4] = b.apply(t);
        map[t][5] = b.apply(a.apply(t));
        map[t][6] = b.apply(a.apply(a.apply(t)));
        map[t][7] = b.apply(a.apply(a.apply(a.apply(t))));

        for (int s = 0; s < 8; s++) map[t][s] += this.T;
      }

      if (unique) {
        for (int t = 0; t < cardinality; t++) {
          BufferedImage xtileData = tileData.get(tilename);
          this.tiles.add(
              tile.apply(
                (Integer x, Integer y) -> new Color(xtileData.getRGB(x, y))
              )
            );
          this.tilenames.add(String.format("%s %s", tilename, t));
        }
      } else {
        BufferedImage xtileData = tileData.get(tilename);
        this.tiles.add(
            tile.apply(
              (Integer x, Integer y) -> new Color(xtileData.getRGB(x, y))
            )
          );
        this.tilenames.add(String.format("%s 0", tilename));

        for (int t = 1; t < cardinality; t++) {
          this.tiles.add(rotate.apply(this.tiles.get(this.T + t - 1)));
          this.tilenames.add(String.format("%s %s", tilename, t));
        }
      }

      for (int t = 0; t < cardinality; t++) tempStationary.add(
        Double.valueOf(xtile.getOrDefault("weight", "1.0"))
      );
    }
    
    this.T = action.size();
    this.weights = tempStationary.toArray();
  }

  @Override
  protected boolean onBoundary(int x, int y) {
    return !this.periodic && (x < 0 || y < 0 || x >= this.FMX || y >= this.FMY);
  }

  @Override
  public BufferedImage graphics() {
    // TODO Auto-generated method stub
    return null;
  }
}
