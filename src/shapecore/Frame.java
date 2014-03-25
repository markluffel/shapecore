package shapecore;


import static shapecore.Geometry.*;

import java.io.Serializable;

// TODO: make a frame with scaling
public class Frame implements Serializable {
  static final long serialVersionUID = -33l;
  
  public pt pos = new pt();
  public float angle;
  
  public static Frame make(pt pos, float angle) {
    Frame f = new Frame();
    f.pos = pos;
    f.angle = angle;
    return f;
  }
  
  /** Make a frame aligned with the vector between these two points, centered at their midpoint */
  public static Frame make(pt a, pt b) {
    return Frame.make(a, b, 0.5f);
  }
  
  /** Make a frame aligned with the vector between two points, center at the interpolation of the points by t*/
  public static Frame make(pt a, pt b, float t) {
    return Frame.make(Geometry.lerp(a, b, t), a.to(b).angle());    
  }
  
  public Frame get() {
    return Frame.make(pos.get(), angle);
  }

  public void lerp(Frame goal, float amount) {
    pos.translateTowardsByRatio(amount, goal.pos);
    angle = Oplet.circularLerp(angle, goal.angle, amount);
    //scale = Oplet.geometricLerp(this.scale, goal.scale, amount);
  }
  public void lerp(pt _pos, float amount) {
    pos.translateTowardsByRatio(amount, _pos);
  }

  public pt toGlobal(vec v) {
    return T(pos, R(v, angle));
  }
  public pt toGlobal(vec v, float angleDelta) {
    return T(pos, R(v, angle+angleDelta));
  }
  public vec toGlobalVector(vec v) {
    return v.get().rotateBy(angle);
  }
  
  /** Returns the given frame in this Frame's local coordinate system. */ 
  public Frame toLocal(Frame that) {
    return Frame.make(P(toLocal(that.pos)), Oplet.angleDiff(this.angle, that.angle));
  }
  
  /** Returns the given point in this Frame's local coordinate system. */
  public vec toLocal(pt p) {
    return p.to(pos).rotateBy(-angle);
  }

  public void rotate(float angleDelta) {
    rotate(pos, angleDelta);
  }
  public void rotate(pt center, float angleDelta) {
    pos.set(center).add(center.to(pos).rotateBy(angleDelta));
    angle += angleDelta;
  }

  public void set(Frame frame) {
    pos.set(frame.pos);
    angle = frame.angle;
  }

  public Frame translateLocal(float x, float y) {
    translateLocal(new vec(x,y));
    return this;
  }
  public Frame translateLocal(vec v) {
    pos.add(toGlobalVector(v));
    return this;
  }
  public Frame translateGlobal(vec v) {
    pos.add(v);
    return this;
  }
  
  /** Copy data from the values array, starting at i into this frame .
   * Return i of next available location in array */
  public int apply(float[] values, int i) {
    pos.x = values[i++];
    pos.y = values[i++];
    angle = values[i++];
    return i;
  }
  /** Copy data from this frame into the values array, starting at i.
   *  Return i of next available location in array */
  public int store(float[] values, int i) {
    values[i++] = pos.x;
    values[i++] = pos.y;
    values[i++] = angle;
    return i;
  }

  public vec I() { return toGlobalVector(V(0,1)); }
  public vec J() { return toGlobalVector(V(1,0)); }
  
}