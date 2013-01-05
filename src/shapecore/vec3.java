package shapecore;

import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import Jama.Matrix;
import processing.core.PApplet;
import processing.core.PVector;
import shapecore.interfaces.Ring;
import shapecore.interfaces.Vector;

public class vec3 implements Vector<vec3> {
  public float x, y, z;
  static final long serialVersionUID = -33l;

  // CREATE
  public vec3() {
  }

  public vec3(vec3 V) {
    x = V.x;
    y = V.y;
  }

  public vec3(float s, vec3 V) {
    x = s * V.x;
    y = s * V.y;
  }

  public vec3(float px, float py, float pz) {
    x = px;
    y = py;
    z = pz;
  }
  
  public vec3(double px, double py, double pz) {
    x = (float) px;
    y = (float) py;
    z = (float) pz;
  }
  
  public vec3(PVector p) {
    x = p.x;
    y = p.y;
    z = p.z;
  }

  public vec3(pt3 P, pt3 Q) {
    x = Q.x - P.x;
    y = Q.y - P.y;
    z = Q.z - P.z;
  }

  // MODIFY
  public void setTo(float px, float py, float pz) {
    x = px;
    y = py;
    z = pz;
  }

  public void setTo(pt3 P, pt3 Q) {
    x = Q.x - P.x;
    y = Q.y - P.y;
    z = Q.z - P.z;
  }

  public void setTo(vec3 V) {
    x = V.x;
    y = V.y;
    z = V.z;
  }
  
  public void setToZero() {
    x = y = z = 0;
  }
  
  public vec3 get() { return new vec3(x,y,z); }

  public vec3 scaleBy(float f) {
    x *= f;
    y *= f;
    z *= f;
    return this;
  }

  public vec3 back() {
    x = -x;
    y = -y;
    z = -z;
    return this;
  }

  public vec3 mul(float f) {
    x *= f;
    y *= f;
    z *= f;
    return this;
  }

  public vec3 div(float f) {
    x /= f;
    y /= f;
    z /= f;
    return this;
  }

  public vec3 normalize() {
    float n = mag();
    if (n > 0.000001) {
      x /= n;
      y /= n;
      z /= n;
    }
    return this;
  }
  
  public vec3 normalized() {
    return get().normalize();
  }
  
  public vec3 toLength(float len) {
    float n = (float) norm();
    if (n > 0.000001) {
      float scale = len/n;
      x *= scale;
      y *= scale;
      z *= scale;
    }
    return this;  
  }


  public vec3 add(vec3 v) {
    x += v.x;
    y += v.y;
    z += v.z;
    return this;
  }
  
  public vec3 sub(vec3 v) {
    x -= v.x;
    y -= v.y;
    z -= v.z;
    return this;
  }
  
  public vec3 addScaledBy(float s, vec3 V) {
    x += s * V.x;
    y += s * V.y;
    z += s * V.z;
    return this;
  }
  
  public vec3 add(float s, vec3 V) {
    x += s * V.x;
    y += s * V.y;
    z += s * V.z;
    return this;
  }

  public vec3 add(double s, vec3 V) {
    x += s * V.x;
    y += s * V.y;
    z += s * V.z;
    return this;
  }

  public vec3 add(float u, float v, float w) {
    x += u;
    y += v;
    z += w;
    return this;
  }
  
  @Deprecated
  public vec3 addPt(pt3 P) {
    x += P.x;
    y += P.y;
    z += P.z;
    return this;
  }

  public static float dot(vec3 U, vec3 V) {
    return U.dot(V);
  }

  public float dot(vec3 that) {
    return this.x * that.x + this.y * that.y + this.z * that.z;
  }

  // OUTPUT TEST MEASURE

  public float mag() {
    return sqrt(sq(x) + sq(y) + sq(z));
  }
  public float norm() {
    return sqrt(sq(x) + sq(y) + sq(z));
  }
  public float sqnorm() {
   return sq(x) + sq(y) + sq(z);
  }
  public vec3 zero() {
    return new vec3(0,0,0);
  }
  public boolean isNull() {
    return abs(x) + abs(y) + abs(z) < 0.000001;
  }

  public void addScaled(float s, vec3 V) {x += s*V.x; y += s*V.y; z += s*V.z; }

  public vec3 parallel(vec3 axis) {
    axis = axis.normalized();
    return axis.scaleBy(axis.dot(this));
  }
    
  public vec3 orthogonal(vec3 axis) {
    vec3 result = this.get();
    result.sub(this.parallel(axis));
    return result;
  }

  public vec3 cross(vec3 that) {
    float crossX = y * that.z - that.y * z;
    float crossY = z * that.x - that.z * x;
    float crossZ = x * that.y - that.x * y;
    return new vec3(crossX, crossY, crossZ);
  }
  
  /** The skew-symmetric matrix "V x"
   * Where (V x) W = V x W
   * Returns a 4x4 (homogeneous) matrix
   */
  public Matrix cross() {
    return new Matrix(new double[][] {
        { 0,-z, y, 0},
        { z, 0,-x, 0},
        {-y, x, 0, 0},
        { 0, 0, 0, 0}
    });
  }
  
  public vec as2D() {
    return new vec(x,y);
  }
  
  public vec xy() { return new vec(x,y); }
  public vec xz() { return new vec(x,z); }
  public vec yz() { return new vec(y,z); }
  
  public String toString() {
    return "vec3("+x+", "+y+", "+z+")";
  }

  
  public static vec3 randomUnitVector(PApplet p) {
    float theta = p.random(TWO_PI*2);
    float z = p.random(-1,1);
    float r = PApplet.sqrt(1-z*z);
    return new vec3(Math.cos(theta)*r,Math.sin(theta)*r,z);
  }
}
