package shapecore.ink;

import shapecore.Oplet;
import shapecore.pt;
import shapecore.vec;
import static shapecore.Geometry.*;

public class StrokeGraphEditor extends Oplet {

  StrokeGraph graph;
  pt[] pts;
  vec[][] handles;
  pt[] draggableHandles;
  pt dragging;
  public void draw() {
    background(0);
    stroke(255); noFill();
    graph.draw(this);
    
    /*
    ellipseMode(RADIUS);
    for(int i = 0; i < handles.length; i++) {
      for(int j = 0; j < handles[i].length; j++) {
        pt end = T(pts[topology[i][j]], handles[i][j]);
        
        stroke(255,200,0);
        line(pts[topology[i][j]], end);
        fill(255,200,0); noStroke();
        circle(end, 3);
      }
    }
    */
    fill(200,0,255);
    stroke(200,0,255);
    
    graph.arrows(this);
    
    noStroke();
    fill(255,200,0);
    graph.label(this);
    textAlign(CENTER);
  }
  
  public void mousePressed() {
    dragging = closestPoint(mouseX, mouseY, pts, draggableHandles); // no handles yet
  }
  
  public void mouseDragged() {
    if(dragging != null) {
      dragging.x += mouseX-pmouseX;
      dragging.y += mouseY-pmouseY;
      fromDraggable();
      graph = StrokeGraph.build(pts, topology, handles);
    }
  }
  
  public void mouseReleased() {
    dragging = null;
    
    println("pts = new pt[] {");
    for(pt p : pts) {
      println("new pt"+p+",");
    }
    println("};\n");
    println("handles = new vec[][] {");
    for(vec[] vv : handles) {
      print("{");
      for(vec v : vv) {
        print("new vec("+v.x+", "+v.y+"), ");
      }
      println("},");
    }
    println("};\n\n\n");
    
    /*
    println("\n\n************\n\n");
    for(String line : graph.toRepr()) {
      println(line);
    }
    println("\n\n************\n\n");
    */
  }
  
  String exampleFile = "/Users/markluffel/code/verkspace/research/src/data/example3.sgrf";
  String outputFile = "/Users/markluffel/Desktop/example.sgrf";
  public void keyPressed() {
    if(key == 's') {
      saveStrings(outputFile, graph.toRepr());
    }
    if(key == 'l') {
      String[] data = loadStrings(exampleFile);
      graph = StrokeGraph.fromRepr(data);
    }
  }
  
  // pair for converting between offsets and screen coords that are used by other stuff
  void toDraggable() {
    int k = 0;
    for(int i = 0; i < handles.length; i++) {
      for(int j = 0; j < handles[i].length; j++) {
        draggableHandles[k] = T(pts[topology[i][j]], handles[i][j]);
        k++;
      }
    }
  }
  
  void fromDraggable() {
    int k = 0;
    for(int i = 0; i < handles.length; i++) {
      for(int j = 0; j < handles[i].length; j++) {
        handles[i][j] = V(pts[topology[i][j]], draggableHandles[k]);
        k++;
      }
    }
  }
  
  int[][] topology;
  public void setup() {
    size(640, 480);
    textFont(createFont("Helvetica", 16));
    
    topology = new int[][] {
        {0,4,6,7,5,3,2,1},
        {0,1},
        {2,3},
        {4,5},
        {6,7}
    };
    
    pts = new pt[] {
      new pt(263.35022, 325.06424),
      new pt(317.81128, 348.77948),
      new pt(364.20032, 332.5838),
      new pt(400.9139, 286.89444),
      new pt(215.98752, 219.85025),
      new pt(390.09787, 172.52396),
      new pt(256.77335, 132.42972),
      new pt(322.30786, 114.893585),
    };

    handles = new vec[][] {
      {},
      {new vec(-29.0, 131.0), new vec(-22.0, 119.0), },
      {new vec(72.0, 120.0), new vec(81.0, 133.0), },
      {new vec(56.0, 30.0), new vec(-24.0, 51.0), },
      {new vec(-48.0, -79.0), new vec(2.0, -86.0), },
    };
    
    draggableHandles = new pt[8]; // from the number of values in the above array
    toDraggable();
    
    graph = StrokeGraph.build(pts, topology, handles);
  }
}
