/**
 * 
 */
package shapecore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoundingBox {
  public float minX, minY, maxX, maxY;
  
  public BoundingBox() {
    minX = Float.MAX_VALUE;
    minY = Float.MAX_VALUE;
    maxX = -Float.MAX_VALUE;
    maxY = -Float.MAX_VALUE;
  }
  
  public BoundingBox(float minX, float minY, float maxX, float maxY) {
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
  }
  
  public BoundingBox(List<pt> pts) {
    this();
    add(pts);
  }
  
  public BoundingBox(pt[]... ptsArray) {
    this();
    
    for(pt[] pts : ptsArray) {
      add(Arrays.asList(pts));
    }
  }
  
  public BoundingBox(float[][]... ptsArray) {
    this();
    
    for(float[][] pts : ptsArray) {
      for(float[] p : pts) {
        float x = p[0], y = p[1];
        if(x > maxX) { maxX = x; }
        if(x < minX) { minX = x; }
        if(y > maxY) { maxY = y; }
        if(y < minY) { minY = y; }
      }
    }
  }

  @Deprecated // look at the callers of this, figure out if this is really what they want
  // the implementation looks like nonsense
  public void intersect(float minX, float minY, float maxX, float maxY) {
    if(minX < this.minX) this.minX = minX;
    if(minY < this.minY) this.minY = minY;
    if(maxX < this.maxX) this.maxX = maxX;
    if(maxY < this.maxY) this.maxY = maxY;
  }
  
  public void pad(float padding) {
    this.minX -= padding;
    this.minY -= padding;
    this.maxX += padding;
    this.maxY += padding;
  }
  
  public boolean contains(float x, float y) {
    return x >= minX && y >= minY && x <= maxX && y <= maxY;
  }
  
  public boolean contains(pt p) {
    return contains(p.x,p.y);
  }

  public boolean intersects(BoundingBox that) {
    boolean xOverlap = this.maxX > that.minX && this.minX < that.maxX;
    boolean yOverlap = this.maxY > that.minY && this.minY < that.maxY;
    return xOverlap && yOverlap;
  }

  public pt sample() {
    return new pt(Math.random()*(maxX-minX)+minX, Math.random()*(maxY-minY)+minY);
  }

  /**
   * Clockwise in screen coords
   * @return
   */
  public List<pt> asPolygon() {
    List<pt> polygon = new ArrayList<pt>();
    polygon.add(upperLeft());
    polygon.add(upperRight());
    polygon.add(lowerRight());
    polygon.add(lowerLeft());
    return polygon;
  }
  
  public pt upperLeft()  { return new pt(minX, minY); }
  public pt upperRight() { return new pt(maxX, minY); }
  public pt lowerLeft()  { return new pt(minX, maxY); }
  public pt lowerRight() { return new pt(maxX, maxY); }

  public float width() {
    return maxX-minX;
  }

  public float height() {
    return maxY-minY;
  }

  public pt center() {
    return new pt((maxX+minX)/2, (maxY+minY)/2);
  }

  public void add(List<pt> points) {
    for(pt p : points) {
      float x = p.x, y = p.y;
      if(x > maxX) { maxX = x; }
      if(x < minX) { minX = x; }
      if(y > maxY) { maxY = y; }
      if(y < minY) { minY = y; }
    }
  }
  
}