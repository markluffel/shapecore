package shapecore;

import static processing.core.PApplet.*;
import static shapecore.Geometry.*;

import java.util.Arrays;
import java.util.Comparator;


public class GrahamScan {
  int M, j;
  boolean done;
  
  class Item {
    float x,y, angle;
    Item(pt p) { x=p.x; y=p.y; }
  }
  Item[] items;
  int N;
  
  public GrahamScan(pt[] _pts) {
    // TODO: test if we can run successfully with 1, 2, 3 points
    if(_pts.length < 4) throw new IllegalArgumentException();
    done = false;
    N = _pts.length;
    items = new Item[_pts.length+1];
    items[0] = new Item(P(Float.MAX_VALUE,Float.MAX_VALUE));
    items[0].angle = -2; // must be less that -1
    // find index of topmost point
    float minY = Float.MAX_VALUE;
    int minYi = -1;
    for(int i = 0; i < _pts.length; i++) {
      pt p = _pts[i];
      if(p.y < minY) {
        minY = p.y;
        minYi = i+1; // an index into items, not points
      }
      items[i+1] = new Item(p);
    }
    
    //sort, place angle relative to minY point in "z"
    float cx = items[minYi].x, cy = items[minYi].y;
    for(int i = 1; i < items.length; i++) {
      Item p = items[i];
      items[i].angle = atan2(p.y-cy, p.x-cx)+TWO_PI;
    }
    items[minYi].angle = -1; // force to the second location in the sort (after the dummy)
    
    Arrays.sort(items, new Comparator<Item>() {
      public int compare(Item p1, Item p2) {
        return Float.compare(p1.angle, p2.angle);
      }
    });

    // top point is in pts[1]
    // dummy value is in pts[0]    
    items[0] = items[N];
    M = 2;
    j = 3;
    // we already know that pts[0], pts[1], and pts[2] and consecutive points on the hull
    // other than that we don't know yet
  }
  
  void next() {
    if(done) return;
    
    if(!cw(items[M-1], items[M], items[j])) {
      if(M == 2) {
        swap(M,j);
        j++;
      } else {
        M--;
      }
    } else {
      M++;
      swap(M,j);
      j++;
    }
    
    if(j > N) done = true;
  }
  
  void run() {
    while(!done) next();
  }
  
  public pt[] getPoints() {
    run();
    pt[] pts = new pt[M];
    for(int i = 0; i < M; i++) {
      pts[i] = new pt(items[i].x, items[i].y);
    }
    return pts;
  }
  
  void swap(int a, int b) {
    Item tmp = items[a];
    items[a] = items[b];
    items[b] = tmp;
  }
  
  boolean cw(Item p1, Item p2, Item p3) {
    return (p2.x-p1.x)*(p3.y-p1.y) - (p2.y-p1.y)*(p3.x-p1.x) > 0;
  }
}