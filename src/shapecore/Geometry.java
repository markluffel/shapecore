package shapecore;

import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static shapecore.Oplet.*;

import java.util.Arrays;
import java.util.Collection;

import shapecore.interfaces.Ring;
import Jama.Matrix;

public class Geometry {


  // Make points
  public static pt P(float x, float y) { return new pt(x, y); }
  public static pt P(double x, double y) { return new pt(x, y); }
  public static pt3 P(float x, float y, float z) { return new pt3(x, y, z); }
  public static pt3 P(double x, double y, double z) { return new pt3(x, y, z); }

  public static pt P() {
    return P(0, 0);
  } // make point (0,0)

  public static pt P(pt P) {
    return P(P.x, P.y);
  } // make copy of point startCurve
  /** @deprecated (whatev) */
  public static pt P(vec v) { return P(v.x, v.y); }
  public static pt3 P(vec3 v) { return P(v.x, v.y, v.z); }

  /** Average */
//  public static pt A(pt a, pt b) { return P((a.x + b.x) / 2, (a.y + b.y) / 2); }
//  public static pt3 A(pt3 a, pt3 b) { return new pt3((a.x + b.x) / 2.0f, (a.y + b.y) / 2.0f, (a.z + b.z) / 2.0f); }
//  public static pt3 A(float[] a, float[] b) { return new pt3((a[0]+b[0])/2, (a[1]+b[1])/2, (a[2]+b[2])/2); }

//  public static pt A(pt a, pt b, pt c) {
//    return new pt((a.x + b.x + c.x) / 3.0f, (a.y + b.y + c.y) / 3.0f);
//  } // Average: (A+B+C)/3
//  
//  public static pt3 A(pt3 a, pt3 b, pt3 c) {
//    return new pt3(
//      (a.x + b.x + c.x) / 3.0f,
//      (a.y + b.y + c.y) / 3.0f,
//      (a.z + b.z + c.z) / 3.0f
//    );
//  }
  
//  public static vec3 A(vec3 A, vec3 B) { return new vec3((A.x + B.x) / 2, (A.y + B.y) / 2, (A.z + B.z) / 2); }
  
  //public static pt L(pt A, pt B, float s) { return new pt(A.x + s * (B.x - A.x), A.y + s * (B.y - A.y)); }
  //public static pt3 L(pt3 A, float s, pt3 B) { return new pt3(A.x + s * (B.x - A.x), A.y + s * (B.y - A.y), A.z + s * (B.z - A.z)); }

  public static <T extends Ring<T>> T lerp(T a, T b, float alpha) {
    return a.get().scaleBy(1-alpha).addScaledBy(alpha, b);
  }
  
  public static pt S(float s, pt a) { return new pt(s*a.x, s*a.y); }

  /** Weighted sum: aA+bB, make sure that a+b=1 */
  public static pt S(float a, pt pa, float b, pt pb) {
    return P(a * pa.x + b * pb.x, a * pa.y + b * pb.y);
  }
  public static pt3 S(float a, pt3 pa, float b, pt3 pb) {
    return new pt3(a * pa.x + b * pb.x, a * pa.y + b * pb.y, a * pa.z + b * pb.z);
  }
  public static pt3 S(float a, pt3 pa, float b, pt3 pb, float c, pt3 pc) {
    return new pt3(a * pa.x + b * pb.x + c * pc.x, a * pa.y + b * pb.y + c * pc.y, a * pa.z + b * pb.z + c * pc.z);
  }
  public static pt S(float a, pt pa, float b, pt pb, float c, pt pc) {
    return P(a * pa.x + b * pb.x + c * pc.x, a * pa.y + b * pb.y + c * pc.y);
  }
  public static pt S(float a, pt pa, float b, pt pb, float c, pt pc, float d, pt pd) {
    return P(a * pa.x + b * pb.x + c * pc.x + d * pd.x, a * pa.y + b * pb.y + c * pc.y + d * pd.y);
  }

  /** weighted sum of points and vectors, for hermite spline */
  public static pt S(float a, pt p1, float b, pt p2, float c, vec v1, float d, vec v2) {
   return new pt(
       a*p1.x + b*p2.x + c*v1.x + d*v2.x,
       a*p1.y + b*p2.y + c*v1.y + d*v2.y
   );
 }

  // transformed points
  public static pt T(pt P, vec v) { return P(P.x + v.x, P.y + v.y); }
  public static pt T(pt P, float s, vec v) { return T(P, S(s, v)); }
  public static pt T(pt P, float s, pt Q) { return T(P, s, V(P, Q)); }
  public static pt T(pt O, float x, vec I, float y, vec J) {
    return P(O.x + x * I.x + y * J.x, O.y + x * I.y + y * J.y);
  } // O+xI+yJ (change of coordinate systems)
  
  public static pt3 T(pt3 P, vec3 v) { return P.get().translateBy(v); }
  public static pt3 T(pt P, vec3 v) { return P.as3D().translateBy(v); }
  public static pt3 T(pt3 P, float s, vec3 v) { return T(P, S(s, v)); }
  public static pt3 T(pt P, float s, vec3 v) { return T(P, S(s, v)); }
  
  public static pt3 T(pt3 O, float u, vec3 I, float v, vec3 J) {
    return new pt3(O.x + u * I.x + v * J.x,
                   O.y + u * I.y + v * J.y,
                   O.z + u * I.z + v * J.z);
  }
  public static pt3 T(pt3 O, float u, vec3 I, float v, vec3 J, float w, vec3 K) {
    return new pt3(O.x + u * I.x + v * J.x + w * K.x,
                   O.y + u * I.y + v * J.y + w * K.y,
                   O.z + u * I.z + v * J.z + w * K.z);
  }
  
  public static pt R(pt Q, float a) {
    float c = cos(a), s = sin(a);
    return P(c * Q.x - s * Q.y, s * Q.x + c * Q.y);
  } // Rotated end by a around origin

  public static pt R(pt Q, float a, pt P) {
    return T(P, R(V(P, Q), a));
  } // Rotated end by a around startCurve
  // distance
  
  public static Edge R(Edge E0, float t, Edge E1) {
    float a = a(E0.dir(),E1.dir());
    float s = E1.length() / E0.length();
    pt g = spiralCenter(E0,E1); 
    return E( spiralPt(E0.a,g,s,a,t) , spiralPt(E0.b,g,s,a,t) );  
  }

  public static Edge R(Edge E0, float t, Edge E1, Edge E) {
    float a = a(E0.dir(),E1.dir());
    float s = E1.length() / E0.length();
    pt g = spiralCenter(E0,E1);  
    return E( spiralPt(E.a,g,s,a,t) , spiralPt(E.b,g,s,a,t) ); 
  }


  public static float d(pt P, pt Q) {
    return P.dist(Q);
  } // ||AB||
    
  public static float d2(pt P, pt Q) {
    return P.sqdist(Q);
  } // ||AB||^2
  // Make vectors

  public static vec V(float x, float y) { return new vec(x, y); }
  public static vec V(double x, double y) { return new vec(x, y); }
  public static vec3 V(float x, float y, float z) { return new vec3(x, y, z); }
  public static vec3 V(double x, double y, double z) { return new vec3(x, y, z); }
  
  public static vec V(pt P) { return V(P.x, P.y); }
  public static vec3 V(pt3 P) { return V(P.x, P.y, P.z); }
  
  public static vec V(pt P, pt Q) {
    return new vec(Q.x - P.x, Q.y - P.y);
  }
  public static vec3 V(pt3 P, pt3 Q) {
    return new vec3(Q.x - P.x, Q.y - P.y, Q.z - P.z);
  }
  public static vec3 V(vec3 P, vec3 Q) {
    return new vec3(Q.x - P.x, Q.y - P.y, Q.z - P.z);
  }
  public static vec V(pt P, float qx, float qy) {
    return new vec(qx - P.x, qy - P.y);
  }
  public static vec3 V(pt3 P, float qx, float qy, float qz) {
    return new vec3(qx - P.x, qy - P.y, qz - P.z);
  }
  public static vec3 V(pt3 P, pt Q) {
    return new vec3(Q.x - P.x, Q.y - P.y, 0 - P.z);
  }

  public static vec S(float s, vec v) { return v.get().scaleBy(s); }
  public static vec3 S(float s, vec3 v) { return v.get().scaleBy(s); }
  public static vec S(vec u, vec v) {
    return V(u.x + v.x, u.y + v.y);
  } // U+V

  /* Weighted sums */
  public static vec S(float a, vec va, float b, vec vb) {
    return V(a * va.x + b * vb.x, a * va.y + b * vb.y);
  }
  public static vec S(float a, vec va, float b, vec vb, float c, vec vc) {
    return V(a * va.x + b * vb.x + c * vc.x, a * va.y + b * vb.y + c * vc.y);
  }
  public static vec S(float a, vec va, float b, vec vb, float c, vec vc, float d, vec vd) {
    return V(a * va.x + b * vb.x + c * vc.x + d * vd.x, a * va.y + b * vb.y + c * vc.y + d * vd.y);
  }
  public static vec3 S(float a, vec3 va, float b, vec3 vb) {
    return new vec3(a * va.x + b * vb.x, a * va.y + b * vb.y, a * va.z + b * vb.z);
  }
  public static vec3 S(float a, vec3 va, float b, vec3 vb, float c, vec3 vc) {
    return new vec3(a * va.x + b * vb.x + c * vc.x, a * va.y + b * vb.y + c * vc.y, a * va.z + b * vb.z + c * vc.z);
  }
  public static vec3 S(double a, vec3 va, double b, vec3 vb, double c, vec3 vc) {
    return new vec3(a * va.x + b * vb.x + c * vc.x, a * va.y + b * vb.y + c * vc.y, a * va.z + b * vb.z + c * vc.z);
  }

//  public static <T extends Ring<T>> T zero(T k) {
//    try {
//      return (T) k.getClass().newInstance();
//    } catch (InstantiationException e) {
//      throw new IllegalStateException(e);
//    } catch (IllegalAccessException e) {
//      throw new IllegalStateException(e);
//    }    
//  }
  
  public static <T extends Ring<T>> T sum(T zero, Collection<T> vals) {
    T result = zero;
    for(T v : vals) result.add(v);
    return result;
  }
  
  public static <T extends Ring<T>> T average(T zero, Collection<T> vals) {
    T result = sum(zero, vals);
    result.scaleBy(1f/vals.size());
    return result;
  }
  
  public static vec average(vec... vecs) { return average(new vec(), Arrays.asList(vecs)); }
  public static pt average(pt... pts) { return average(new pt(), Arrays.asList(pts)); }
  
  public static vec3 average(vec3... vecs) { return average(new vec3(), Arrays.asList(vecs)); }
  public static pt3 average(pt3... pts) { return average(new pt3(), Arrays.asList(pts)); }

  public static float dot(vec u, vec v) {
    return u.dot(v);
  } // U*V (dot product)

  public static float dot(vec3 u, vec3 v) {
    return u.dot(v);
  } // U*V (dot product)
  
  public static float dot(vec3 u, pt3 v) {
    return u.x*v.x + u.y*v.y + u.z*v.z;
  } // U*V (dot product)
  
  public static float dot(pt3 u, vec3 v) {
    return u.x*v.x + u.y*v.y + u.z*v.z;
  } // U*V (dot product)
  
  public static float dot(pt3 u, pt3 v) {
    return u.x*v.x + u.y*v.y + u.z*v.z;
  } // U*V (dot product)
  
  public static float n2(vec v) {
    return v.sqnorm();
  } // V*V (V^2)

  public static vec U(vec v) {
    float n = v.norm();
    if (n == 0) return V(0,0);
    else return S(1 / n, v);
  } // V/||V|| (normalized V, unit vector)
  
  public static vec3 U(vec3 v) {
    return v.get().normalize();
  }

  public static vec U(pt Q, pt P) { return U(Q.to(P)); }
  public static vec3 U(pt3 Q, pt3 P) { return U(Q.to(P)); }
  // angles and rotations

  public static float a(vec u, vec v) {
    float a = atan2(dot(R(u), v), dot(u, v));
    if (a > PI)
      return mPItoPIangle(a - 2 * PI);
    if (a < -PI)
      return mPItoPIangle(a + 2 * PI);
    return a;
  } // angle(U,V) between -PI and PI

  public static vec R(vec v) {
    return V(-v.y, v.x);
  } // V rotated 90 degrees (clockwise as seen on screen)

  public static vec R(pt Q, pt P) {
    return R(V(Q, P));
  } // vector QP rotated 90 degrees
  
  /**
   * A unit vector facing outward (leftward) from a region bounded by this line segment, assuming clockwise winding.
   * @param Q tail
   * @param P head
   * @return
   */
  public static vec normal(pt Q, pt P) {
    vec v = V(Q,P);
    v.turnLeft();
    v.normalize();
    return v;
  }
  
  @Deprecated
  public static vec R(vec u, float a) {
    return u.get().rotateBy(a);
  } // U rotated by a

  public static vec R(vec u, float t, vec v) {
    return S(1 + t * (v.norm() - u.norm()) / u.norm(), R(u, t * angle(u, v)));
  } // interpolation (angle and length) between U and V
  // three points

  // Rotate v by 'a' parallel to plane (I,J)
  public static vec3 R(vec3 v, float a, vec3 I, vec3 J) {
    float x = dot(v,I), y = dot(v,J);
    float c = cos(a), s = sin(a);
    return average(v, S(x*c-x-y*s,I,x*s+y*c-y,J));
  }
  // Rotated V by 'a' around axis A
  public static vec3 R(vec3 v, float a, vec3 axis) {
    float d = dot(v,axis), c = cos(a), s = sin(a);
    return S(c, v, d*(1-c), axis, s, axis.cross(v));
  }
  
  public static pt3 R(pt3 p, float a, vec3 axis) {
    vec3 vv = new vec3(p.x,p.y,p.z);
    vec3 vr = R(vv, a, axis);
    return new pt3(vr.x,vr.y,vr.z);
  }

  public static float angle(vec v) {
    return atan2(v.y, v.x);
  }

  public static float angle(vec u, vec v) {
    return atan2(dot(R(u), v), dot(u, v));
  }
  
  public static float angle(pt a, pt b) {
    return angle(V(a,b));
  }
  
  public static float angle(pt a, pt b, pt c) {
    return angle(a.to(b), b.to(c));
  }

  public static float turnAngle(pt a, pt b, pt C) {
    return a(V(a, b), V(b, C));
  }
  
  public static float closestArcLengthOnEdge(pt here, pt start, pt end) {
    vec edge = V(start,end);
    vec toHere = V(start,here);
    float t = dot(toHere, edge) / dot(edge, edge);
    return constrain(t, 0, 1);
  }
  
  public static pt projectOnto(pt p, Circle c) {
    return T(c.center, U(c.center, p).scaleBy(c.radius));
  }
  
  public static pt projectOnto(pt here, Edge edge) {
    return closestPointOnEdge(here, edge.a, edge.b);
  }

  public static pt3 projectOnto(pt3 here, Edge3 edge) {
    return closestPointOnEdge(here, edge.start, edge.end);
  }
  
  public static pt closestPointOnEdge(pt here, pt start, pt end) {
    vec edge = start.to(end);
    vec toHere = start.to(here);
    float t = dot(toHere, edge) / dot(edge, edge);
    t = constrain(t, 0, 1);
    return start.get().add(t, edge);
  }
  
  public static pt3 closestPointOnEdge(pt3 here, pt3 start, pt3 end) {
    vec3 edge = start.to(end);
    vec3 toHere = start.to(here);
    float t = dot(toHere, edge) / dot(edge, edge);
    t = constrain(t, 0, 1);
    return start.get().add(t, edge);
  }
    
  public static float closestArcLengthOnLine(pt here, pt start, vec dir) {
    vec toHere = V(start,here);
    return dot(toHere, dir) / dot(dir, dir);
  }
  
  public static pt closestPointOnLine(pt here, pt start, vec dir) {
    float t = closestArcLengthOnLine(here, start, dir);
    return start.get().add(t, dir);
  }
  
  public static pt lineIntersection(pt start1, vec dir1, pt start2, vec dir2) {
    float t = (-(start1.y*dir2.x) + start2.y*dir2.x + start1.x*dir2.y - start2.x*dir2.y)
    / (dir1.y*dir2.x - dir1.x*dir2.y);
    return start1.get().add(t, dir1);
  }
  
  public static pt lineIntersection(pt start1, pt end1, pt start2, pt end2) {
    return lineIntersection(start1, V(start1,end1), start2, V(start2,end2));
  }
  
  public static pt lineEdgeIntersection(pt start1, vec dir1, pt start2, pt end2) {
    vec dir2 = start2.to(end2);
    float t = (-(start2.y*dir1.x) + start1.y*dir1.x + start2.x*dir1.y - start1.x*dir1.y)
    / (dir2.y*dir1.x - dir2.x*dir1.y);
    if(t > 0 && t < 1) {
      return start2.get().add(t, dir2);
    } else {
      return null;
    }
  }
  
  public static pt edgeRayIntersection(pt start1, pt end1, pt rayStart, vec rayDir) {
    vec dir1 = start1.to(end1);
    float t1 = (-(start1.y*rayDir.x) + rayStart.y*rayDir.x + start1.x*rayDir.y - rayStart.x*rayDir.y)
    / (dir1.y*rayDir.x - dir1.x*rayDir.y);
    float t2 = (-(rayStart.y*dir1.x) + start1.y*dir1.x + rayStart.x*dir1.y - start1.x*dir1.y)
    / (rayDir.y*dir1.x - rayDir.x*dir1.y);
    if(t1 >= 0 && t1 <= 1 && t2 >= 0) {
      return start1.get().add(t1, dir1);
    } else {
      return null;
    }
  }
  
  public static pt edgeIntersection(pt start1, pt end1, pt start2, pt end2) {
    vec dir1 = start1.to(end1);
    vec dir2 = start2.to(end2);
    float t1 = (-(start1.y*dir2.x) + start2.y*dir2.x + start1.x*dir2.y - start2.x*dir2.y)
    / (dir1.y*dir2.x - dir1.x*dir2.y);
    float t2 = (-(start2.y*dir1.x) + start1.y*dir1.x + start2.x*dir1.y - start1.x*dir1.y)
    / (dir2.y*dir1.x - dir2.x*dir1.y);
    if(t1 >= 0 && t1 <= 1 && t2 >= 0 && t2 <= 1) {
      return start1.get().add(t1, dir1);
    } else {
      return null;
    }
  }
  
  /**
   * Midpoint on edge between min distance points
   */
  public static pt3 lineLineIntersection(pt3 start1, vec3 dir1, pt3 start2, vec3 dir2) {
    pt3 p = start1, q = start2, w = P(p.x-q.x, p.y-q.y, p.z-q.z);
    vec3 u = dir1, v = dir2;
    float a = dot(u,u), b = dot(u,v), c = dot(v,v), d = dot(u,w), e = dot(v, w);
    float s = (b*e - c*d)/(a*c - b*b);// for 1
    float t = (a*e - b*d)/(a*c - b*b);// for 2
    return average(T(p, s, u), T(q, t, v));
  }
  
  public static pt circumcenter(pt a, pt b, pt c) {
    // based on the megamu mesh library
    double
    v1x = 2 * (b.x-a.x),
    v1y = 2 * (b.y-a.y),
    v1z = a.x*a.x - b.x*b.x + a.y*a.y - b.y*b.y,

    v2x = 2 * (c.x-a.x),
    v2y = 2 * (c.y-a.y),
    v2z = a.x*a.x - c.x*c.x + a.y*a.y - c.y*c.y,

    tmpx = v1y * v2z - v1z * v2y,
    tmpy = v1z * v2x - v1x * v2z,
    tmpz = v1x * v2y - v1y * v2x;

    return new pt(tmpx/tmpz, tmpy/tmpz);
  }
  
  public static float distToEdge(pt here, pt start, pt end) {
    return sqrt(sqdistToEdge(here, start, end));
  }
  
  public static float sqdistToEdge(pt here, pt start, pt end) {
    return here.sqdist(closestPointOnEdge(here, start, end));
  }
  
  public static float distToEdge(pt3 here, pt3 start, pt3 end) {
    return sqrt(sqdistToEdge(here, start, end));
  }
  
  public static float sqdistToEdge(pt3 here, pt3 start, pt3 end) {
    return here.sqdist(closestPointOnEdge(here, start, end));
  }
  
  public static boolean isRightTurn(pt a, pt b, pt C) {
    return dot(R(a, b), b.to(C)) > 0;
  } // right turn (as seen on screen)

  public static boolean isRightOf(pt a, pt rayStart, vec rayDir) {
    return dot(R(rayDir), rayStart.to(a)) > 0;
  } // A is on right of ray(end,dir) (as seen on screen)
  
  public static boolean isInFrontOf(pt a, pt Q, vec T) {
    return dot(T, Q.to(a)) > 0;
  } // A is in frontof ray(end,dir)

  public static boolean inTriangle(pt q, pt a, pt b, pt c) {
    return isRightTurn(a,b,q) && isRightTurn(b,c,q) && isRightTurn(c,a,q);
  }
  
  public static float norm(float[] a, float[] b) {
    float sum = 0;
    int len = min(a.length, b.length);
    for(int i = 0; i < len; i++) {
      sum += sq(a[i]-b[i]);
    }
    return sqrt(sum);
  }
}
