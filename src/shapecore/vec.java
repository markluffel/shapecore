package shapecore;

import static processing.core.PApplet.*;
import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import shapecore.interfaces.Vector;

public class vec implements Vector<vec>, Serializable {
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

  @Deprecated
  public void setTo(float px, float py) {
    x = px;
    y = py;
  }

  @Deprecated
  public void setTo(pt P, pt Q) {
    x = Q.x - P.x;
    y = Q.y - P.y;
  }

  @Deprecated
  public void setTo(vec V) {
    set(V);
  }
  
  public vec set(vec V) {
    x = V.x;
    y = V.y;
    return this;
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

  public vec mul(float f) {
    x *= f;
    y *= f;
    return this;
  }

  public vec div(float f) {
    x /= f;
    y /= f;
    return this;
  }

  public vec scaleBy(float u, float v) {
    x *= u;
    y *= v;
    return this;
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
  
  public vec add(double x_, double y_) {
    this.x += x_;
    this.y += y_;
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
  
  public static float dot(vec u, vec v) {
    return u.x * v.x + u.y * v.y;
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

  public vec rotateBy(float a) {
    float xx = x, yy = y;
    float ca = cos(a), sa = sin(a);
    x = (float)(xx * ca - yy * sa);
    y = (float)(xx * sa + yy * ca);
    return this;
  }

  // OUTPUT VEC
  public vec makeClone() {
    return (new vec(x, y));
  }

  public vec makeOffsetVec(vec v) {
    return (new vec(x + v.x, y + v.y));
  }

  public vec makeOffsetVec(float s, vec v) {
    return (new vec(x + s * v.x, y + s * v.y));
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
  
  @Deprecated // sqnorm
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
  public vec addScaled(float s, vec v) {x += s*v.x; y += s*v.y; return this;}

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
  
  public List<vec> diffs(List<vec> a, List<vec> b) {
    List<vec> result = new ArrayList<vec>();
    int len = min(a.size(), b.size());
    for(int i = 0; i < len; i++) {
      result.add(a.get(i).get().add(-1, b.get(i)));
    }
    return result;
  }
  
  static float[] lengths(List<vec> vecs) {
    float[] result = new float[vecs.size()];
    for(int i = 0; i < result.length; i++) {
      result[i] = vecs.get(i).norm();
    }
    return result;
  }

  static float[] angles(List<vec> vecs) {
    float[] result = new float[vecs.size()];
    for(int i = 0; i < result.length; i++) {
      result[i] = vecs.get(i).angle();
    }
    return result;
  }
  
  static List<vec> laplacian(List<vec> vecs) {
    List<vec> result = new ArrayList<vec>();
    result.add(new vec(0,0));
    for(int i = 1; i < vecs.size()-1; i++) {
      vec a = vecs.get(i-1), b = vecs.get(i), c = vecs.get(i+1);
      result.add(a.get().add(c).scaleBy(0.25f).add(-0.5f, b));
    }
    result.add(new vec(0,0));
    return result;
  }
  
  public static void smooth(List<vec> vecs) {
    // TODO: make this more memory efficient
    List<vec> lap = laplacian(vecs);
    for(int i = 0; i < vecs.size(); i++) {
      vecs.get(i).add(0.5f, lap.get(i));
    }
  }

  public static void bismooth(List<vec> vecs) {
    List<vec> bilap = laplacian(laplacian(vecs));
    for(int i = 0; i < vecs.size(); i++) {
      vecs.get(i).add(-0.5f, bilap.get(i));
    }
  }
  
  public static void smoothRadial(List<vec> vecs, int iterations) {
    float[] lens = vec.lengths(vecs);
    float[] angles = vec.angles(vecs);
    for( int i = 0; i < iterations; i++) Oplet.smooth(lens, 0.5f);
    for(int i = 0; i < iterations; i++) smoothAngles(angles, 0.5f);
    
    for(int i = 0; i < vecs.size(); i++) {
      vecs.set(i, fromRadial(lens[i], angles[i]));
    }
  }
  
  public static float sumOfSquares(Collection<vec> errs) {
    float sum = 0;
    for(vec v : errs) sum += v.sqnorm();
    return sum;
  }
  
  public static vec sum(Collection<vec> vs) {
    vec sum = new vec();
    for(vec v : vs) sum.add(v);
    return sum;
  }
  
  public static vec average(Collection<vec> vs) {
    return sum(vs).scaleBy(1f/vs.size());
  }
}
