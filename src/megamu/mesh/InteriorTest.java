package megamu.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;



import processing.core.PApplet;
import shapecore.BoundingBox;

/**
 * 
 * I never got the clever part of this running correctly.
 * It was going to use fractional cascading and monotone chains.
 * 
 * The naive implementation is robust
 *
 */
public class InteriorTest {

  float[][] points;
  BoundingBox bb;
  //float minX, minY, maxX, maxY;
  boolean flip = false;
  MonoRect[] rects;
  int[] rectRangeXs, rectRangeYs;
  BitSet[] rectOverlapsX, rectOverlapsY;
  
  private class MonoRect {
    // xs are sorted ascending, ys are paired with them
    float[] xs, ys;
    float minX, minY, maxX, maxY;
    Corner corner;
    
    MonoRect(ArrayList<float[]> pts, Corner dir) {
      // sort based on x
      Collections.sort(pts, new Comparator<float[]>() {
        public int compare(float[] a, float[] b) {
          return Float.compare(a[0], b[0]);
        }
      });
      int len = pts.size();
      xs = new float[len];
      ys = new float[len];
      for(int i = 0; i < len; i++) {
        float[] p = pts.get(i);
        xs[i] = p[0];
        ys[i] = p[1];
      }
      minX = xs[0];
      maxX = xs[len-1];
      // because we break the polygon into monotonic parts,
      // we know that the min and max are at the ends
      float y0 = ys[0], y1 = ys[len-1];
      if(y0 < y1) { 
        minY = y0; maxY = y1;
      } else {
        minY = y1; maxY = y0;
      }
        
      this.corner = dir;
    }

    public boolean rectContains(float x, float y) {
      return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public boolean contains(float x, float y) {
      int index = Arrays.binarySearch(xs, x); // -insertion_point - 1
      float testY;
      if(index < -1) {
        float lx = xs[-index-2], rx = xs[-index-1], ly = ys[-index-2], ry = ys[-index-1];
        float t = (x-lx)/(rx-lx);
        testY = ly*(1-t) + ry*t;
      } else if(index >= 0 && index < xs.length) {
        testY = ys[index];
      } else {
        //return false; // outside the x bounds
        return Math.random() > 0.5;
      }
      if(corner == Corner.DOWN_LEFT || corner == Corner.DOWN_RIGHT) {
        return y < testY;
      } else {
        return y > testY;
      }
    }
  }
  
  private static enum Corner {
    // this direction is the part of the monorect that is inside the shape
    UP_LEFT, UP_RIGHT, DOWN_RIGHT, DOWN_LEFT, AXIS; // AXIS = none-of-the-above
  }
  
  // outside coordinates are defined to be outside, use this to handle the 8-glyph case
  public InteriorTest(float[][] points, float outsideX, float outsideY) {
    this.points = points;
    bb = new BoundingBox(points);
    // buildMonotoneChains(outsideX, outsideY);
  }
  
  void buildMonotoneChains(float outsideX, float outsideY) {
    // find the leftmost point
    int minXi = 0;
    for(int i = 0; i < points.length; i++) {
      float x = points[i][0], y = points[i][1];
      if(bb.minX == x) minXi = i;
    }
    if(contains1(outsideX,outsideY)) flip = true;
    
    // TODO: for simplicity of the code, break the array apart at the the minXi
    //
    ArrayList<MonoRect> _rects = new ArrayList<MonoRect>();
    ArrayList<float[]> pts = new ArrayList<float[]>();
    Corner curDir = innerCorner(points[(minXi+points.length-1)%points.length],points[minXi]);
    Corner dir;
    pts.add(points[minXi]);
    minXi++;
    for(int i = minXi; i < points.length; i++) {
      dir = innerCorner(points[i-1],points[i]);
      pts.add(points[i]);
      if(dir != curDir && dir != Corner.AXIS) { // need to split it off
        MonoRect mr = new MonoRect(pts, curDir);
        _rects.add(mr);
        pts.clear();
        pts.add(points[i]);
        curDir = dir;
      }
    }
    dir = innerCorner(points[points.length-1],points[0]);
    pts.add(points[0]);
    if(dir != curDir && dir != Corner.AXIS) { // need to split it off
      MonoRect mr = new MonoRect(pts, curDir);
      _rects.add(mr);
      //pts.clear();
      pts.add(points[0]);
      curDir = dir;
    }
    for(int i = 1; i < minXi; i++) {
      dir = innerCorner(points[i-1],points[i]);
      pts.add(points[i]);
      if(dir != curDir && dir != Corner.AXIS) { // need to split it off
        MonoRect mr = new MonoRect(pts, curDir);
        _rects.add(mr);
        pts.clear();
        pts.add(points[i]);
        curDir = dir;
      }
    }
    pts.add(points[minXi-1]);
    MonoRect mr = new MonoRect(pts, curDir);
    _rects.add(mr);
    rects = _rects.toArray(new MonoRect[_rects.size()]);
  }
  
  Corner innerCorner(float[] a, float[] b) {
    float dx = b[0]-a[0], dy = b[1]-a[1];
    if(dx > 0) {
      if(dy > 0) {
        return Corner.DOWN_LEFT;
      } else if (dy < 0) {
        return Corner.DOWN_RIGHT;
      } else {
        return Corner.AXIS;
      }
    } else if(dx < 0) {
      if(dy > 0) {
        return Corner.UP_LEFT;
      } else if(dy < 0) {
        return Corner.UP_RIGHT;
      } else {
        return Corner.AXIS;
      }
    } else {
      return Corner.AXIS;
    }
  }
  
  public InteriorTest(float[][] points) {
    this(points, -10000, -10000);
  }
  
  public void visualize(PApplet p) {
    
    for(int i = 0; i < rects.length; i++) {
      MonoRect r = rects[i];
      
      int last = r.xs.length-1;
      p.ellipseMode(p.RADIUS); p.noStroke(); 
      p.fill(0,0,0,128); p.ellipse(r.xs[0], r.ys[0], 6,6);
      p.fill(255,255,0,128); p.ellipse(r.xs[last], r.ys[last], 7,7);
      
      /*
      p.noFill(); p.stroke(255,200,0);
      p.rect(r.minX,r.minY,r.maxX-r.minX,r.maxY-r.minY);
      p.noStroke();
      p.fill(0);
      int n = 5;
      switch(r.corner) { 
      case UP_LEFT: p.rect(r.minX,r.minY,n,n); break;
      case UP_RIGHT: p.rect(r.maxX-n,r.minY,n,n); break;
      case DOWN_LEFT: p.rect(r.minX,r.maxY,n,n); break;
      case DOWN_RIGHT: p.rect(r.maxX-n,r.minY,n,n); break;
      }
      */
    }
  }
  
  public boolean contains(float x, float y) {
    if(!bb.contains(x, y)) return false;
    return contains4(x,y);
    //return cheapContains(x,y);
  }
  
  // this isn't finished, but is the right idea
  // needs to properly detect winding
  // and needs to handle regions of the interior which are not in any monotone bounding box 
  private boolean cheapContains(float x, float y) {
    int possibilities = 0;
    int actualities = 0;
    for(MonoRect rect : rects) {
      if(rect.rectContains(x,y)) {
        possibilities++;
        if(rect.contains(x,y)) {
          actualities++;
        }
      }
    }
    return possibilities > 0 && actualities == possibilities;
  }
  
  // this is the only code in this file actually being run now ;)
  private boolean contains4(float x, float y) {
    boolean c = false;
    for (int i = 0, j = points.length-1; i < points.length; j = i++) {
      if( ((points[i][1] > y) != (points[j][1] > y))
      && (x < (points[j][0]-points[i][0]) * (y-points[i][1]) / (points[j][1]-points[i][1]) + points[i][0]) )
         c = !c;
    }
    return c;
  }
  
  
  // assuming clockwise, should enforce this
  private boolean contains1(float x, float y) {
    int count = 0;
    boolean previous = true;
    for(int i = 1; i < points.length; i++) {
      boolean right = rightOf(x,y, points[i-1],points[i]);
      if(right != previous && frontOf(x,y,points[i-1],points[i])) {
        count++;
      }
      previous = right;
    }
    int i = points.length;
    boolean right = rightOf(x,y, points[i-1],points[0]);
    if(right != previous && frontOf(x,y,points[i-1],points[0])) {
      count++;
    }
    if(!rightOf(x,y,points[i-1],points[0]) && frontOf(x,y, points[0],points[1])) {
      count++;
    }
    return (count%2 != 1) ^ flip;
  }
  
  float sqdist(float[] a, float[] b) {
    float dx = a[0]-b[0], dy = a[1]-b[1];
    return dx*dx+dy*dy;
  }
  
  private boolean contains3(float x, float y) {
    int count = 0;
    boolean previous = true;
    for(int j = 0, i = 1; i < points.length; i++) {
      if(sqdist(points[j],points[i]) < 10) continue;
      boolean right = rightOf(x,y, points[j],points[i]);
      if(right != previous && frontOf(x,y,points[i-1],points[i])) {
        count++;
      }
      previous = right;
      j = i;
    }
    int i = points.length;
    boolean right = rightOf(x,y, points[i-1],points[0]);
    if(right != previous && frontOf(x,y,points[i-1],points[0])) {
      count++;
    }
    if(!rightOf(x,y,points[i-1],points[0]) && frontOf(x,y, points[0],points[1])) {
      count++;
    }
    return (count%2 != 1) ^ flip;
  }
  
    
  boolean rightOf(float px, float py, float[] edgeStart, float[] edgeEnd) {
    
    float edgeNormX = edgeStart[1]-edgeEnd[1];
    float edgeNormY = edgeEnd[0]-edgeStart[0];
    return dot(px-edgeStart[0], py-edgeStart[1], edgeNormX, edgeNormY) > 0;
  }
  
  boolean frontOf(float px, float py, float[] edgeStart, float[] edgeEnd) {
    
    float edgeTanX = edgeEnd[0]-edgeStart[0];
    float edgeTanY = edgeEnd[1]-edgeStart[1];
    return dot(px-edgeStart[0], py-edgeStart[1], edgeTanX, edgeTanY) > 0;
  }
  
  private boolean contains2(float x, float y) {
    int count = 0;
    float[] other = new float[]{0,0};
    for(int i = 1; i < points.length; i++) {
      if(inTri(x,y, other,points[i-1],points[i])) {
        count++;
      }
    }
    if(inTri(x,y, other,points[points.length-1],points[0])) {
      count++;
    }
    return count%2 == 1;
  }
  
  private boolean inTri(float x, float y, float[] a, float[] b, float[] c) {
    
    //v0 = C - A,
    //v1 = B - A,
    //v2 = P - A;
    float []
    v0 = new float[]{
      c[0]-a[0],
      c[1]-a[1]
    },
    v1 = new float[]{
      b[0]-a[0],
      b[1]-a[1]
    },
    v2 = new float[] {
      x-a[0],
      y-a[1]
    };
    
    // Compute dot products
    float
    dot00 = dot(v0, v0),
    dot01 = dot(v0, v1),
    dot02 = dot(v0, v2),
    dot11 = dot(v1, v1),
    dot12 = dot(v1, v2),

    // Compute barycentric coordinates
    denom = (dot00 * dot11 - dot01 * dot01),
    invDenom = 1 / denom,
    u = (dot11 * dot02 - dot01 * dot12) / denom,
    v = (dot00 * dot12 - dot01 * dot02) / denom;

    // Check if point is in triangle
    return (u >= 0) && (v >= 0) && (u + v < 1);
  }
  
  private float dot(float[] a, float[] b) {
    return a[0]*b[0] + a[1]*b[1];
  }
  
  private float dot(float ax, float ay, float bx, float by) {
    return ax*bx + ay*by;
  }

  public BoundingBox getBoundingBox() {
    return bb;
  }
}
