package shapecore.rendering;

import processing.core.PApplet;
import processing.core.PImage;

public class CoarseToFine {

  Renderable renderable;
  int coarseSize;
  int gridSize;
  
  public CoarseToFine(Renderable renderable) {
    this(renderable, 5);
  }
  
  public CoarseToFine(Renderable renderable, int coarseLevels) {
    this.renderable = renderable;
    this.coarseSize = (int) Math.pow(2, coarseLevels);
    this.gridSize = coarseSize;
  }
  
  public void renderStep(PImage image) {
    if(gridSize >= 1) {
      float halfGrid = gridSize/2f;
      image.loadPixels();
      int width = image.width;
      int height = image.height;
      // loop over cells
      for(int cellY = 0; cellY < height; cellY += gridSize) {
        for(int cellX = 0; cellX < width; cellX += gridSize) {
          int color = renderable.trace(cellX+halfGrid, cellY+halfGrid);
          int cellHeight = PApplet.min(gridSize, height-cellY);
          int cellWidth = PApplet.min(gridSize, width-cellX);
          // loop over each pixel in the cell
          int i = cellY*width + cellX;
          for(int y = 0; y < cellHeight; y++) {
            for(int x = 0; x < cellWidth; x++) {
              image.pixels[i] = color; 
              i++;
            }
            i += width-cellWidth;
          }
        }
      }
      image.updatePixels();
      if(gridSize == 1) {
        gridSize = 0;
      } else {
        gridSize /= 2;
      }
    }
  }
  
  /**
   * Do next render at the coarsest level
   */
  public void setCoarse() {
    gridSize = coarseSize;
  }
  
  /**
   * Do the next render at the finest level
   */
  public void setFine() {
    gridSize = 1;
  }
  
  public interface Renderable {
    int trace(float x, float y);
  }
}
