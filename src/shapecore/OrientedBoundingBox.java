/**
 * 
 */
package shapecore;

import static processing.core.PApplet.*;
import static shapecore.Geometry.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrientedBoundingBox implements Serializable {
  pt center;
  public vec U,V; // basis
  static final long serialVersionUID = -33l;
  // contains all points: center + U x + V y, such that x = [-1,1], y = [-1,1]
  
  public OrientedBoundingBox() {
    U = new vec(0,0);
    V = new vec(0,0);
    center = new pt(0,0);
  }
  
  public OrientedBoundingBox(vec u, List<pt> pts) {
    U = U(u);
    V = R(U);
    
    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE;
    float maxY = -Float.MAX_VALUE;
    for(pt p : pts) {
      vec vp = V(p);
      float x = U.dot(vp);
      float y = V.dot(vp);
      if(x > maxX) { maxX = x; }
      if(x < minX) { minX = x; }
      if(y > maxY) { maxY = y; }
      if(y < minY) { minY = y; }
    }
    recenter(minX, minY, maxX, maxY);
  }
  
  void recenter(float minX, float minY, float maxX, float maxY) {
    center = T(P(0,0), (maxX+minX)/2, U, (maxY+minY)/2, V);
    U.scaleBy((maxX-minX)/2);
    V.scaleBy((maxY-minY)/2);
  }
  
  public boolean contains(float x, float y) {
    return contains(P(x,y));
  }
  
  public boolean contains(pt p) {
    vec cp = V(center,p);
    float x = U.dot(cp);
    float y = V.dot(cp);
    return x < U.norm2() && y < V.norm2();
  }
  
  public OrientedBoundingBox pad(float padding) {
    U.toLength(U.norm()+padding);
    V.toLength(V.norm()+padding);
    return this;
  }

  /**
   * Clockwise in screen coords
   * @return
   */
  public List<pt> corners() {
    List<pt> polygon = new ArrayList<pt>();
    polygon.add(upperRight());
    polygon.add(lowerRight());
    polygon.add(lowerLeft());
    polygon.add(upperLeft());
    return polygon;
  }
  public Polygon asPolygon() {
    return new Polygon(new pt[]{
      upperRight(),
      lowerRight(),
      lowerLeft(),
      upperLeft()
    });
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
  
  /** Square radius of bounding circle */
  float sqradius() { return U.get().add(V).sqnorm(); }
  float radius() { return sqrt(sqradius()); }

  public boolean intersects(OrientedBoundingBox that) {
    float d = this.center.disTo(that.center);
    if(d > this.radius()+that.radius()) {
      return false;
    } else {
      // TODO: could also check the points of one against the halfspaces of the other
      List<pt> thisPoly = this.corners();
      List<pt> thatPoly = that.corners();
      // does one contain any of the points of the others?
      for(pt p : thisPoly) if(that.contains(p)) return true;
      for(pt p : thatPoly) if(this.contains(p)) return true;
      // 16 combinations
      for(Edge thisE : Polygon.edges(thisPoly)) {
        for(Edge thatE : Polygon.edges(thatPoly)) {
          if(thisE.intersects(thatE)) return true;
        }
      }
      return false;
    }
  }
  
  public float area() { return width()*height(); }

  public OrientedBoundingBox get() {
    OrientedBoundingBox obb = new OrientedBoundingBox();
    obb.center = center.get();
    obb.U = U.get();
    obb.V = V.get();
    return obb;
  }

  public float dist(OrientedBoundingBox that) {
    if(this.intersects(that)) return 0;
    else {
      Polygon thisPoly = new Polygon(this.corners());
      Polygon thatPoly = new Polygon(that.corners());
      Min<pt> min = new Min<pt>();
      for(pt p : thisPoly.points) min.update(p, thatPoly.dist(p));
      for(pt p : thatPoly.points) min.update(p, thisPoly.dist(p));
      return min.getMin();
    }
  }
}
