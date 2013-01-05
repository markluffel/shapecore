package shapecore.ink;


import java.util.List;

import shapecore.Oplet;
import shapecore.pt;
import shapecore.ink.WingedStrokeGraph.Stroke;

import static shapecore.Geometry.*;


public class BuildGraphDemo extends Oplet {

  public void draw() {
    background(255);
    stroke(0); noFill();
    g.draw(this);
  }
  
  public void keyPressed() {
    if(key == ' ') {
      dataSetup();
    }
  }
  
  public void dataSetup() {
    g = WingedStrokeGraph.build(new pt[] {
        new pt(100,450),
        new pt(200,100),
        new pt(400,400),
        new pt(400,200),
        new pt(500,100),
    }, new int[][] {
        {1,2},
        {2,2,3,4},
        {3},
        {},
        {2}
    });
    
    for(Stroke s : g.strokes) {
      jitter(s.points.subList(1, s.points.size()-2), 40);
      smoothPolyline(s.points, 0.5);
      smoothPolyline(s.points, 0.5);
    }
  }
  
  WingedStrokeGraph g;
  public void setup() {
    size(640,480);
    dataSetup();
  }
  
  void jitter(List<pt> pts, float amount) {
    for(pt p : pts) {
      p.x += random(-amount,amount);
      p.y += random(-amount,amount);
    }
  }
}
