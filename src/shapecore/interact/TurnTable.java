package shapecore.interact;

import static shapecore.Oplet.*;
import processing.core.PApplet;
import shapecore.Quaternion;

public class TurnTable extends ArcBall {

  public TurnTable(PApplet parent) {
    super(parent);
  }
  float scaleY, scaleX;
  float deadZone;
  
  float angle, azimuth;
  private float startAngle, startAzimuth;
  private float startMouseX, startMouseY;
  private float lastMouseX, lastMouseY;
  private boolean active = false; // outside of deadZone

  public void mousePressed(float mouseX, float mouseY) {
    startMouseX = mouseX;
    startMouseY = mouseY;
    startAngle = angle;
    startAzimuth = azimuth;
    active = false;
  }
  
  public void mouseDragged(float mouseX, float mouseY) {
    if(abs(startMouseX-mouseX) > parent.width*deadZone
    || abs(startMouseY-mouseY) > parent.height*deadZone) {
      active = true;
    }
    this.lastMouseX = mouseX;
    this.lastMouseY = mouseY;
    tick();
  }
  
  public void tick() {
    if(active) {
      float minAz = asin(getFocus().y/getFocus().dist(getEye()));
      float newAngle = startAngle + (lastMouseX-startMouseX)*scaleX;
      float newAzimuth = constrain(startAzimuth + (lastMouseY-startMouseY)*scaleY, -minAz+0.1f, PI/2);
      if(abs(angle-newAngle) < 0.001 && abs(azimuth-newAzimuth) < 0.001) {
        active = false;
      } else {
        angle = lerp(angle, newAngle, 0.5f);
        azimuth = lerp(azimuth, newAzimuth, 0.5f);
      }
      updateView();
    }
  }
  
  public void reset() { // called by constructor
    azimuth = angle = 0;
    scaleY = 0.002f;
    scaleX = -0.004f;
    deadZone = 0.01f;
    super.reset();
  }
  
  public void updateView() {
    Quaternion x = Quaternion.fromEuler(0, angle, 0);
    Quaternion y = Quaternion.fromEuler(azimuth, 0, 0);
    q_now = mul(y,mul(x, q_down));
    super.updateView();
  }
  
  public void rotate(float x, float y, float z) {
    angle += y;
    azimuth += x;
    updateView();
  }
  
  public String toString() {
    return "angle "+angle+" azimuth "+azimuth+" distance "+zoomDistance;
  }
}
