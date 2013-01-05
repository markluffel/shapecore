package shapecore.interact;

import shapecore.pt3;
import shapecore.vec3;

public interface CameraInterface {
  
  /** Setup the camera projection of the controlled PApplet */
  void camera();
  /** Respond to mouse press at the given coordinates */
  void mousePressed(float mx, float my);
  /** Respond to mouse drag at the given coordinates */
  void mouseDragged(float mx, float my);
  /** Respond to window resize */
  void resize(int width, int height);
  /** Reset to default view */
  void reset();
  /** Apply a rotation (in Euler angles) to the camera */
  void rotate(float x, float y, float z);
  /** Do any animation before draw method */
  void tick(); 

  /** Get the center of rotation and focus point */
  pt3 getFocus();
  /** Set the center of rotation and focus point */
  void setFocus(pt3 p);

  /** Get the distance from the camera eye to the focus */
  float getDistance();
  /** Set the distance from the camera eye to the focus */
  void setDistance(float d);
  
  /** Get the location of the camera */
  pt3 getEye();
  /** Get the camera's up vector */
  vec3 getUp();
  
  /** Install hook in controlled PApplet */
  void listenForWheel();  
}
