package shapecore.interact;

import java.awt.event.KeyEvent;

import shapecore.Oplet;
import shapecore.pt;

import static shapecore.Geometry.*;


public abstract class ZoomApplet extends Oplet {

  public abstract void drawZoomed();

  float zoomIncrement = 0.75f;
  
  float zoomScale = 1;
  pt zoomCenter = new pt(0,0);
  float zoomScaleGoal = 1;
  
  public final void draw() {
    // TODO, use apple zoom style behavior,
    // keep track of ... something else ... like a center that gets moved by dmouseX
    if(!mousePressed) {
      zoomCenter = lerp(zoomCenter, new pt(mouseX, mouseY), 0.1f);
      zoomScale = lerp(zoomScale, zoomScaleGoal, 0.2f);
    }
    translate(zoomCenter.x,zoomCenter.y);
    scale(zoomScale);
    translate(-zoomCenter.x,-zoomCenter.y);
    
    drawZoomed();
  }
  
  public void init() {
    super.init();
    registerKeyEvent(new Nothing());
  }
    
  // seems like this can't be an anonymous object
  // related to processing's reflection hackery
  public class Nothing {
    public void keyEvent(KeyEvent e) {
      if(e.getID() == KeyEvent.KEY_PRESSED) {
        if(e.getKeyChar() == '=') {
          zoomScaleGoal /= zoomIncrement;
        } else if(e.getKeyChar() == '-') {
          if(zoomScaleGoal > 1) {
            zoomScaleGoal *= zoomIncrement;
          }
        }
      }
    }
  }
}
