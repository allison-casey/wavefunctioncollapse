import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.*;

public class SimpleTiledModel extends Model {
	int tilesize;
	boolean black; 

  public SimpleTiledModel(
    BufferedImage image,
    String subsetName,
    int width,
    int height,
    boolean periodic,
    boolean black
  ) {
    super(width, height);
    this.periodic = periodic;
    this.black = black;
    
  }
  
}
