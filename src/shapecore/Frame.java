package shapecore;


import static shapecore.Geometry.*;

import java.io.Serializable;


public class Frame implements Serializable {
  static final long serialVersionUID = -33l;
  public pt pos = new pt();
  public float angle;
  
  public static Frame make(pt pos, float angle) {
    Frame f = new Frame();
    f.pos.set(pos);
    f.angle = angle;
    return f;
  }
  
  /** Make a frame aligned with the vector between these two points, centered at their midpoint */
  public static Frame make(pt a, pt b) {
    return Frame.make(a, b, 0.5f);
  }
  
  /** Make a frame aligned with the vector between two points, center at the interpolation of the points by t*/
  public static Frame make(pt a, pt b, float t) {
    return Frame.make(Geometry.L(a, t, b), V(a, b).angle()); // TODO: rename lerp, reorder parameters    
  }
  
  public Frame get() {
    return Frame.make(pos, angle);
  }

  public void lerp(Frame goal, float amount) {
    pos.translateTowardsByRatio(amount, goal.pos);
    angle = Oplet.circular_lerp(angle, goal.angle, amount);
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
    return R(v, angle);
  }
  
  public Frame toLocal(Frame that) {
    return Frame.make(P(toLocal(that.pos)), Oplet.angle_diff(this.angle, that.angle));
  }
  
  public vec toLocal(pt p) {
    vec v = V(p,pos);
    v.rotateBy(-angle);
    return v;
  }

  public void rotate(float angleDelta) {
    rotate(pos, angleDelta);
  }
  public void rotate(pt center, float angleDelta) {
    pos.setTo(T(center,R(V(center,pos),angleDelta)));
    angle += angleDelta;
  }

  public void set(Frame frame) {
    pos.set(frame.pos);
    angle = frame.angle;
  }

  public Frame translateLocal(float x, float y) {
    pos.add(toGlobalVector(new vec(x,y)));
    return this;
  }
  public Frame translateGlobal(vec v) {
    pos.add(v);
    return this;
  }
  // TODO: would be nice to have a point/vector equivalence with Frame, like Frame/RigidXform
  public Frame subtract(Frame that) { // TODO: spiral interpolation between two frames
    return Frame.make(P(V(that.pos, this.pos)), Oplet.angle_diff(that.angle, this.angle));
  }
  public Frame add(Frame that) {
    return Frame.make(P(S(V(that.pos), V(this.pos))), this.angle + that.angle);
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