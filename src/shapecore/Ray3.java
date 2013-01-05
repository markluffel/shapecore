package shapecore;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

public class Ray3 {    
  public pt3 start;
  public vec3 dir;
  
  public Ray3(pt3 start, vec3 dir) {
    this.start = start;
    this.dir = dir.normalize(); // required by sqdist
  }

  /**
   * Get the closest projection of the given point onto this ray.
   * (Actually onto the line containing this ray) 
   */
  public pt3 project(pt3 p) {
    return T(start, V(start, p).dot(dir), dir);
  }
  
  public float sqdist(pt3 p) {
    vec3 sp = V(start, p);
    return sp.sqnorm() - sq(sp.dot(dir));
  }

  public pt3 at(float dist) {
    return T(start, dist, dir);
  }

  /*
  Ray(pt pQ, vec pT) {
    start.setTo(pQ);
    dir.setTo(U(pT));
  }

  Ray(pt pQ, vec pT, float pd) {
    start.setTo(pQ);
    dir.setTo(U(pT));
    d = max(0, pd);
  }

  Ray(pt A, pt B) {
    start.setTo(A);
    dir.setTo(U(V(A, B)));
    d = d(A, B);
  }

  Ray(Ray B) {
    start.setTo(B.start);
    dir.setTo(B.dir);
    d = B.d;
    dir.normalize();
  }

  void setTo(pt P, vec V) {
    start.setTo(P);
    dir.setTo(U(V));
  }

  void setTo(Ray B) {
    start.setTo(B.start);
    dir.setTo(B.dir);
    d = B.d;
    dir.normalize();
  }

  // void showArrow() {arrow(start,r,dir); }
  // void showLine() {show(start,d,dir);}
  pt at(float s) {
    return new pt(start, s, dir);
  }

  pt at() {
    return new pt(start, d, dir);
  }

  // void turn(float a) {dir.rotateBy(a);} void turn() {dir.rotateBy(PI/180.);}

  float disToLine(pt A, vec N) {
    float n = dot(N, dir);
    float t = 0;
    if (abs(n) > 0.000001)
      t = -dot(N, V(A, start)) / n;
    return t;
  }

  boolean hitsEdge(pt A, pt B) {
    boolean hit = isRightOf(A, start, dir) != isRightOf(B, start, dir);
    if (isRightTurn(A, B, start) == (dot(dir, R(V(A, B))) > 0)) {
      hit = false;
    }
    return hit;
  }
  
  float disToEdge(pt A, pt B) {
    vec N = U(R(V(A, B)));
    float t = 0;
    float n = dot(N, dir);
    if (abs(n) > 0.000001)
      t = -dot(N, V(A, start)) / n;
    return t;
  } // distance to edge along ray if hits

  pt intersectionWithEdge(pt A, pt B) {
    return at(disToEdge(A, B));
  } // hit point if hits

  Ray reflectedOfEdge(pt A, pt B) {
    pt X = intersectionWithEdge(A, B);
    vec V = dir.makeReflectedVec(R(U(V(A, B))));
    float rd = d - disToEdge(A, B);
    return ray(X, V, rd);
  } // bounced ray

  Ray surfelOfEdge(pt A, pt B) {
    pt X = intersectionWithEdge(A, B);
    vec V = R(U(V(A, B)));
    float rd = d - disToEdge(A, B);
    return ray(X, V, rd);
  } // bounced ray

  // float disToCircle(pt C, float r) { return
  // rayCircleIntesectionParameter(start,dir,C,r);} // distance to circle along ray
  // pt intersectionWithCircle(pt C, float r) {return at(disToCircle(C,r));} //
  // intersection point if hits
  //boolean hitsCircle(pt C, float r) {return disToCircle(C,r)!=-1;}                   // hit test
  //Ray reflectedOfCircle(pt C, float r) {pt X=intersectionWithCircle(C,r); vec V =dir.makeReflectedVec(U(V(C,X))); float rd=d-disToCircle(C,r); return ray (X,V,rd); }
  */
  
}
