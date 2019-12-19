package com.github.sjcasey21.wavefunctioncollapse;

import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.io.File;
//import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import com.github.sjcasey21.wavefunctioncollapse.*;

public class Main {

	public static String getAttributeWithDefault(Element elem, String key, String d) {
		if (elem.hasAttribute(key))
			return elem.getAttribute(key);
		else
			return d;
	}
	
	static List<Map<String, String>> initTilesData() {
	      List<Map<String, String>> tilesData = new ArrayList();
	      String[] pairs = new String[] {
	    		  "corner", "L",
	    		  "cross", "I",
	    		  "empty", "X",
	    		  "line", "I",
	    		  "t", "T"
	      };
	      
	      for(int i = 0; i < pairs.length; i += 2) {
	    	  int index = i / 2;
	    	  tilesData.add(new HashMap<String, String>());
	    	  
	    	  tilesData.get(index).put("name", pairs[i]);
	    	  tilesData.get(index).put("symmetry", pairs[i + 1]);
	      }
	      

	      
	      return tilesData;
	}
	
	static List<Map<String, String>> initNeighborsData() {
		List<Map<String, String>> neighborsData = new ArrayList();
	      String[] pairs = new String[] {
	    		  "corner 1", "empty",
	    		  "corner", "cross",
	    		  "corner", "cross 1",
	    		  "corner", "line",
	    		  "corner 1", "line 1",
	    		  "corner", "t 2",
	    		  "corner", "t 3",
	    		  "corner", "t",
	    		  "corner 1", "t 1",
	    		  "corner 1", "corner 3",
	    		  "corner 1", "corner",
	    		  "corner", "corner 1",
	    		  "corner", "corner 2",
	    		  "cross", "cross",
	    		  "cross", "cross 1",
	    		  "cross 1", "cross 1",
	    		  "cross", "line",
	    		  "cross 1", "line",
	    		  "cross", "t",
	    		  "cross", "t 3",
	    		  "cross 1", "t",
	    		  "cross 1", "t 3",
	    		  "empty", "empty",
	    		  "empty", "line 1",
	    		  "empty", "t 1",
	    		  "line", "line",
	    		  "line 1", "line 1",
	    		  "line", "t",
	    		  "line 1", "t 1",
	    		  "line", "t 3",
	    		  "t 1", "t 3",
	    		  "t", "t",
	    		  "t 2", "t",
	    		  "t 1", "t",
	    		  "t 3", "t 1"
	      };
	      
	      for(int i = 0; i < pairs.length; i += 2) {
	    	  int index = i / 2;
	    	  neighborsData.add(new HashMap<String, String>());
	    	  
	    	  neighborsData.get(index).put("left", pairs[i]);
	    	  neighborsData.get(index).put("right", pairs[i + 1]);
	      }
	      
//	      for (HashMap<String, String> x : neighborsData) 
//	    	  System.out.println(x);
	      
	      return neighborsData;
	}
	
	static BufferedImage loadImage(String path) throws IOException {
		Image knot = ImageIO.read(new File(path));
		return (BufferedImage) knot;
	}
	
	static void runTiledModel() {
		String subset = "Dense Fabric";
	    int width = 32;
	    int height = 32;
	    boolean periodic = true;

	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    try {
	      int tilesize = 10;
	      
	      List<Map<String, String>> tiles = initTilesData();
	      List<Map<String, String>> neighborsData = initNeighborsData();
	      Map<String, String[]> subsetsData = new HashMap<String, String[]>();
	      subsetsData.put("Standard", new String[] {"corner", "cross", "empty", "line", "t"});
	      subsetsData.put("Crossless", new String[] {"corner", "empty", "line",});
	      subsetsData.put("TE", new String[] { "empty", "t"});
	      subsetsData.put("T", new String[] {"t"});
	      subsetsData.put("CL", new String[] {"corner", "line" });
	      subsetsData.put("CE", new String[] {"corner","empty", });
	      subsetsData.put("C", new String[] {"corner"});
	      subsetsData.put("Fabric", new String[] {"cross","line"});
	      subsetsData.put("Dense Fabric", new String[] {"cross" });
	      subsetsData.put("Dense", new String[] {"corner", "cross", "line"});

	      
	      Map<String, BufferedImage> tileData = new HashMap<String, BufferedImage>();
	      String[] tileNames = new String[] {
	    		  "corner",
	    		  "cross",
	    		  "empty",
	    		  "line",
	    		  "t"
	      };
	      for(String tile : tileNames) {
	    	  String path = "./knot/" + tile + ".png";    	
	    	  tileData.put(tile, loadImage(path));
	      }
	      Random random = new Random();
	      
	      SimpleTiledModel tiled_model = new SimpleTiledModel(
	        tilesize,
	        tiles,
	        neighborsData,
	        subsetsData,
	        tileData,
	        subset,
	        width,
	        height,
	        periodic,
	        false,
	        false
	      );
	      
	      Boolean finished = tiled_model.run(random.nextInt(), 0);
	      
	      System.out.println(String.format("Finished %s", finished));
	      
	      BufferedImage output = tiled_model.graphics();
	      
	      File output_file = new File("image_out.png");
	      ImageIO.write(output, "png", output_file);
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	}
	
	static void runOverlappingModel() {
	    try {
			BufferedImage image_buffered = ImageIO.read(new File("Flowers.png"));
			Random random = new Random();

			OverlappingModel model = new OverlappingModel(
			  image_buffered,
			  3,
			  32,
			  32,
			  true,
			  false,
		  2,
			102
			);
			boolean finished = model.run(random.nextInt(), 0);

			System.out.println("Finished: " + finished);

			BufferedImage output = model.graphics();

			File output_file = new File("image_out.png");
			ImageIO.write(output, "png", output_file);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	}
	
  public static void main(String[] args) {
//    runTiledModel();
    runOverlappingModel();
  }
}
