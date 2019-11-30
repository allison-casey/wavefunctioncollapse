

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.*;
import java.util.stream.IntStream;

public class SimpleTiledModel extends Model {
  List<Color[]> tiles;
  List<String> tilenames;
  int tilesize;
  boolean black;

  /**
   * 
   * @param tilesize
   * @param tilesData
   * @param neighborsData
   * @param subsetsData
   * @param tileData
   * @param subsetName
   * @param width
   * @param height
   * @param periodic
   * @param black
   * @param unique
   */
  public SimpleTiledModel(
    int tilesize,
    List<HashMap<String, String>> tilesData,
    List<HashMap<String, String>> neighborsData,
    HashMap<String, String[]> subsetsData,
    HashMap<String, BufferedImage> tileData,
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

    //    List<String> subset = null;
    //  C# Subset initilization if applicable
    //    if (subsetName && data.subsets && !!data.subsets[subsetName]) {
    //        subset = data.subsets[subsetName];
    //    }
    List<String> subset = null;
    if (
      subsetName != null &&
      subsetsData != null &&
      subsetsData.containsKey(subsetName)
    ) {
      subset = Arrays.asList(subsetsData.get(subsetName));
    }
    

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

    for (HashMap<String, String> xtile : tilesData) {
      String tilename = xtile.get("name");

      Function<Integer, Integer> a, b;
      int cardinality;

      String sym = xtile.getOrDefault("symmetry", "X");

      if (subset != null && !subset.contains(tilename)) continue;
      
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

      Integer[][] map = new Integer[cardinality][];
      for (int t = 0; t < cardinality; t++) {
        map[t] = new Integer[8];

        map[t][0] = t;
        map[t][1] = a.apply(t);
        map[t][2] = a.apply(a.apply(t));
        map[t][3] = a.apply(a.apply(a.apply(t)));
        map[t][4] = b.apply(t);
        map[t][5] = b.apply(a.apply(t));
        map[t][6] = b.apply(a.apply(a.apply(t)));
        map[t][7] = b.apply(a.apply(a.apply(a.apply(t))));

        for (int s = 0; s < 8; s++) map[t][s] += this.T;
        
        action.add(map[t]);
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
    this.weights = tempStationary.toArray(new Double[0]);

    this.propagator = new int[4][][];
    boolean[][][] tempPropagator = new boolean[4][][];
    for (int d = 0; d < 4; d++) {
      tempPropagator[d] = new boolean[this.T][];
      this.propagator[d] = new int[this.T][];
      for (int t = 0; t < this.T; t++) tempPropagator[d][t] =
        new boolean[this.T];
    }
    
    
    for (HashMap<String, String> xneighbor : neighborsData) {
    	
      String[] left = Arrays
        .stream(xneighbor.get("left").split(" "))
        .filter(x -> !x.isEmpty())
        .toArray(String[]::new);

      String[] right = Arrays
        .stream(xneighbor.get("right").split(" "))
        .filter(x -> !x.isEmpty())
        .toArray(String[]::new);
      
      if (
        subset != null &&
        (!subset.contains(left[0]) || !subset.contains(right[0]))
      ) continue;
      
      
      int L = action.get(firstOccurrence.get(left[0]))[left.length == 1 ? 0
          : Integer.valueOf(left[1])];
      int D = action.get(L)[1];

      int R = action.get(firstOccurrence.get(right[0]))[right.length == 1 ? 0
          : Integer.valueOf(right[1])];
      int U = action.get(R)[1];
      
      
      tempPropagator[0][R][L] = true;
      tempPropagator[0][action.get(R)[6]][action.get(L)[6]] = true;
      tempPropagator[0][action.get(L)[4]][action.get(R)[4]] = true;
      tempPropagator[0][action.get(L)[2]][action.get(R)[2]] = true;

      tempPropagator[1][U][D] = true;
      tempPropagator[1][action.get(D)[6]][action.get(U)[6]] = true;
      tempPropagator[1][action.get(U)[4]][action.get(D)[4]] = true;
      tempPropagator[1][action.get(D)[2]][action.get(U)[2]] = true;
    }
    
    for (int t2 = 0; t2 < this.T; t2++) for (int t1 = 0; t1 < this.T; t1++) {
      tempPropagator[2][t2][t1] = tempPropagator[0][t1][t2];
      tempPropagator[3][t2][t1] = tempPropagator[1][t1][t2];
    }

    ArrayList<ArrayList<ArrayList<Integer>>> sparsePropagator = new ArrayList<ArrayList<ArrayList<Integer>>>();

    for(int d = 0; d < 4; d++) {
    	sparsePropagator.add(d, new ArrayList<ArrayList<Integer>>());
    	for (int t = 0; t < this.T; t++) 
    		sparsePropagator.get(d).add(t, new ArrayList<Integer>());
    }

    for (int d = 0; d < 4; d++) for (int t1 = 0; t1 < this.T; t1++) {
      ArrayList<Integer> sp = sparsePropagator.get(d).get(t1);
      boolean[] tp = tempPropagator[d][t1];
            
      for (int t2 = 0; t2 < this.T; t2++) {
    	  if (tp[t2]) sp.add(t2);
      }
      

      int ST = sp.size();
      this.propagator[d][t1] = new int[ST];
      for (int st = 0; st < ST; st++) this.propagator[d][t1][st] = sp.get(st);
      
    }
  }

  @Override
  protected boolean onBoundary(int x, int y) {
    return !this.periodic && (x < 0 || y < 0 || x >= this.FMX || y >= this.FMY);
  }
  
  public String textOutput() {
	  StringBuilder result = new StringBuilder();
	  
	  for (int y = 0; y < this.FMY; y++) {
		  for (int x = 0; x < this.FMX; x++) 
			  result.append(String.format("{%s}, ", this.tilenames.get(this.observed[x + y * this.FMX])));
		  result.append("\n");
	  }
	  
	  return result.toString();
  }

  @Override
  public BufferedImage graphics() {
    BufferedImage result = new BufferedImage(
      this.FMX * this.tilesize,
      this.FMY * this.tilesize,
      BufferedImage.TYPE_INT_RGB
    );
    
//    System.out.println(this.observed);

    if (this.observed != null) {
      for (int x = 0; x < this.FMX; x++) for (int y = 0; y < this.FMY; y++) {
        Color[] tile = this.tiles.get(this.observed[x + y * this.FMX]);
        for (int yt = 0; yt < this.tilesize; yt++) for (int xt = 0; xt <
          this.tilesize; xt++) {
          Color c = tile[xt + yt * this.tilesize];
          result.setRGB(
            x * this.tilesize + xt,
            y * this.tilesize + yt,
            c.getRGB()
          );
        }
      }
    } else {
      for (int x = 0; x < this.FMX; x++) for (int y = 0; y < this.FMY; y++) {
        boolean[] a = this.wave[x + y * this.FMX];
        int amount = IntStream
          .range(0, a.length)
          .map(idx -> a[idx] ? 1 : 0)
          .sum();
        
        
        double lambda =
          1.0 /
            IntStream
              .range(0, this.T)
              .filter(idx -> a[idx])
              .mapToDouble(idx -> this.weights[idx])
              .sum();

        for (int yt = 0; yt < this.tilesize; yt++) for (int xt = 0; xt <
          this.tilesize; xt++) {
          if (this.black && amount == this.T) result.setRGB(
            x * this.tilesize + xt,
            y * this.tilesize * yt,
            (int) 0xff000000
          ); else {
            double r = 0, g = 0, b = 0;
            for (int t = 0; t < this.T; t++) if (a[t]) {
              Color c = this.tiles.get(t)[xt + yt * this.tilesize];
              r += c.getRed() * this.weights[t] * lambda;
              g += c.getGreen() * this.weights[t] * lambda;
              b += c.getBlue() * this.weights[t] * lambda;
            }

            Color newColor = new Color((int) r, (int) g, (int) b);
            result.setRGB(
              x * tilesize + xt,
              y * tilesize + yt,
              newColor.getRGB()
            );
          }
        }
      }
    }

    return result;
  }
}
