package shapecore.interact;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import processing.core.PApplet;
import shapecore.Quaternion;
import shapecore.pt3;
import shapecore.vec3;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ArcBall implements CameraInterface {

  float zoomDistance;
  Quaternion q_now;
  
  float minZoom, maxZoom;
  transient float radius;
  transient float flip;
  transient pt3 center;
  transient vec3 v_down, v_drag;
  transient Quaternion q_down, q_drag;
  transient pt3 eye = P(0,0,0), focus; // eye, focus
  transient vec3 up = V(0,0,0); // up

  transient PApplet parent;
  
  public ArcBall(PApplet parent) {
    this(parent, P(parent.width/2, parent.height/2, 0));
  }
  
  /** defaults to radius of min(width/2,height/2) and center_z of -radius */
  public ArcBall(PApplet parent, pt3 _focus) {
    this.parent = parent;
    this.center = P(0,0,0);
    resize(parent.width, parent.height);
    focus = _focus.get();
    reset();
  }
  
  public void reset() {
    zoomDistance = min(parent.width,parent.height);
    flip = 1;
    minZoom = 0.5f;
    maxZoom = 10;

    v_down = V(0,0,0);
    v_drag = V(0,0,0);
    
    q_now = new Quaternion();
    q_down = new Quaternion();
    q_drag = new Quaternion();
    updateView();
  }
  
  public void mousePressed() { mousePressed(parent.mouseX, parent.mouseY); }
  public void mouseDragged() { mouseDragged(parent.mouseX, parent.mouseY); }
  public void resize(int width, int height) {
    center.setTo(width/2.0f, height/2.0f, -PApplet.min(width/2.0f,height/2.0f));
    radius = (width+height)/4;
  }
  
  public void mousePressed(float mouseX, float mouseY) {
    v_down = mouse_to_sphere(mouseX, mouseY);
    q_down.set(q_now);
    q_drag.reset();
  }
  
  public void mouseDragged(float mouseX, float mouseY) {
    v_drag = mouse_to_sphere(mouseX, mouseY);
    q_drag.set(dot(v_down, v_drag), v_down.cross(v_drag));
    q_now = mul(q_drag, q_down);
    // focus stays fixed, eye and up vectors updated
    updateView();
  }
  
  public void listenForWheel() {
    parent.addMouseWheelListener(new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        zoom(e.getWheelRotation());
      }
    });
  }
  
  public void listenForMouse() {
    //parent.registerMouseEvent(this);
    throw new NotImplementedException();
    // TODO - or not, given that often we want to customize
    // when the arcball is given control, and when the sketch has control
  }
  
  /** Get the distance from eye to focus */
  public float getDistance() {
    return zoomDistance;
  }
  
  /** Set distance from eye to focus */
  public void setDistance(float distance) {
    zoomDistance = constrain(distance, minZoom, maxZoom);
    updateView();
  }
  
  public void zoom(int amount) {
    setDistance(zoomDistance * pow(1.05f, -amount));
    updateView();
  }
  
  public void setFocus(pt3 p) {
    focus = p.get();
    updateView();
  }
  
  public void moveFocus(float x, float y, float z) {
    focus.add(x, y, z);
    updateView();
  }
  
  public void camera() { 
    parent.camera(
        eye.x,eye.y,eye.z,
        focus.x,focus.y,focus.z,
        up.x,up.y,up.z);
  }
  
  public float[] getState() {
    return new float[] {q_now.w, q_now.x, q_now.y, q_now.z, zoomDistance};
  }
  
  public void setState(float[] vals) {
    q_now.set(vals[0], new vec3(vals[1], vals[2], vals[3]));
    zoomDistance = vals[4];
    updateView();
  }

  void updateView() {
    eye.setTo(T(focus,rotateVector(0,0,zoomDistance)));
    up.setTo(rotateVector(0,flip,0));
  }

  vec3 mouse_to_sphere(float x, float y) {
    vec3 v = new vec3();
    v.x = (x - center.x) / radius * flip;
    v.y = (y - center.y) / radius * flip;

    float mag = v.x * v.x + v.y * v.y;
    if(mag > 1.0f) {
      v.normalize();
    } else {
      v.z = PApplet.sqrt(1.0f - mag);
    }

    return v;
    // TODO: figure out how to use the axis part
    //return (axis == -1) ? v : constrain_vector(v, axisSet[axis]);
  }
  
  vec3 constrain_vector(vec3 vector, vec3 _axis) {
    return V(S(dot(_axis, vector), _axis), vector).normalize();
  }
  
  vec3 rotateVector(float x, float y, float z) { return rotateVector(V(x,y,z)); }
  vec3 rotateVector(vec3 v) { return q_now.rotate(v); }

  public void rotateBetween(vec3 u, vec3 v) {
    q_drag.set(dot(u, v), u.cross(v));
    q_now = mul(q_drag, q_now);
    updateView();
  }

  /** Flip the up direction */
  public void flipUp() { flip = -flip; updateView(); }

  public pt3 getEye() { return eye.get(); }
  public pt3 getFocus() { return focus.get(); }
  public vec3 getUp() { return up.get(); }

  public void rotate(float x, float y, float z) {
    Quaternion r = Quaternion.fromEuler(x,y,z);
    q_now = mul(r, q_now);
  }
  public void tick() {}
}