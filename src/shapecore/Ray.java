package shapecore;

import static processing.core.PApplet.*;
import static shapecore.Geometry.*;

public class Ray {  
  public pt start;
  public vec dir;
  
  public Ray(pt start, vec dir) {
    this.start = start;
    this.dir = dir;
  }

  public Ray(pt from, pt to) {
    start = from;
    dir = from.to(to);
  }

  public void set(pt start, vec dir) {
    this.start.set(start);
    this.dir.set(dir).normalize();
  }

  public void set(Ray r) {
    start.set(r.start);
    dir.set(r.dir).normalize();
  }

  pt at(float s) {
    return new pt(start, s, dir);
  }

  float disToLine(pt a, vec along) {
    float dot = dot(along, dir);
    float t = 0;
    if(abs(dot) > 0.000001) {
      t = -dot(along, V(a, start)) / dot;
    }
    return t;
  }

  boolean hitsEdge(pt a, pt b) {
    boolean hit = isRightOf(a, start, dir) != isRightOf(b, start, dir);
    if(isRightTurn(a, b, start) == (dot(dir, R(V(a, b))) > 0)) {
      hit = false;
    }
    return hit;
  }
  
  /** distance to edge along ray if hits */
  public float distToEdge(pt a, pt b) {
    vec n = a.to(b).turnLeft().normalize();
    float dot = dot(n, dir);
    if(abs(dot) > 0.000001) {
      return -dot(n, V(a, start)) / dot;
    } else {
      return Float.MAX_VALUE;
    }
  } 

  pt intersectionWithEdge(pt a, pt b) {
    return at(distToEdge(a, b));
  }
}