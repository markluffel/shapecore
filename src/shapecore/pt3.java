package shapecore;

import static processing.core.PApplet.*;
import static shapecore.Geometry.*;
import shapecore.interfaces.Ring;

public final class pt3 implements Ring<pt3> {
  public float x, y, z;
  static final long serialVersionUID = -33l;

  // CREATE
  public pt3() {
  }

  public pt3(float px, float py, float pz) {
    x = px;
    y = py;
    z = pz;
  }
  
  public pt3(double px, double py, double pz) {
    x = (float) px;
    y = (float) py;
    z = (float) pz;
  }

  public pt3(pt3 P) {
    x = P.x;
    y = P.y;
    z = P.z;
  }

  public pt3(pt3 P, vec3 V) {
    x = P.x + V.x;
    y = P.y + V.y;
    z = P.z + V.z;
  }
  public pt3(pt3 P, float s, vec3 V) {
    x = P.x + s * V.x;
    y = P.y + s * V.y;
    z = P.z + s * V.z;
  }
  public pt3(pt3 A, float s, pt3 B) {
    x = A.x + s * (B.x - A.x);
    y = A.y + s * (B.y - A.y);
    z = A.z + s * (B.z - A.z);
  }

  // MODIFY
  public void setTo(float px, float py, float pz) {
    x = px;
    y = py;
    z = pz;
  }

  public void setTo(pt3 P) {
    x = P.x;
    y = P.y;
    z = P.z;
  }
  
  public pt3 get() { return new pt3(x, y, z); }
  
  public pt3 scaleBy(float f) {
    x *= f;
    y *= f;
    z *= f;
    return this;
  }
  
  public pt3 scaleBy(double f) {
    x *= f;
    y *= f;
    z *= f;
    return this;
  }

  public pt3 add(vec3 v) {
    x += v.x;
    y += v.y;
    z += v.z;
    return this;
  }
  
  public pt3 add(float x_, float y_, float z_) {
    x += x_;
    y += y_;
    z += z_;
    return this;
  }

  public pt3 add(float s, vec3 v) {
    x += s * v.x;
    y += s * v.y;
    z += s * v.z;
    return this;
  }
  
  public pt3 addPt(pt3 P) {
    x += P.x;
    y += P.y;
    z += P.z;
    return this;
  }
  
  @Deprecated // for conformance to the Ring interface
  public pt3 add(pt3 P) {
    x += P.x;
    y += P.y;
    z += P.z;
    return this;
  }
  
  public pt3 addScaledBy(float s, pt3 P) {
    x += s * P.x;
    y += s * P.y;
    z += s * P.z;
    return this;
  }
  
  public pt3 translateBy(vec3 v) {
    x += v.x;
    y += v.y;
    z += v.z;
    return this;
  }
  
  public pt3 translateBy(float s, vec3 v) {
    x += s * v.x;
    y += s * v.y;
    z += s * v.z;
    return this;
  }
  
  public pt3 translateBy(double s, vec3 v) {
    return translateBy((float)s, v);
  }
  
  public pt3 translateBy(float u, float v, float w) {
    x += u;
    y += v;
    z += w;
    return this;
  }
  
  public pt3 translateTowardsByRatio(float s, pt3 P) {
    x += s * (P.x - x);
    y += s * (P.y - y);
    z += s * (P.z - z);
    return this;
  }

  public pt3 translateTowardsBy(double s, pt3 P) {
    return translateTowardsBy((float)s, P);
  }
  
  public pt3 translateTowardsBy(float s, pt3 P) {
    return translateBy(s, makeVecTo(P).normalize());
  }

  /*
  public pt3 makeProjectionOnLine(pt3 P, pt3 Q) {
    float a = dot(P.makeVecTo(this), P.makeVecTo(Q)), b = dot(P.makeVecTo(Q),
        P.makeVecTo(Q));
    return (P.get().translateTowards(a / b, Q));
  }
  */

  public vec3 makeVecTo(pt3 P) {
    return new vec3(P.x - x, P.y - y, P.z - z);
  }
  
  public vec3 makeVecToAverage(pt3 P, pt3 Q) {
    return new vec3(
        (P.x + Q.x) / 2f - x,
        (P.y + Q.y) / 2f - y,
        (P.z + Q.z) / 2f - z
    );
  }

  public vec3 makeVecToAverage(pt3 P, pt3 Q, pt3 r) {
    return new vec3(
        (P.x + Q.x + r.x) / 3f - x,
        (P.y + Q.y + r.x) / 3f - y,
        (P.z + Q.z + r.z) / 3f - y
    );
  }
  
  // OUTPUT TEST OR MEASURE
  public float disTo(pt3 P) {
    return sqrt(sq(P.x - x) + sq(P.y - y) + sq(P.z - z));
  }
  public float dist(pt3 P) {
    return sqrt(sq(P.x - x) + sq(P.y - y) + sq(P.z - z));
  }
  
  public float sqdist(pt3 P) {
    return sq(P.x - x) + sq(P.y - y) + sq(P.z - z);
  }
  
  public float sqDisTo(float _x, float _y, float _z) {
    return sq(_x - x) + sq(_y - y) + sq(_z - z);
  }
  
  public boolean projectsBetween(pt3 P, pt3 Q) {
    float
    a = dot(P.makeVecTo(this), P.makeVecTo(Q)),
    b = dot(P.makeVecTo(Q), P.makeVecTo(Q));
    return 0 < a && a < b;
  }

  public boolean isZero() {
    return x == 0 && y == 0 && z == 0;
  }
  
  public pt as2D() {
    return new pt(x,y);
  }
  
  public pt xy() { return new pt(x,y); }
  public pt xz() { return new pt(x,z); }
  public pt yz() { return new pt(y,z); }
  
  public String toString() {
    return "pt3("+x+", "+y+", "+z+")";
  }
  
  public void transform(pt3 O, vec3 I, vec3 J, vec3 K) {
    this.setTo(
        O.x + x*I.x + y*J.x + z*K.x,
        O.y + x*I.y + y*J.y + z*K.y,
        O.z + x*I.z + y*J.z + z*K.z);
  }

  public void transform(Affinity3D aff) {
    this.setTo(
        aff.O.x + x*aff.I.x + y*aff.J.x + z*aff.K.x,
        aff.O.y + x*aff.I.y + y*aff.J.y + z*aff.K.y,
        aff.O.z + x*aff.I.z + y*aff.J.z + z*aff.K.z);
  }

  
  // terrible things that you shouldn't do....

  @Deprecated 
  public float _sqnorm() {
    return sq(x)+sq(y)+sq(z);
  }

  @Deprecated // try not to this, unless it's more convenient
  public float _norm() {
    return sqrt(_sqnorm());
  }
  
  public pt3 zero() {
    return new pt3(0,0,0);
  }

  public String flatString() {
    return x+" "+y+" "+z;
  }
}
