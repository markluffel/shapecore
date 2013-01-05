package shapecore.fancy;

import java.util.ArrayList;

import shapecore.Oplet;
import shapecore.pt;
import shapecore.vec;

import static shapecore.Geometry.*;

public class GreenCoordinates extends Oplet {

  //
  // TODO: selection based on edges
  // 
  //
  public void draw() {
    if(!dirty) return;
    dirty = false;
    background(255);
    drawButtons();
    
    stroke(255,200,0); noFill(); strokeWeight(3);
    if(mode == Mode.MAKE_CAGE) { stroke(255,200,0); } else { stroke(255,200,0,0); }
    draw(cage, true);
    stroke(255,200,0,100);
    draw(editedCage, true);

    if(mode != Mode.EDIT_CAGE) {
      stroke(0); strokeWeight(1);
      for(ArrayList<pt> pts : doodles) {
        draw(pts);
      }
    }
    stroke(0,0,0,100); strokeWeight(2);
    for(ArrayList<pt> pts : deformed) {
      draw(pts);
    }
  }
  
  int maxCageEditDistance = 20;
  
  ArrayList<pt> cage;
  ArrayList<pt> editedCage;
  ArrayList<ArrayList<pt>> doodles;
  ArrayList<ArrayList<pt>> deformed;
  ArrayList<ArrayList<GreenCoord>> coordinates;
  private static enum Mode {MAKE_CAGE, EDIT_CAGE, DOODLE};
  Mode mode = Mode.DOODLE;
  boolean dirty = true;
  
  public void setup() {
    size(640, 480);
    cage = new ArrayList<pt>();
    editedCage = new ArrayList<pt>();
    doodles = new ArrayList<ArrayList<pt>>();
    deformed = new ArrayList<ArrayList<pt>>();
    coordinates = new ArrayList<ArrayList<GreenCoord>>();
    
    textFont(createFont("Helvetica", textHeight), textHeight);
  }
  
  pt dragging = null;
  
  String[] buttons = new String[] {"New Cage", "Revert Warp", "New Doodle", "Save", "Load"};
  void buttonPressed(int i) {
    
    // if we're making a cage and press anything, lose the mode 
    if(mode == Mode.MAKE_CAGE) {
      finishCage();
      mode = Mode.EDIT_CAGE;
      buttons[0] = "New Cage";
      return;
    }
    
    switch(i) {
    case 0: // make/finish cage
      cage.clear();
      editedCage.clear();
      deformed.clear();
      coordinates.clear();
      mode = Mode.MAKE_CAGE;
      buttons[0] = "Finish Cage";
      break;
      
    case 1: // reset deformation
      finishCage();
      mode = Mode.EDIT_CAGE;
      dirty = true;
      break;
      
    case 2: // new doodle
      cage.clear();
      editedCage.clear();
      doodles.clear();
      deformed.clear();
      coordinates.clear();
      mode = Mode.DOODLE;
      break;
      
    case 3: // save
      String outputName = selectOutput("Save drawing and cage as...");
      if(outputName != null) {
        saveDrawing(outputName);
      }
      break;
    
    case 4: // load
      String inputName = selectInput("Load drawing and cage as...");
      if(inputName != null) {
        doodles.clear();
        deformed.clear();
        cage.clear();
        editedCage.clear();
        loadDrawing(inputName);
        refreshCoordinates();
        deform();
        mode = Mode.EDIT_CAGE;
        dirty = true;
      }
      break;
    }
  }
  
  void saveDrawing(String filename) {
    String[] data = new String[doodles.size()+2];
    for(int i = 0; i < doodles.size(); i++) {
      ArrayList<pt> pts = doodles.get(i);
      data[i] = pointListToString(pts);
    }
    data[data.length-2] = pointListToString(cage);
    data[data.length-1] = pointListToString(editedCage);
    saveStrings(filename, data);
  }
  
  String pointListToString(ArrayList<pt> pts) {
    StringBuilder b = new StringBuilder();
    for(pt p : pts) {
      b.append(p.x);
      b.append(' ');
      b.append(p.y);
      b.append(' ');
    }
    return b.toString();
  }
  
  ArrayList<pt> stringToPointList(String data) {
    String[] values = data.split(" ");
    ArrayList<pt> result = new ArrayList<pt>();
    for(int i = 1; i < values.length; i += 2) {
      result.add(new pt(parseFloat(values[i-1]), parseFloat(values[i])));
    }
    return result;
  }
  
  void loadDrawing(String filename) {
    String[] data = loadStrings(filename);
    for(int i = 0; i < data.length-2; i++) {
      doodles.add(stringToPointList(data[i]));
    }
    cage = stringToPointList(data[data.length-2]);
    editedCage = stringToPointList(data[data.length-1]);
  }
  
  public void mouseDragged() {
    dirty = true;
    
    switch(mode) {
    case MAKE_CAGE:
      // whatev
      break;
      
    case EDIT_CAGE:
      if(dragging != null) {
        dragging.add(mouseX-pmouseX, mouseY-pmouseY);
        deform();
      }
      break;
      
    case DOODLE:
      if(doodles.size() > 0) {
        doodles.get(doodles.size()-1).add(new pt(mouseX, mouseY));
      }
      break;
    }
    
  }
  
  public void mousePressed() {
    dirty = true;
    if(checkButtonPressed()) return;
    
    pt mouse = new pt(mouseX,mouseY);
    switch(mode) {
    case MAKE_CAGE:
      cage.add(new pt(mouseX,mouseY));
      break;

    case EDIT_CAGE:
      float minDist = maxCageEditDistance;
      for(pt p : editedCage) {
        float d = p.disTo(mouse);
        if(d < minDist) {
          dragging = p;
          minDist = d;
        }
      }
      break;
      
    case DOODLE:
      ArrayList<pt> pts = new ArrayList<pt>();
      pts.add(new pt(mouseX, mouseY));
      doodles.add(pts);
      break;
    }
  }
  
  public void mouseReleased() {
    dragging = null;
  }
  
  void finishCage() {
    if(cage.size() < 3) return;
    
    editedCage.clear();
    for(pt p : cage) {
      editedCage.add(p.clone());
    }
    refreshCoordinates();
    deform();
  }
  
  void refreshCoordinates() {
    coordinates.clear();
    for(ArrayList<pt> stroke : doodles) {
      ArrayList<GreenCoord> strokeCoords = new ArrayList<GreenCoord>();
      coordinates.add(strokeCoords);
      for(pt p : stroke) {
        strokeCoords.add(new GreenCoord(p, cage));
      }
    }
  }
  
  void deform() {
    deformed.clear();
    for(ArrayList<GreenCoord> strokeCoords : coordinates) {
      ArrayList<pt> stroke = new ArrayList<pt>();
      deformed.add(stroke);
      for(GreenCoord coord : strokeCoords) {
        stroke.add(coord.deform(editedCage));
      }
    }
  }
  
  public static class GreenCoord {
    public float[] phi,psi;
    public float[] edgeLengths;
    
    GreenCoord(pt eta, ArrayList<pt> cage) {
      int size = cage.size();
      phi = new float[size];
      psi = new float[size];
      edgeLengths = new float[size];
      
      int pj = size-1;
      for(int j = 0; j < size; pj = j, j++) {
        pt vj1 = cage.get(pj);
        pt vj2 = cage.get(j);
        vec a = V(vj1, vj2);
        vec b = V(eta, vj1);
        double Q = a.dot(a);
        double S = b.dot(b);
        double R = a.dot(b)*2;
        
        // in the paper they normalize this and then multiply by its length ???
        double BA = b.dot(a.get().turnLeft());
        double SRT = Math.sqrt(4*S*Q - R*R);
        double L0 = Math.log(S);
        double L1 = Math.log(S+Q+R);
        double A0 = Math.atan2(R, SRT) / SRT;
        double A1 = Math.atan2(2*Q+R, SRT) / SRT;
        
        double A10 = A1-A0;
        double L10 = L1-L0;
        
        double edgeLength = Math.sqrt(Q); 
        psi[j] = (float)((-edgeLength / (4*PI)) * ( (4*S - R*R/Q) * A10 + R/(2*Q) * L10 + L1 - 2));
        
        phi[pj] += (BA/TWO_PI)*(L10/(2*Q) - A10*(2 + R/Q));
        phi[j]  -= (BA/TWO_PI)*(L10/(2*Q) - A10*(R/Q));
        
        edgeLengths[j] = (float)edgeLength;
      }
    }
    
    public pt deform(ArrayList<pt> newCage) {
      if(newCage.size() != phi.length || newCage.size() != psi.length) {
        throw new IllegalArgumentException(); // need to have identical size cage
      }
      pt result = new pt();
      //for(int i = 0; i < phi.length; i++) {
      //  result.addScaledPt(phi[i], newCage.get(i));
      //}
      int pj = psi.length-1;
      for(int j = 0; j < psi.length; pj = j, j++) {
        vec v = V(newCage.get(pj), newCage.get(j));
        v.turnRight();
        result.add(psi[j]/(edgeLengths[j]), v);
        result.addScaledPt(phi[j], newCage.get(j));
      }
      return result;
    }
  }
  
  int textHeight = 20;
  int padding = 6;
  void drawButtons() {
    fill(80); noStroke();
    rect(0,0,width,textHeight+padding*3);
    
    int x = padding*2;
    for(String button : buttons) {
      int nextX = (int)textWidth(button) + padding*2;
      fill(120);
      rect(x-padding, padding, nextX, textHeight+padding);
      fill(0);
      text(button, x, textHeight+padding);
      x += nextX+padding*2;
    }
  }
  
  boolean checkButtonPressed() {
    if(mouseY > padding && mouseY < textHeight+padding*2) {      
      int x = padding;
      for(int i = 0; i < buttons.length; i++) {
        String button = buttons[i];
        x += (int)textWidth(button) + padding*2;
        
        if(mouseX < x) {
          buttonPressed(i);
          break;
        }
        x += padding*2;
        if(mouseX < x) {
          return false; // padding
        }
      }
      return true;
    }
    return false;
  }
}
