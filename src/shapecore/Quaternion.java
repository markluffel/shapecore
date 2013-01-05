package shapecore;

import static shapecore.Geometry.*;
import processing.core.PApplet;

public class Quaternion {
  public float w, x, y, z;
  
  public Quaternion() {
    reset();
  }

  public Quaternion(float w, float x, float y, float z) {
    this.w = w;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public void reset() {
    w = 1.0f;
    x = 0.0f;
    y = 0.0f;
    z = 0.0f;
  }

  public void set(float w, vec3 v) {
    this.w = w;
    x = v.x;
    y = v.y;
    z = v.z;
  }

  public void set(Quaternion q) {
    w = q.w;
    x = q.x;
    y = q.y;
    z = q.z;
  }

  public AxisAngle getAxisAngle() {
    // transforming this quat into an angle and an axis vector...
    float sa = (float) Math.sqrt(1.0f - w * w);
    if(sa < PApplet.EPSILON) {
      sa = 1.0f;
    }
    return new AxisAngle(new vec3(x / sa, y / sa, z / sa), (float) Math.acos(w) * 2.0f);
  }

  public vec3 rotate(vec3 v) {
    // TODO: skip conversion, apply directly
    AxisAngle aa = getAxisAngle();
    return R(v, -aa.angle, aa.axis);
  }

  public static Quaternion fromEuler(float x, float y, float z) {
    Rotation ro = new Rotation(x,y,z);
    ro.toQuaternion();
    return new Quaternion((float)ro.angle, (float)ro.x, (float)ro.y, (float)ro.z);
  }
}
