package shapecore.matching;

import processing.core.PApplet;

public class DynamicTimeWarp extends PApplet {


  public void setup() {
    size(400,400);
    seqA = new int[width];
    seqB = new int[width];
    int cy = height/2;
    for(int i = 0; i < seqA.length; i++) {
      seqA[i] = seqB[i] = cy-(int)(i/2f);
    }
    dtw = new SimpleDTW(seqA, seqB);
    warp = dtw.getWarp();
  }

  int[] seqA, seqB;
  SimpleDTW dtw;
  public void draw() {
    if(drawn) return;
    drawn = true;
    
    background(255);
    
    stroke(255, 0, 0);
    if(drawingCurve == 0) {
      fill(220);
    } else {
      noFill();
    }
    drawSeq(seqA);
    
    if(drawingCurve == 1) {
      fill(220);
    } else {
      noFill();
    }
    translate(0, height/2);
    drawSeq(seqB);
    

    
    if(!mousePressed) {
      if(showPath) {
        float max = 0; // we want to scale values into an interesting range
        int aLength = dtw.warpData.length, bLength = dtw.warpData[0].length;
        for(int i = 1; i < aLength; i++) {
          for(int j = 1; j < bLength; j++) {
            float val = dtw.warpData[i][j];
            if(val > max) max = val;
          }
        }
        
        loadPixels();
        for(int i = 1; i < aLength-1; i++) {
          for(int j = 1; j < bLength-1; j++) {
            float val = dtw.warpData[i][j];
            if(val < dtw.warpData[i+1][j] && val < dtw.warpData[i][j+1]) {
              pixels[i*width+j] = color(255,0,0); // valleys
            } else if(val <= max) { // else ignore, it's one of the walls
              pixels[i*width+j] = color(val*255/max);
            }
          }
        }
        for(int i = 0; i < warp.length; i += 1) {
          int a = warp[i][0], b = warp[i][1];
          pixels[a*width+b] = color(0,255,0); // choosen path
        }
        updatePixels();
      } else {
        // draw the warp
        stroke(0,255,0,128);
        for(int i = 0; i < warp.length; i += 10) {
          int a = warp[i][0], b = warp[i][1];
          line(a, seqA[a]-height/2, b, seqB[b]);
        }
      }
    }
  }
  boolean drawn = false;
  boolean showPath = false;
  
  void drawSeq(int[] seq) {
    beginShape();
    vertex(-1,height/2);
    for(int i = 0; i < seq.length; i++) {
      vertex(i, seq[i]);
    }
    vertex(width+1, seq[seq.length-1]);
    vertex(width,height/2);
    endShape();
  }
  
  public void keyPressed() {
    if(key == ' ') {
      showPath = !showPath;
      drawn = false;
    }
  }
  
  int drawingCurve = 0;  
  int yOffset = 0;
  public void mousePressed() {
    yOffset = mouseY-getCurrentCurve()[mouseX];
  }
  
  public void mouseDragged() {
    int[] seq = getCurrentCurve();
    
    int firstX, firstY, secondX, secondY;
    if(mouseX > pmouseX) {
      firstX = pmouseX;
      firstY = pmouseY;
      secondX = mouseX;
      secondY = mouseY;
    } else {
      firstX = mouseX;
      firstY = mouseY;
      secondX = pmouseX;
      secondY = pmouseY;
    }
    firstY -= yOffset;
    secondY -= yOffset;
    // safety
    firstY = constrain(firstY, 0, height/2);
    secondY = constrain(secondY, 0, height/2);
    secondX = min(width, secondX);
    // otherwise, can't draw all the way over to the left :(
    if(firstX < 10) {
      firstX = 0;
    }
    
    float xStep = 1f/(secondX - firstX);
    float interp = 0;
    for(int i = firstX; i < secondX; i++) {
      seq[i] = (int) lerp(firstY, secondY, interp);
      interp += xStep;
    }
    drawn = false;
  }
  
  private int[] getCurrentCurve() {
    if(drawingCurve == 0) {
      return seqA;
    } else if(drawingCurve == 1) {
      return seqB;
    } else {
      return null; // fail
    }
  }
  
  int[][] warp;
  public void mouseReleased() {
    drawn = false;
    drawingCurve = (drawingCurve+1)%2; // draw one of the two
    // compute the new warp
    warp = dtw.getWarp();
  }

  
  class SimpleDTW extends DTW {
    
    int[] seqA, seqB;
    
    float yScaling = 0.00001f;
    // for the slope relative to points +/- 1, 5, and 15 pixels away
    float dy1Scaling = 0.1f;
    float dy5Scaling = 0.05f;
    float dy15Scaling = 0.01f;
    float dy30Scaling = 0.005f;
    
    public float skipCost(int i, int j) {
      return 200/max(seqA.length, seqB.length);
    }
    
    public SimpleDTW(int[] seqA, int[] seqB) {
      
      this.seqA = seqA;
      this.seqB = seqB;
      //yScaling = 100/Math.max(seqA.length, seqB.length);
    }

    public float costFunction(int i, int j) {
      
      int
      pi1 = constrain(i+seqA.length-1, 0, seqA.length-1),
      pj1 = constrain(j+seqB.length-1, 0, seqB.length-1),
      ni1 = constrain(i+1, 0, seqA.length-1),
      nj1 = constrain(j+1, 0, seqB.length-1),

      pi5 = constrain(i+seqA.length-5, 0, seqA.length-1),
      pj5 = constrain(j+seqB.length-5, 0, seqB.length-1),
      ni5 = constrain(i+5, 0, seqA.length-1),
      nj5 = constrain(j+5, 0, seqB.length-1),

      pi15 = constrain(i+seqA.length-15, 0, seqA.length-1),
      pj15 = constrain(j+seqB.length-15, 0, seqB.length-1),
      ni15 = constrain(i+15, 0, seqA.length-1),
      nj15 = constrain(j+15, 0, seqB.length-1),

      pi30 = constrain(i+seqA.length-30, 0, seqA.length-1),
      pj30 = constrain(j+seqB.length-30, 0, seqB.length-1),
      ni30 = constrain(i+30, 0, seqA.length-1),
      nj30 = constrain(j+30, 0, seqB.length-1)
      ;

      
      return sq(seqA[i]-seqB[j])*yScaling // height
      + sq((seqA[pi1]-seqA[i]) - (seqB[pj1]-seqB[j]))*dy1Scaling // slope relative to values at x-1 
      + sq((seqA[ni1]-seqA[i]) - (seqB[nj1]-seqB[j]))*dy1Scaling // slope relative to values at x+1
      
      + sq((seqA[pi5]-seqA[i]) - (seqB[pj5]-seqB[j]))*dy5Scaling // slope relative to values at x-5 
      + sq((seqA[ni5]-seqA[i]) - (seqB[nj5]-seqB[j]))*dy5Scaling // slope relative to values at x+5
      
      + sq((seqA[pi15]-seqA[i]) - (seqB[pj15]-seqB[j]))*dy15Scaling // slope relative to values at x-15 
      + sq((seqA[ni15]-seqA[i]) - (seqB[nj15]-seqB[j]))*dy15Scaling // slope relative to values at x+15

      + sq((seqA[pi30]-seqA[i]) - (seqB[pj30]-seqB[j]))*dy30Scaling // slope relative to values at x-30 
      + sq((seqA[ni30]-seqA[i]) - (seqB[nj30]-seqB[j]))*dy30Scaling // slope relative to values at x+30

      ;

    }
    
    public float[][] exec() {
      return exec(seqA.length, seqB.length);
    }
    
  }
}
