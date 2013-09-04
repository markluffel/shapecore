package shapecore;

import static shapecore.Oplet.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import processing.core.PApplet;
import shapecore.interfaces.Ring;

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
  public static pt P(vec V) { return P(V.x, V.y); }
  public static pt3 P(vec3 V) { return P(V.x, V.y, V.z); }

  /** Average */
  public static pt A(pt A, pt B) { return P((A.x + B.x) / 2, (A.y + B.y) / 2); }
  public static pt3 A(pt3 A, pt3 B) { return new pt3((A.x + B.x) / 2.0f, (A.y + B.y) / 2.0f, (A.z + B.z) / 2.0f); }
  public static pt3 A(float[] A, float[] B) { return new pt3((A[0]+B[0])/2, (A[1]+B[1])/2, (A[2]+B[2])/2); }

  public static pt A(pt A, pt B, pt C) {
    return new pt((A.x + B.x + C.x) / 3.0f, (A.y + B.y + C.y) / 3.0f);
  } // Average: (A+B+C)/3
  
  public static pt3 A(pt3 A, pt3 B, pt3 C) {
    return new pt3(
      (A.x + B.x + C.x) / 3.0f,
      (A.y + B.y + C.y) / 3.0f,
      (A.z + B.z + C.z) / 3.0f
    );
  }
  
  public static vec3 A(vec3 A, vec3 B) { return new vec3((A.x + B.x) / 2, (A.y + B.y) / 2, (A.z + B.z) / 2); }
  
  public static pt L(pt A, float s, pt B) { return new pt(A.x + s * (B.x - A.x), A.y + s * (B.y - A.y)); }
  public static pt3 L(pt3 A, float s, pt3 B) { return new pt3(A.x + s * (B.x - A.x), A.y + s * (B.y - A.y), A.z + s * (B.z - A.z)); }

  public static <T extends Ring<T>> T lerp(T a, T b, float alpha) {
    return a.get().scaleBy(1-alpha).addScaledBy(alpha, b);
  }
  
  public static pt S(float s, pt A) { return new pt(s*A.x, s*A.y); }

  /** Weighted sum: aA+bB, make sure that a+b=1 */
  public static pt S(float a, pt A, float b, pt B) {
    return P(a * A.x + b * B.x, a * A.y + b * B.y); }
  public static pt3 S(float a, pt3 A, float b, pt3 B) {
    return new pt3(a * A.x + b * B.x, a * A.y + b * B.y, a * A.z + b * B.z); }
  public static pt3 S(float a, pt3 A, float b, pt3 B, float c, pt3 C) {
    return new pt3(a * A.x + b * B.x + c * C.x, a * A.y + b * B.y + c * C.y, a * A.z + b * B.z + c * C.z); }
  
  public static pt S(float a, pt A, float b, pt B, float c, pt C) {
    return P(a * A.x + b * B.x + c * C.x, a * A.y + b * B.y + c * C.y);
  } // Weighted sum: aA+bB+cC, make sure that a+b+c=1

  public static pt S(float a, pt A, float b, pt B, float c, pt C, float d, pt D) {
    return P(a * A.x + b * B.x + c * C.x + d * D.x, a * A.y + b * B.y + c * C.y
        + d * D.y);
  } // Weighted sum: aA+bB+cC+dD, make sure that a+b+c+d=1

  /** weighted sum of points and vectors, for hermite spline */
  public static pt S(float a, pt p1, float b, pt p2,
      float c, vec v1, float d, vec v2) {
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
    pt G = spiralCenter(E0,E1); 
    return E( spiralPt(E0.a,G,s,a,t) , spiralPt(E0.b,G,s,a,t) );  
  }

  public static Edge R(Edge E0, float t, Edge E1, Edge E) {
    float a = a(E0.dir(),E1.dir());
    float s = E1.length() / E0.length();
    pt G = spiralCenter(E0,E1);  
    return E( spiralPt(E.a,G,s,a,t) , spiralPt(E.b,G,s,a,t) ); 
  }


  public static float d(pt P, pt Q) {
    return PApplet.dist(P.x, P.y, Q.x, Q.y);
  } // ||AB||
    
  public static float d2(pt P, pt Q) {
    return n2(V(P, Q));
  } // ||AB||^2
  // Make vectors

  public static vec V(float x, float y) { return new vec(x, y); }
  public static vec V(double x, double y) { return new vec(x, y); }
  public static vec3 V(float x, float y, float z) { return new vec3(x, y, z); }
  public static vec3 V(double x, double y, double z) { return new vec3(x, y, z); }
  
  public static vec V() {
    return V(0, 0);
  } // V(0,0)

  public static vec V(vec V) {
    return new vec(V.x, V.y);
  } // make copy of V

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

  public static vec S(float s, vec V) { return V.get().scaleBy(s); }
  public static vec3 S(float s, vec3 V) { return V.get().scaleBy(s); }
  public static vec S(vec U, vec V) {
    return V(U.x + V.x, U.y + V.y);
  } // U+V

  /** Weighted sum: aA+bB */
  public static vec S(float a, vec A, float b, vec B) { return V(a * A.x + b * B.x, a * A.y + b * B.y); }
  public static vec3 S(float a, vec3 A, float b, vec3 B) { return new vec3(a * A.x + b * B.x, a * A.y + b * B.y, a * A.z + b * B.z); }
  public static vec3 S(double a, vec3 A, double b, vec3 B) { return new vec3(a * A.x + b * B.x, a * A.y + b * B.y, a * A.z + b * B.z); }
  public static vec3 S(float a, vec3 A, float b, vec3 B, float c, vec3 C) {
    return new vec3(a * A.x + b * B.x + c * C.x, a * A.y + b * B.y + c * C.y, a * A.z + b * B.z + c * C.z);
  }
  public static vec3 S(double a, vec3 A, double b, vec3 B, double c, vec3 C) {
    return new vec3(a * A.x + b * B.x + c * C.x, a * A.y + b * B.y + c * C.y, a * A.z + b * B.z + c * C.z);
  }

  
  public static vec S(float a, vec A, float b, vec B, float c, vec C) {
    return V(a * A.x + b * B.x + c * C.x, a * A.y + b * B.y + c * C.y);
  } // Weighted sum: aA+bB+cC

  public static vec S(float a, vec A, float b, vec B, float c, vec C, float d, vec D) {
    return V(a * A.x + b * B.x + c * C.x + d * D.x, a * A.y + b * B.y + c * C.y
        + d * D.y);
  } // Weighted sum: aA+bB+cC+dD
  
  // A + sAB
  public static pt S(pt a, float s, pt b) {
    return new pt(a.x+s*(b.x-a.x),a.y+s*(b.y-a.y));
  }


  public static vec L(vec u, float s, vec v) {
    return V(u.x + s * (v.x - u.x), u.y + s * (v.y - u.y));
  } // (1-s)U+sV

  static vec A(vec u, vec v) {
    return L(u, 0.5f, v);
  } // (U+V)/2
  // dot product, norm, normalization
  
  public static <T extends Ring<T>> T zero(T k) {
    try {
      return (T) k.getClass().newInstance();
    } catch (InstantiationException e) {
      throw new IllegalStateException(e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }    
  }
  
  public static vec S(vec... vecs) { return S(new vec(), Arrays.asList(vecs)); }
  public static vec Sv(List<vec> vecs) { return S(new vec(), vecs); }
  public static pt S(pt... pts) { return S(new pt(), Arrays.asList(pts)); }
  public static pt Sp(List<pt> pts) { return S(new pt(), pts); }
  
  public static vec3 w(vec3... vecs) { return S(new vec3(), Arrays.asList(vecs)); }
  public static vec3 Sv3(List<vec3> vecs) { return S(new vec3(), vecs); }
  
  public static vec A(vec... vecs) { return A(new vec(), Arrays.asList(vecs)); }
  public static vec Av(List<vec> vecs) { return A(new vec(), vecs); }
  public static pt A(pt... vecs) { return A(new pt(), Arrays.asList(vecs)); }
  public static pt Ap(List<pt> vecs) { return A(new pt(), vecs); }
  
  public static vec3 A(vec3... vecs) { return A(new vec3(), Arrays.asList(vecs)); }
  public static vec3 Av3(List<vec3> vecs) { return A(new vec3(), vecs); }
  public static pt3 A(pt3... vecs) { return A(new pt3(), Arrays.asList(vecs)); }
  public static pt3 Ap3(List<pt3> vecs) { return A(new pt3(), vecs); }
  
  public static <T extends Ring<T>> T S(T zero, Collection<T> vals) {
    T result = zero;
    for(T v : vals) result.add(v);
    return result;
  }
  
  public static <T extends Ring<T>> T A(T zero, Collection<T> vals) {
    T result = S(zero, vals);
    result.scaleBy(1f/vals.size());
    return result;
  }
  
  public static vec average(vec... v) { return A(v); }
  public static vec averagev(List<vec> v) { return Av(v); }
  public static pt average(pt... v) { return A(v); }
  public static pt averagep(List<pt> v) { return Ap(v); }
  
  public static vec3 average(vec3... v) { return A(v); }
  public static vec3 averagev3(List<vec3> v) { return Av3(v); }
  public static pt3 average(pt3... v) { return A(v); }
  public static pt3 averagep3(List<pt3> v) { return Ap3(v); }


  public static float dot(vec U, vec V) {
    return U.dot(V);
  } // U*V (dot product)

  public static float dot(vec3 U, vec3 V) {
    return U.dot(V);
  } // U*V (dot product)
  
  public static float dot(vec3 U, pt3 V) {
    return U.x*V.x + U.y*V.y + U.z*V.z;
  } // U*V (dot product)
  
  public static float dot(pt3 U, vec3 V) {
    return U.x*V.x + U.y*V.y + U.z*V.z;
  } // U*V (dot product)
  
  public static float dot(pt3 U, pt3 V) {
    return U.x*V.x + U.y*V.y + U.z*V.z;
  } // U*V (dot product)
  
  public static float n2(vec V) {
    return V.sqnorm();
  } // V*V (V^2)

  public static vec U(vec V) {
    float n = V.norm();
    if (n == 0) return V(0,0);
    else return S(1 / n, V);
  } // V/||V|| (normalized V, unit vector)
  public static vec3 U(vec3 V) {
    float n = V.norm();
    if (n == 0) return V(0,0,0);
    else return S(1 / n, V);
  }

  public static vec U(pt Q, pt P) { return U(V(Q, P)); }
  public static vec3 U(pt3 Q, pt3 P) { return U(V(Q, P)); }
  // angles and rotations

  public static float a(vec U, vec V) {
    float a = atan2(dot(R(U), V), dot(U, V));
    if (a > PI)
      return mPItoPIangle(a - 2 * PI);
    if (a < -PI)
      return mPItoPIangle(a + 2 * PI);
    return a;
  } // angle(U,V) between -PI and PI

  public static vec R(vec V) {
    return V(-V.y, V.x);
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
  
  public static vec R(vec U, float a) {
    float c = cos(a), s = sin(a);
    return V(c * U.x - s * U.y, s * U.x + c * U.y);
  } // U rotated by a

  public static vec R(vec U, float t, vec V) {
    return S(1 + t * (V.norm() - U.norm()) / U.norm(), R(U, t * angle(U, V)));
  } // interpolation (angle and length) between U and V
  // three points

  // Rotate v by 'a' parallel to plane (I,J)
  public static vec3 R(vec3 v, float a, vec3 I, vec3 J) {
    float x = dot(v,I), y = dot(v,J);
    float c = cos(a), s = sin(a);
    return A(v, S(x*c-x-y*s,I,x*s+y*c-y,J));
  }
  // Rotated V by 'a' around axis A
  public static vec3 R(vec3 V, float a, vec3 A) {
    float d = dot(V,A), c = cos(a), s = sin(a);
    return S(c, V, d*(1-c), A, s, A.cross(V));
  }
  
  public static pt3 R(pt3 v, float a, vec3 A) {
    vec3 vv = new vec3(v.x,v.y,v.z);
    vec3 vr = R(vv, a, A);
    return new pt3(vr.x,vr.y,vr.z);
  }

  
  public static float a(pt A, pt B, pt C) {
    return a(V(B, A), V(B, C));
  } // angle (BA,BC)

  public static float turnAngle(pt A, pt B, pt C) {
    return a(V(A, B), V(B, C));
  } // angle (AB,BC)
  
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
    return projectOnto(here, edge.a, edge.b);
  }


  public static pt3 projectOnto(pt3 here, Edge3 edge) {
    return projectOnto(here, edge.start, edge.end);
  }
  
  public static pt closestPointOnEdge(pt here, pt start, pt end) {
    vec edge = V(start,end);
    vec toHere = V(start,here);
    float t = dot(toHere, edge) / dot(edge, edge);
    t = constrain(t, 0, 1);
    return T(start, t, edge);
  }
  
  public static pt3 projectOnto(pt3 here, pt3 start, pt3 end) {
    vec3 edge = V(start,end);
    vec3 toHere = V(start,here);
    float t = dot(toHere, edge) / dot(edge, edge);
    t = constrain(t, 0, 1);
    return T(start, t, edge);
  }
  
  public static pt projectOnto(pt here, pt start, pt end) {
    vec edge = V(start,end);
    vec toHere = V(start,here);
    float t = dot(toHere, edge) / dot(edge, edge);
    t = constrain(t, 0, 1);
    return T(start, t, edge);
  }
  
  public static float closestArcLengthOnLine(pt here, pt start, vec dir) {
    vec toHere = V(start,here);
    return dot(toHere, dir) / dot(dir, dir);
  }
  
  public static pt closestPointOnLine(pt here, pt start, vec dir) {
    float t = closestArcLengthOnLine(here, start, dir);
    return T(start, t, dir);
  }
  
  public static pt lineIntersection(pt start1, vec dir1, pt start2, vec dir2) {
    float t = (-(start1.y*dir2.x) + start2.y*dir2.x + start1.x*dir2.y - start2.x*dir2.y)
    / (dir1.y*dir2.x - dir1.x*dir2.y);
    return T(start1, t, dir1);
  }
  
  public static pt lineIntersection(pt start1, pt end1, pt start2, pt end2) {
    return lineIntersection(start1, V(start1,end1), start2, V(start2,end2));
  }
  
  public static pt lineEdgeIntersection(pt start1, vec dir1, pt start2, pt end2) {
    vec dir2 = V(start2,end2);
    float t = (-(start2.y*dir1.x) + start1.y*dir1.x + start2.x*dir1.y - start1.x*dir1.y)
    / (dir2.y*dir1.x - dir2.x*dir1.y);
    if(t > 0 && t < 1) {
      return T(start2, t, dir2);
    } else {
      return null;
    }
  }
  
  public static pt edgeIntersection(pt start1, pt end1, pt start2, pt end2) {
    vec dir1 = V(start1,end1);
    vec dir2 = V(start2,end2);
    float t1 = (-(start1.y*dir2.x) + start2.y*dir2.x + start1.x*dir2.y - start2.x*dir2.y)
    / (dir1.y*dir2.x - dir1.x*dir2.y);
    float t2 = (-(start2.y*dir1.x) + start1.y*dir1.x + start2.x*dir1.y - start1.x*dir1.y)
    / (dir2.y*dir1.x - dir2.x*dir1.y);
    if(t1 >= 0 && t1 <= 1 && t2 >= 0 && t2 <= 1) {
      return T(start1, t1, dir1);
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
    return A(T(p, s, u), T(q, t, v));
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
    return d(here, closestPointOnEdge(here, start, end));
  }
  
  public static float sqdistToEdge(pt here, pt start, pt end) {
    return d2(here, closestPointOnEdge(here, start, end));
  }
  
  public static boolean isRightTurn(pt A, pt B, pt C) {
    return dot(R(A, B), V(B, C)) > 0;
  } // right turn (as seen on screen)

  public static boolean isRightOf(pt A, pt Q, vec T) {
    return dot(R(T), V(Q, A)) > 0;
  } // A is on right of ray(end,dir) (as seen on screen)
  
  public static boolean isInFrontOf(pt A, pt Q, vec T) {
    return dot(T, V(Q, A)) > 0;
  } // A is in frontof ray(end,dir)

  public static boolean inTriangle(pt q, pt a, pt b, pt c) {
    return isRightTurn(a,b,q) && isRightTurn(b,c,q) && isRightTurn(c,a,q);
  }

  // 3rd component of cross product.
  public static float cr(vec U, vec V) {
    return U.x*V.y-U.y*V.x;
  }
}
