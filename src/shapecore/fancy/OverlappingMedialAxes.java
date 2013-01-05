package shapecore.fancy;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import shapecore.Oplet;
import shapecore.pt;

import static shapecore.Geometry.*;

/**
 * 
 * This sketch was supposed to create an edge-segment-graph from a user's sketch.
 * 
 * It is entirely buggy though.
 * ***************************
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 * It might be better to restart development of this by extracting the BRep datastructure,
 * and writing testcases to drive the development of the operators.
 * 
 * Also, it might be worth using some CGAL instead
 *
 */
public class OverlappingMedialAxes extends Oplet {

  public void setup() {
    size(400,400);
    ellipseMode(RADIUS);
  }
  
  boolean dirty = true;
  public void draw() {
    if(!dirty) return;
    dirty = false;
    background(255);
    for(Segment seg : brep.segments) {
      noStroke(); fill(0,0,0,50);
      draw(new BoundingBox(seg.points));
      stroke(0); noFill();
      draw(seg.points);
    }
    noFill(); stroke(0);
    draw(drawing);
    
    noStroke(); fill(255,0,0);
    for(Corner c : brep.corners) {
      ellipse(c.location.x, c.location.y, 4, 4);
    }
  }
  
  static class BoundingBox {
    float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE; 
    BoundingBox(ArrayList<pt> points) {
      for(pt p : points) {
        if(p.x < minX) minX = p.x;
        if(p.y < minY) minY = p.y;
        if(p.x > maxX) maxX = p.x;
        if(p.y > maxY) maxY = p.y;
      }
    }
  }
  
  void draw(BoundingBox b) {
    rect(b.minX, b.minY, b.maxX-b.minX, b.maxY-b.minY);
  }

  Brep brep = new Brep();
  
  static class Brep {
    ArrayList<Segment> segments = new ArrayList<Segment>();
    ArrayList<Corner> corners = new ArrayList<Corner>();
    
    boolean isEmpty() {
      return segments.isEmpty();
    }
    
    // this might-should affect the location of a...?
    Corner merge(Corner a, Corner b) {
      corners.remove(b);
      a.segments.addAll(b.segments);
      for(Segment seg : b.segments) {
        if(seg.startCorner == b) {
          seg.startCorner = a;
        }
        if(seg.endCorner == b) {
          seg.endCorner = a;
        }
      }
      return a;
    }
    
    Corner splitNear(Segment seg, pt p) {
      int i = seg.closestIndex(p);
      pt pointOnEdge = closestPointOnEdge(p, seg.points.get(i-1), seg.points.get(i));
      return split(seg, pointOnEdge, i);
    }
    
    Corner splitAt(Segment seg, pt p) {
      // get the index + 1 __of the closest edge__
      // splitting takes the i-th vertex as the second point of the newly split segment
      // the first point of the new segment will be p
      // and the last point of the old segment will become p
      // and the next-to-last point of the old segment will be the (i-1)th point of the original segment
      int i = seg.closestIndex(p);
      return split(seg, p, i);
    }
    
    Corner split(Segment seg, Corner c, int splitIndex) {
      pt p = c.location;
      
      List<pt> chopped = seg.points.subList(splitIndex, seg.points.size());
      ArrayList<pt> pts = new ArrayList<pt>(chopped.size()+1);
      pts.add(p.clone());
      pts.addAll(chopped);
      Segment newSeg = new Segment(pts);
      
      // remove the chopped off part from the original
      chopped.clear();
      // and add the point at the corner
      seg.points.add(p.clone());

      // connect new seg to old end
      newSeg.endCorner = seg.endCorner;
      // disconnect from old end
      seg.endCorner.remove(seg);
      seg.endCorner.add(newSeg);
      
      // create new corner
      c.add(seg);
      c.add(newSeg);
      seg.endCorner = c;
      newSeg.startCorner = c;
      
      segments.add(newSeg);
      corners.add(c);
      
      return c;
    }
    
    Corner split(Segment seg, pt p, int splitIndex) {
      Corner c = new Corner(p.clone());
      return split(seg, c, splitIndex);
    }
    
    void addSegment(ArrayList<pt> pts) {
      Segment seg = new Segment(pts);
      segments.add(seg);
      Corner start = new Corner(seg.firstPoint());
      Corner end = new Corner(seg.lastPoint());
      seg.startCorner = start;
      seg.endCorner = end;
      start.add(seg);
      end.add(seg);
      
      for(int k = 0; k < segments.size(); k++) {
        Segment other = segments.get(k);
        for(int i = 1; i < other.points.size(); i++) {
          for(int j = 1; j < seg.points.size(); j++) {
            pt intersection = edgeIntersection(
                other.points.get(i-1), other.points.get(i),
                seg.points.get(j-1), seg.points.get(j));
            
            if(intersection != null) {
              // split the new segment at the intersection
              Corner c = split(seg, intersection, j);
              // then split the other, reusing the corner
              split(other, c, i);
              // this may handle self-intersection too
            }
          }
        }
      }
    }
  }
  
  static class Segment implements Comparable<Segment> {
    public Segment(ArrayList<pt> pts) {
      this.points = pts;
    }
    public pt firstPoint() {
      return points.get(0);
    }
    public pt lastPoint() {
      return points.get(points.size()-1);
    }
    public int closestIndex(pt p) {
      float closestDist = Float.MAX_VALUE;
      int closestEdge = -1;
      for(int i = 1; i < points.size(); i++) {
        pt prev = points.get(i-1);
        pt next = points.get(i);
        float d = sqdistToEdge(p, prev, next);
        if(d < closestDist) {
          closestEdge = i;
        }
      }
      return closestEdge;
    }
    public pt closestPoint(pt p) {
      float closestDist = Float.MAX_VALUE;
      pt closest = null;
      for(int i = 1; i < points.size(); i++) {
        pt prev = points.get(i-1);
        pt next = points.get(i);
        pt pTest = closestPointOnEdge(p, prev, next);
        float d = V(p,pTest).sqnorm();
        if(d < closestDist) {
          closest = pTest;
        }
      }
      return closest;
    }
    ArrayList<pt> points = new ArrayList<pt>();
    Corner startCorner, endCorner;
    
    public int compareTo(Segment that) {
      return new Integer(this.hashCode()).compareTo(this.hashCode());
    }
  }
  static class Corner {
    public Corner(pt location) {
      this.location = location;
    }
    public void remove(Segment seg) {
      segments.remove(seg);
    }
    public void add(Segment seg) {
      segments.add(seg);
    }
    pt location;
    private TreeSet<Segment> segments = new TreeSet<Segment>();
  }

  ArrayList<pt> drawing = new ArrayList<pt>();
  public void mouseDragged() {
    drawing.add(new pt(mouseX,mouseY));
    dirty = true;
  }
  
  public void mouseReleased() {
    smoothPolyline(drawing, 0.5);
    smoothPolyline(drawing, 0.5);
    brep.addSegment(drawing);
    
    drawing = new ArrayList<pt>();
    dirty = true;
  }
  
  public void keyPressed() {
    if(key == ' ') {
      brep = new Brep();
      dirty = true;
    }
    
  }
}
