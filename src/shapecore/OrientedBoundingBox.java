/**
 * 
 */
package shapecore;

import java.util.ArrayList;

import java.util.List;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

public class OrientedBoundingBox {
  pt center;
  public vec U,V; // basis
  // contains all points: center + U x + V y, such that x = [-1,1], y = [-1,1]
  
  // uh, why have the centroid? aren't we just ignoring it?
  public OrientedBoundingBox(pt c, vec u, List<pt> pts) {
    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE;
    float maxY = -Float.MAX_VALUE;
    
    U = U(u);
    V = R(U);
    
    for(pt p : pts) {
      vec cp = V(c,p);
      float x = dot(U, cp);
      float y = dot(V, cp);
      if(x > maxX) { maxX = x; }
      if(x < minX) { minX = x; }
      if(y > maxY) { maxY = y; }
      if(y < minY) { minY = y; }
    }
    
    float
    uLen = (abs(minX)+abs(maxX))/2,
    vLen = (abs(minY)+abs(maxY))/2,
    shiftX = maxX - uLen,
    shiftY = maxY - vLen;
    center = T(c, shiftX, U, shiftY, V);
    U.scaleBy(uLen);
    V.scaleBy(vLen);
  }
  
  public OrientedBoundingBox(vec u, List<pt> pts) {
    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE;
    float maxY = -Float.MAX_VALUE;
    
    U = U(u);
    V = R(U);
    
    pt zero = P();
    for(pt p : pts) {
      vec cp = V(zero,p);
      float x = dot(U, cp);
      float y = dot(V, cp);
      if(x > maxX) { maxX = x; }
      if(x < minX) { minX = x; }
      if(y > maxY) { maxY = y; }
      if(y < minY) { minY = y; }
    }
    
    center = T(zero, (maxX+minX)/2, U, (maxY+minY)/2, V);
    U.scaleBy((maxX-minX)/2);
    V.scaleBy((maxY-minY)/2);
  }
    
  public boolean contains(float x, float y) {
    return contains(P(x,y));
  }
  
  public boolean contains(pt p) {
    vec cp = V(center,p);
    float x = dot(U, cp);
    float y = dot(V, cp);
    return x < U.norm2() && y < V.norm2();
  }

  /**
   * Clockwise in screen coords
   * @return
   */
  public List<pt> asPolygon() {
    List<pt> polygon = new ArrayList<pt>();
    polygon.add(upperRight());
    polygon.add(lowerRight());
    polygon.add(lowerLeft());
    polygon.add(upperLeft());
    return polygon;
  }
  
  public pt upperRight() { return T(center, +1, U, +1, V); }
  public pt lowerRight() { return T(center, +1, U, -1, V); }
  public pt lowerLeft()  { return T(center, -1, U, -1, V); }
  public pt upperLeft()  { return T(center, -1, U, +1, V); } 

  public float width() {
    return U.norm()*2;
  }

  public float height() {
    return V.norm()*2;
  }

  public pt center() {
    return center;
  }

  public Frame frame() {
    return Frame.make(center, U.angle());
  }
}