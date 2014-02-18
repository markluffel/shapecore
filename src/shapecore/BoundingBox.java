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
  
  public static BoundingBox fromCenter(pt center, float width, float height) {
    BoundingBox b = new BoundingBox();
    b.minX = center.x-width/2;
    b.maxX = center.x+width/2;
    b.minY = center.y-height/2;
    b.maxY = center.y+height/2;    
    return b;
  }
  
  public BoundingBox(List<pt> pts) {
    this();
    add(pts);
  }
  
  public BoundingBox(pt[]... ptsArray) {
    this();
    
    for(pt[] pts : ptsArray) {
      for(pt p : pts) {
        add(p.x, p.y);
      }
    }
  }
  
  public BoundingBox(float[][]... ptsArray) {
    this();
    
    for(float[][] pts : ptsArray) {
      for(float[] p : pts) {
        add(p[0], p[1]);
      }
    }
  }

  public void intersect(float _minX, float _minY, float _maxX, float _maxY) {
    if(_minX > this.minX) this.minX = _minX;
    if(_minY > this.minY) this.minY = _minY;
    if(_maxX < this.maxX) this.maxX = _maxX;
    if(_maxY < this.maxY) this.maxY = _maxY;
    
    if(height() < 0 || width() < 0) setNull(); // non-intersecting
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

  public void add(pt p) {
    add(p.x, p.y);
  }
  
  public void add(float x, float y) {
    if(x > maxX) { maxX = x; }
    if(x < minX) { minX = x; }
    if(y > maxY) { maxY = y; }
    if(y < minY) { minY = y; }
  }

  public void add(List<pt> points) {
    for(pt p : points) {
      add(p.x, p.y);
    }
  }
  
  public void setNull() {
    minX = maxX = minY = maxY = 0;
  }

  public Affinity to(BoundingBox that) {
    // TODO: make this more efficient
    return Affinity.fit(this.asPolygon(), that.asPolygon());
  }

  public Affinity fitInside(BoundingBox that) {
    float scale = Math.min(
      that.width()/this.width(),
      that.height()/this.height()
    );
    return Affinity.makeSimilarity(scale, scale, 0, this.center(), that.center());
  }
}