package wavefunctioncollapse;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class Main {

  public static void main(String[] args) {
    try {
      Image knot = ImageIO.read(new File("lair3.png"));
      BufferedImage image_buffered = (BufferedImage) knot;
      Random random = new Random();

      OverlappingModel model = new OverlappingModel(
        image_buffered,
        3,
        125,
        125,
        true,
        false,
        8,
        0
      );
      boolean finished = model.Run(random.nextInt(), 0);

      System.out.println("Finished: " + finished);

      BufferedImage output = model.graphics();

      File output_file = new File("image_out.png");
      ImageIO.write(output, "png", output_file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  //		OverlappingModel model = new OverlappingModel(null, 0, 0, 0, false, false, 0, 0);
  }
}
