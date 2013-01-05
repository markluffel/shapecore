package shapecore.interact;

import processing.opengl.PGraphicsOpenGL;
import shapecore.Oplet;
import shapecore.vec3;



public class LazySusanApplet extends Oplet {

  public float cameraDist = 30;
  public vec3 center = new vec3(0,0,0);
  public vec3 view = new vec3(0,1,0);
  public vec3 up = new vec3(0,0,1);
  public float speed = 0.01f;
  
  public void lazySusan() {
    camera(
        center.x+view.x*cameraDist,center.y+view.y*cameraDist,center.z+view.z*cameraDist,
        center.x,center.y,center.z,
        up.x,up.y,up.z);
    ((PGraphicsOpenGL)g).cameraNear = 1;
    ((PGraphicsOpenGL)g).cameraFar = 200;
    perspective();
  }
  
  public void mouseDragged() {
    int dx = mouseX-pmouseX;
    int dy = mouseY-pmouseY;
    vec3 h = view.cross(up);
    vec3 v = view.cross(h);
    view.add(dx*speed, h);
    view.add(dy*speed, v);
    view.normalize();
  }

  /*
  // clipping plane
((PGraphics3D)g).cameraNear = 5;
((PGraphics3D)g).cameraFar = 200;
perspective();
   */
  
  // demo
  /*
  public void draw() {
    background(0);
    lights();
    noStroke();
    arcball();
    box(5);
  }
  
  public void setup() {
    size(640, 480, OPENGL);
  }
  */
}
