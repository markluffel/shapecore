package shapecore;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;
import shapecore.interfaces.Ring;
import shapecore.interfaces.Vector;
import Jama.Matrix;

public class vec implements Vector<vec> {
  public float x = 0, y = 0;
  static final long serialVersionUID = -33l;

  // CREATE
  public vec() {
  }

  public vec(vec V) {
    x = V.x;
    y = V.y;
  }

  public vec(float s, vec V) {
    x = s * V.x;
    y = s * V.y;
  }

  public vec(float px, float py) {
    x = px;
    y = py;
  }

  public vec(double px, double py) {
    x = (float)px;
    y = (float)py;
  }

  public vec(pt P, pt Q) {
    x = Q.x - P.x;
    y = Q.y - P.y;
  }

  // MODIFY
  public void setTo(float px, float py) {
    x = px;
    y = py;
  }

  public void setTo(pt P, pt Q) {
    x = Q.x - P.x;
    y = Q.y - P.y;
  }

  public void setTo(vec V) {
    x = V.x;
    y = V.y;
  }

  public vec scaleBy(float f) {
    x *= f;
    y *= f;
    return this;
  }

  public vec back() {
    x = -x;
    y = -y;
    return this;
  }
  
  public vec makeBack() {
    return new vec(-x,-y);
  }

  public void mul(float f) {
    x *= f;
    y *= f;
  }

  public void div(float f) {
    x /= f;
    y /= f;
  }

  public void scaleBy(float u, float v) {
    x *= u;
    y *= v;
  }

  public vec normalize() {
    float n = (float) Math.sqrt(sq(x) + sq(y));
    if (n > 0.000001) {
      x /= n;
      y /= n;
    }
    return this;
  }

  public vec add(vec v) {
    x += v.x;
    y += v.y;
    return this;
  }

  public vec addScaledBy(float s, vec v) {
    x += s * v.x;
    y += s * v.y;
    return this;
  }

  public vec add(float s, vec v) {
    x += s * v.x;
    y += s * v.y;
    return this;
  }

  public vec add(float u, float v) {
    x += u;
    y += v;
    return this;
  }
  
  public vec add(double x, double y) {
    this.x += x;
    this.y += y;
    return this;
  }

  public vec turnLeft() {
    float w = x; x = -y; y = w;
    return this;
  }
  
  public vec turnRight() {
    float w = x; x = y; y = -w;
    return this;
  }
  
  public static float dot(vec U, vec V) {
    return U.x * V.x + U.y * V.y;
  }

  public float dot(vec that) {
    return this.x * that.x + this.y * that.y;
  }
  
  public vec perp() {
    return new vec(-y, x);
  }
  
  // perp of this, then dot with that
  public double perpDot(vec that) {
    return this.y*that.x - this.x*that.y;
  }
  
  // perp of that, then dot with this
  public double dotPerp(vec that) {
    return this.x*that.y - this.y*that.x;
  }
  
  // same as dot
  public double perpDotPerp(vec that) {
    return this.x*that.x + this.y*that.y;
  }

  public void rotateBy(float a) {
    float xx = x, yy = y;
    x = (float)(xx * Math.cos(a) - yy * Math.sin(a));
    y = (float)(xx * Math.sin(a) + yy * Math.cos(a));
  }

  // OUTPUT VEC
  public vec makeClone() {
    return (new vec(x, y));
  }

  /*
  public vec makeUnit() {
    float n = (float) Math.sqrt(sq(x) + sq(y));
    if (n < 0.000001)
      n = 1;
    return new vec(x / n, y / n);
  }

  public vec unit() {
    float n = (float) Math.sqrt(sq(x) + sq(y));
    if (n < 0.000001)
      n = 1;
    return new vec(x / n, y / n);
  }
  */

  //public vec makeScaledBy(float s) { return new vec(x * s, y * s); }
  //public vec makeTurnedLeft() { return new vec(-y, x); }
  //public vec left() { return new vec(-y, x); }
  //public vec right() { return new vec(y, -x); }

  public vec makeOffsetVec(vec V) {
    return (new vec(x + V.x, y + V.y));
  }

  public vec makeOffsetVec(float s, vec V) {
    return (new vec(x + s * V.x, y + s * V.y));
  }

  public vec makeOffsetVec(float u, float v) {
    return (new vec(x + u, y + v));
  }

  public vec makeRotatedBy(float a) {
    return new vec((float)(x * Math.cos(a) - y * Math.sin(a)), (float)(x * Math.sin(a) + y * Math.cos(a)));
  }

  public vec makeReflectedVec(vec N) {
    return makeOffsetVec(-2f * this.dot(N), N);
  }

  // OUTPUT TEST MEASURE
  public float norm() {
    return sqrt(sq(x) + sq(y));
  }
  
  public float norm2() {
    return sq(x) + sq(y);
  }
  
  public boolean isNull() {
    return ((Math.abs(x) + Math.abs(y) < 0.000001));
  }

  public float angle() {
    return (float) Math.atan2(y, x);
  }
  
  public vec make() { return get(); }
  public vec clone() { return get(); }
  public vec get() { return new vec(x,y); }
  public void addScaled(float s, vec V) {x += s*V.x; y += s*V.y;}

  // this is weird, what are you trying to do here?
  public vec parallel(vec axis) {
    vec _axis = new vec(axis);
    _axis.normalize();
    _axis.scaleBy(_axis.dot(this));
    return _axis;
  }

  // also weird, why not static... perp to what, with length what?
  public vec perpendicular(vec axis) {
    vec _axis = new vec(axis);
    _axis.normalize();
    _axis = R(_axis);
    _axis.scaleBy(_axis.dot(this));
    return _axis;
  }

  public float sqnorm() {
    return sq(x)+sq(y);
  }
  
  public vec zero() {
    return new vec(0,0);
  }

  public vec3 as3D() {
    return new vec3(x,y,0);
  }

  public vec toLength(float len) {
    float n = (float) Math.sqrt(sq(x) + sq(y));
    if (n > 0.000001) {
      float scale = len/n;
      x *= scale;
      y *= scale;
    }
    return this;
  }

  public String toString() {
    return "vec("+x+", "+y+")";
  }

  public static vec perp(pt a, pt b) {
    return new vec(b.y-a.y, a.x-b.x);
  }
  
  public static vec perpNormalized(pt a, pt b) {
    vec v = new vec(b.y-a.y, a.x-b.x);
    v.normalize();
    return v;
  }
}
