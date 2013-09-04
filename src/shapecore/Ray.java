package shapecore;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

public class Ray {  
  pt start;
  vec dir;
  
  Ray(pt start, vec dir) {
    this.start = start;
    this.dir = dir;
  }

  Ray(pt a, pt b) {
    start = a;
    dir = a.to(b);
  }

  void set(pt P, vec V) {
    start.setTo(P);
    dir.setTo(U(V));
  }

  void setTo(Ray B) {
    start.setTo(B.start);
    dir.setTo(B.dir);
    dir.normalize();
  }

  // void showArrow() {arrow(start,r,dir); }
  // void showLine() {show(start,d,dir);}
  pt at(float s) {
    return new pt(start, s, dir);
  }

  // void turn(float a) {dir.rotateBy(a);} void turn() {dir.rotateBy(PI/180.);}

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
  float disToEdge(pt a, pt b) {
    vec n = a.to(b).turnLeft().normalize();
    float dot = dot(n, dir);
    if(abs(dot) > 0.000001) {
      return -dot(n, V(a, start)) / dot;
    } else {
      return Float.MAX_VALUE;
    }
  } 

  pt intersectionWithEdge(pt a, pt b) {
    return at(disToEdge(a, b));
  }
}