package shapecore;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.io.Serializable;
import java.util.List;

import shapecore.interfaces.Ring;


public class pt implements Ring<pt>, Serializable {
  public float x = 0, y = 0;
  static final long serialVersionUID = -33l;

  // CREATE
  public pt() {
  }

  public pt(float px, float py) {
    x = px;
    y = py;
  }

  public pt(double px, double py) {
    x = (float) px;
    y = (float) py;
  }

  public pt(pt P) {
    x = P.x;
    y = P.y;
  }

  public pt(pt P, vec V) {
    x = P.x + V.x;
    y = P.y + V.y;
  }

  public pt(pt P, float s, vec V) {
    x = P.x + s * V.x;
    y = P.y + s * V.y;
  }

  public pt(pt A, float s, pt B) {
    x = A.x + s * (B.x - A.x);
    y = A.y + s * (B.y - A.y);
  }

  public pt(float[] A) {
    x = A[0];
    y = A[1];
  }
  
  public float[] toArray() {
    return new float[] {x,y};
  }

  public static pt random(Oplet p) {
    return new pt(p.random(p.width), p.random(p.height));
  }

  // MODIFY
  public void setTo(float px, float py) {
    x = px;
    y = py;
  }

  public void setTo(double px, double py) {
    x = (float) px;
    y = (float) py;
  }

  public void setTo(pt P) {
    x = P.x;
    y = P.y;
  }

  public void set(pt p) {
    x = p.x;
    y = p.y;
  }

  public pt scale(float t) {
    x *= t;
    y *= t;
    return this;
  }

  public pt scaleBy(float f) {
    x *= f;
    y *= f;
    return this;
  }

  public pt scaleBy(float u, float v) {
    x *= u;
    y *= v;
    return this;
  }

  public pt add(vec v) {
    x += v.x;
    y += v.y;
    return this;
  }

  public pt add(float x_, float y_) {
    x += x_;
    y += y_;
    return this;
  }

  public pt add(double x_, double y_) {
    x += x_;
    y += y_;
    return this;
  }

  public pt add(float s, vec v) {
    x += s * v.x;
    y += s * v.y;
    return this;
  }

  public pt addVec(vec v) {
    x += v.x;
    y += v.y;
    return this;
  }

  public pt translateBy(vec v) {
    x += v.x;
    y += v.y;
    return this;
  }

  public pt translateBy(float s, vec v) {
    x += s * v.x;
    y += s * v.y;
    return this;
  }

  public pt translateBy(double s, vec v) {
    return translateBy((float) s, v);
  }

  public pt translateBy(float u, float v) {
    x += u;
    y += v;
    return this;
  }

  public pt translateTowardsByRatio(float s, pt P) {
    x += s * (P.x - x);
    y += s * (P.y - y);
    return this;
  }

  public pt translateTowardsBy(float s, pt P) {
    translateBy(s, to(P).normalize());
    return this;
  }

  public pt addPt(pt P) {
    x += P.x;
    y += P.y;
    return this;
  } // incorrect notation, but useful for computing weighted averages

  @Deprecated
  // for compliance to interface
  public pt add(pt P) {
    return addPt(P);
  }

  @Deprecated
  // for compliance to interface
  public pt addScaledBy(float s, pt P) {
    x += s * P.x;
    y += s * P.y;
    return this;
  }

  public pt addScaledPt(float s, pt P) {
    x += s * P.x;
    y += s * P.y;
    return this;
  }

  public pt rotateBy(float a) {
    float dx = x, dy = y, c = cos(a), s = sin(a);
    x = c * dx - s * dy;
    y = s * dx + c * dy;
    return this;
  } // around origin

  public pt rotateBy(float a, pt P) {
    float dx = x - P.x, dy = y - P.y, c = cos(a), s = sin(a);
    x = P.x + c * dx - s * dy;
    y = P.y + s * dx + c * dy;
    return this;
  } // around point startCurve

  public pt rotateBy(float s, float t, pt P) {
    float dx = x - P.x, dy = y - P.y;
    dx -= dy * t;
    dy += dx * s;
    dx -= dy * t;
    x = P.x + dx;
    y = P.y + dy;
    return this;
  } // s=sin(a); t=tan(a/2);

  @Override
  public pt clone() {
    return get();
  }

  public pt get() {
    return new pt(x, y);
  }

  public pt makeProjectionOnLine(pt P, pt Q) {
    float a = dot(P.to(this), P.to(Q)), b = dot(P.to(Q),
        P.to(Q));
    return P.get().translateTowardsByRatio(a / b, Q);
  }

  public pt makeOffset(pt P, pt Q, float dist) {
    float a = Geometry.angle(to(P), to(Q)) / 2;
    float h = (float) (dist / Math.tan(a));
    vec T = to(P);
    T.normalize();
    vec N = T.get().turnLeft();
    pt r = new pt(x, y);
    r.translateBy(h, T);
    r.translateBy(dist, N);
    return r;
  }

  public float dist(pt P) {
    return (float) Math.sqrt(sq(P.x - x) + sq(P.y - y));
  }

  public float dist(float px, float py) {
    return (float) Math.sqrt(sq(x - px) + sq(y - py));
  }

  public float sqdist(float px, float py) {
    return sq(x - px) + sq(y - py);
  }

  public float sqdist(pt that) {
    return sq(this.x - that.x) + sq(this.y - that.y);
  }

  public boolean projectsBetween(pt P, pt Q) {
    float a = dot(P.to(this), P.to(Q)), b = dot(P.to(Q),P.to(Q));
    return (0 < a) && (a < b);
  }

  public float ratioOfProjectionBetween(pt P, pt Q) {
    float a = dot(P.to(this), P.to(Q)), b = dot(P.to(Q), P.to(Q));
    return a / b;
  }

  /*
   * public float disToLine(pt P, pt Q) { float a = dot(P.makeVecTo(this),
   * P.makeVecTo(Q).normalize().turnLeft()); return abs(a); }
   */

  public boolean isInTriangle(pt A_, pt B_, pt C_) {
    boolean a = this.isLeftOf(B_, C_);
    boolean b = this.isLeftOf(C_, A_);
    boolean c = this.isLeftOf(A_, B_);
    return (a && b && c) || (!a && !b && !c);
  }

  public boolean inCircle(pt center, float radius) {
    return d(this, center) < radius;
  }

  public boolean isLeftOf(pt P, pt Q) {
    return dot(P.to(this), P.to(Q).turnLeft()) > 0;
  }

  public boolean isLeftOf(pt P, pt Q, float e) {
    return dot(P.to(this), P.to(Q).turnLeft()) > e;
  }

  public pt3 as3D() {
    return new pt3(x, y, 0);
  }

  public String toString() {
    return "(" + x + ", " + y + ")";
  }

  public String unparse() {
    return x + " " + y;
  }

  // this doesn't parse the toString version
  public static pt parse(String string) {
    String[] parts = string.split(" ");
    return new pt(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
  }

  public boolean equals(Object that) {
    if (that instanceof pt) {
      pt thatP = (pt) that;
      return this.x == thatP.x && this.y == thatP.y;
    } else {
      return false;
    }
  }

  public void transform(pt O, vec I, vec J) {
    this.setTo(O.x + x * I.x + y * J.x, O.y + x * I.y + y * J.y);
  }

  public void transform(Affinity aff) {
    this.setTo(aff.O.x + x * aff.I.x + y * aff.J.x, aff.O.y + x * aff.I.y + y
        * aff.J.y);
  }

  public void localP(pt O, vec I, vec J) {
    pt L = local(this, O, I, J);
    x = L.x;
    y = L.y;
  }

  public pt zero() {
    return new pt(0, 0);
  }

  public boolean leftOf(Edge e) {
    return R(e.a, e.b).dot(V(e.a, this)) > 0;
  }

  public vec to(pt that) {
    return new vec(this,that);
  }
  
  public static pt average(List<pt> pts) { return Geometry.average(new pt(), pts); }
}
