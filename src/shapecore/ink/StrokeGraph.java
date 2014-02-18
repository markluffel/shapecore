package shapecore.ink;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import shapecore.Oplet;
import shapecore.SamplablePolyline;
import shapecore.pt;
import shapecore.vec;



public class StrokeGraph {

  public ArrayList<HalfEdge> halfedges = new ArrayList<HalfEdge>();
  public int numIntersections = 0;
  
  /**
   * A point along a stroke, with an associated radius.
   */
  public static class Disk extends pt {
    //float x,y,r;
    float r;
    Disk(float x, float y, float r) {
      this.x = x;
      this.y = y;
      this.r = r;
    }
    
    public String toString() {
      return "("+x+", "+y+", "+r+")";
    }
  }
  
  public static class Color {
    float r,g,b;
    Color(float r, float g, float b) {
      this.r = r;
      this.g = g;
      this.b = b;
    }
    static Color NoColor = new Color(-1,-1,-1);
  }
  
  public static class HalfEdge {
    
    public HalfEdge(){}
    
    public HalfEdge(List<pt> centers) {
      this.disks = new ArrayList<Disk>(centers.size());
      for(pt p : centers) {
        this.disks.add(new Disk(p.x, p.y, 1));
      }
    }

    /**
     * Geometry for this part of the graph.
     * 
     * This list will be shared with the opposite halfedge,
     * however the opposite halfedge's disks will be in the opposite order as these:
     *  halfedges[e.opposite].disks == e.disks
     *  halfedges[e.opposite].reversed = !e.reversed
     *  
     */
    public ArrayList<Disk> disks;

    /**
     * Flag to specify that the disks occur in reverse order along this halfedge. 
     */
    public boolean reversed = false;

    /**
     * An index into the halfedges list,
     * points to the halfedge on the other "side" of the stroke.
     */
    public int opposite = -1;
    
    /**
     * An index into the halfedges list,
     * points to the halfedge that contains the "continuation" of this one.
     * May be -1, indicating that this edge either:
     *  1) dead-ends at a T-junction
     *  2) dangles, ending not at a junction 
     */
    public int forward = -1;
    
    /**
     * An index into the halfedges list,
     * points to the next halfedge clockwise around the region this one bounds.
     * If this halfedge
     * May be -1, when this halfedge is on the occluding side of a T-junction
     */
    public int clockwise = -1;
    
    /**
     * The assigned color of this halfedge,
     * will be null if unassigned,
     * should match with opposite halfedge.
     */
    Color color = Color.NoColor;
    
    public vec headDirection() {
      if(reversed) {
        return V(disks.get(1), disks.get(0)).normalize();
      } else {
        return V(disks.get(disks.size()-2), disks.get(disks.size()-1)).normalize();
      }
    }
    
    public vec tailDirection() {
      if(reversed) {
        return V(disks.get(disks.size()-1), disks.get(disks.size()-2)).normalize();
      } else {
        return V(disks.get(0), disks.get(1)).normalize();
      }
    }
    

    public pt head() {
      if(reversed) {
        return disks.get(0);
      } else {
        return disks.get(disks.size()-1);
      }
    }

    // in terms of arc lengths
    public pt midpoint() {
      float leftDist = 0;
      float rightDist = 0;
      int left = 0;
      int right = disks.size()-2;
      
      do {
        float leftSeg = arclength(left);
        float rightSeg = arclength(right); 
        if(leftDist+leftSeg < rightDist+rightSeg) {
          leftDist += leftSeg;
          left++;
        } else {
          rightDist += rightSeg;
          right--;
        }
      } while(left < right);
      
      float splitSeg = 2*arclength(left);
      float diff = leftDist-rightDist;
      float t = 0.5f - diff/splitSeg;
      
      return lerp(disks.get(left), disks.get(left+1), t);
    }
    
    private float arclength(int i) {
      return V(disks.get(i),disks.get(i+1)).norm();
    }

    public vec headToTail() {
      return V(disks.get(0), disks.get(disks.size()-1));
    }

    public boolean hasClockwise() {
      return clockwise >= 0;
    }
    
    public boolean hasForward() {
      return forward >= 0;
    }

    public void arrow(Oplet p) {
      SamplablePolyline poly = new SamplablePolyline(disks);
      
      float endPadding = 12;
      float sidewaysOffset = 8;
      
      pt nearlast, last;
      if(reversed) {
        nearlast = poly.sampleByDistance(endPadding+1);
        last = poly.sampleByDistance(endPadding);
      } else {
        nearlast = poly.sampleByDistance(-endPadding-1);
        last = poly.sampleByDistance(-endPadding);
      }
      
      // draw the edge directions
      vec lastTang = V(last, nearlast);
      lastTang.normalize();
      nearlast = T(last, 10, lastTang);
      vec offset = R(nearlast,last);
      offset.normalize();
      pt wing = T(nearlast, 7, offset);
      
      // let's mess with the transform matrix,
      // just to make this more of a mess
      p.pushMatrix();
      p.translate(offset.x*sidewaysOffset, offset.y*sidewaysOffset);
      p.triangle(last.x, last.y, wing.x, wing.y, nearlast.x, nearlast.y);
      p.popMatrix();
            
      p.pushStyle();
      p.noFill();
      p.strokeWeight(2);
      
      float length = poly.getLength()-endPadding;
      float incr = 5f;
      if(reversed) sidewaysOffset = -sidewaysOffset;
      
      p.beginShape();
      for(float d = endPadding; d < length; d += incr) {
        pt a = poly.sampleByDistance(d),
           b = poly.sampleByDistance(d+incr);
        vec offs = R(a,b);
        offs.normalize();
        p.vertex(T(a, sidewaysOffset, offs));
      }
      p.endShape();
      p.popStyle();      
    }
  }
  
  /**
   * Create a flat file representation of the stroke graph,
   * each string in the array will be separated by a newline in the output.
   */
  public String[] toRepr() {
    String[] repr = new String[halfedges.size()*2];
    
    for(int i = 0; i < halfedges.size(); i++) {
      HalfEdge he = halfedges.get(i);
      if(!he.reversed) {
        StringBuilder geom = new StringBuilder();
        geom.append(he.color.r);
        geom.append(' ');
        geom.append(he.color.g);
        geom.append(' ');
        geom.append(he.color.b);
        geom.append(' ');
        
        for(Disk d : he.disks) {
          geom.append(d.x);
          geom.append(' ');
          geom.append(d.y);
          geom.append(' ');
          geom.append(d.r);
          geom.append(' ');
        }
        repr[i*2] = geom.toString();
      } else {
        repr[i*2] = ""; // don't record disks twice, only for the "unreversed" halfedge
      }
      repr[i*2+1] = he.opposite+" "+he.forward+" "+he.clockwise;
    }
    return repr;
  }
  
  /**
   * Read the file representation in and build a graph.
   */
  public static StrokeGraph fromRepr(String[] data) {
    StrokeGraph graph = new StrokeGraph();
    for(int i = 0; i < data.length; i += 2) {
      String[] geom = data[i].split(" ");
      String[] topo = data[i+1].split(" ");
      HalfEdge e = new HalfEdge();
      graph.halfedges.add(e);
      if(geom.length >= 3) {
        e.color = new Color(
          Float.parseFloat(geom[0]),
          Float.parseFloat(geom[1]),
          Float.parseFloat(geom[2])
        );
        e.disks = new ArrayList<Disk>();        
        for(int j = 3; j < geom.length; j += 3) {
          e.disks.add(new Disk(Float.parseFloat(geom[j]), Float.parseFloat(geom[j+1]), Float.parseFloat(geom[j+2])));
        }
      } else {
        e.reversed = true;
      }
      e.opposite = Integer.parseInt(topo[0]);
      e.forward = Integer.parseInt(topo[1]);
      e.clockwise = Integer.parseInt(topo[2]);
    }
    // since we only record the disks for the forward edge, this reconnects them
    for(HalfEdge e : graph.halfedges) {
      if(e.reversed) {
        e.disks = graph.halfedges.get(e.opposite).disks;
      }
    }
    return graph;
  }
  
  /**
   * Attempt to complete a matching of two graphs, given an initial matching of two halfedges.
   * Uses a simultaneous breadth-first traversal of the two graphs,
   * if 
   * 
   * @param that a graph to compare against
   * @param thisStart the index of a halfedge in this graph at which to start the match   
   * @param thatStart the index of a halfedge in the other graph at which to start the match
   * @return a pair of arrays, the first mapping from halfedges in this to halfedges in that,
   *         the second an inverse of the first,
   *         if no match is possible, returns null
   */
  public int[][] perfectMatch(StrokeGraph that, int thisStart, int thatStart) {
    
    // sizes need to match
    if(this.halfedges.size() != that.halfedges.size()) return null;
    
    int[] thisVisited = new int[this.halfedges.size()];
    int[] thatVisited = new int[this.halfedges.size()];
    Arrays.fill(thisVisited, -1);
    Arrays.fill(thatVisited, -1);
    
    Queue<int[]> toVisit = new LinkedList<int[]>();
    toVisit.add(new int[]{thisStart,thatStart});
    
    while(!toVisit.isEmpty()) {
      int[] pair = toVisit.remove();
      int thisHe = pair[0];
      int thatHe = pair[1];
      
      // several references may be undefined
      // ok if both this and that are at the same time
      if(thisHe == -1 && thatHe == -1) continue;
      // but if only one is undefined, fail
      if((thisHe == -1) != (thatHe == -1)) return null; 
      
      // check if we've already traversed
      int thisMatch = thisVisited[thisHe];
      int thatMatch = thatVisited[thatHe];
      if(thisMatch == -1 && thatMatch == -1) {
        // remember the match, if new
        thisVisited[thisHe] = thatHe;
        thatVisited[thatHe] = thisHe;
        
        toVisit.add(new int[]{this.opposite(thisHe),  that.opposite(thatHe)});
        toVisit.add(new int[]{this.forward(thisHe),   that.forward(thatHe)});
        toVisit.add(new int[]{this.clockwise(thisHe), that.clockwise(thatHe)});
      
      // check that we have the same match, if not new
      } else if(thisMatch != thatHe || thatMatch != thisHe) {
        // uh-oh, we should always be traversing in the same order,
        // and shouldn't ever change a match
        // no good...
        return null;
      }
    }
    
    return new int[][]{thisVisited,thatVisited};
  }
  
  public PerfectMatchIterator allPerfectMatches(StrokeGraph that) {
    return new PerfectMatchIterator(this,that);    
  }
  
  static class PerfectMatchIterator implements Iterator<int[][]>,Iterable<int[][]> {

    StrokeGraph A,B;
    int aIndex, bIndex, numHalfEdges;
    int[][] nextMatch;
    
    PerfectMatchIterator(StrokeGraph A, StrokeGraph B) {
      this.A = A;
      this.B = B;
      
      aIndex = 0;
      bIndex = 0; // store an array of the possible edges in b, ordered by _likelihood_ of matching with aIndex  
      numHalfEdges = A.halfedges.size();
      
      if(A.halfedges.size() != B.halfedges.size()) {
        // fail flag
        nextMatch = null;
        bIndex = numHalfEdges;
      }
      
      lookForMatch();
    }
    
    private void lookForMatch() {
      nextMatch = null;
      
      while(nextMatch == null && bIndex < numHalfEdges) {
        nextMatch = A.perfectMatch(B, aIndex, bIndex);
        bIndex++;
      }
    }
    
    public boolean hasNext() {
      return nextMatch != null || bIndex < numHalfEdges;
    }

    public int[][] next() {
      int[][] result = nextMatch;
      lookForMatch();
      return result;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    public Iterator<int[][]> iterator() {
      return this;
    }
  }
  
  public int[][] slidingMatch(StrokeGraph that, int thisStart, int thatStart) {
    
    // sizes need to match
    if(this.halfedges.size() != that.halfedges.size()) return null;
    
    int[] thisVisited = new int[this.halfedges.size()];
    int[] thatVisited = new int[this.halfedges.size()];
    Arrays.fill(thisVisited, -1);
    Arrays.fill(thatVisited, -1);
    
    Queue<int[]> toVisit = new LinkedList<int[]>();
    toVisit.add(new int[]{thisStart,thatStart});
    
    while(!toVisit.isEmpty()) {
      int[] pair = toVisit.remove();
      int thisHe = pair[0];
      int thatHe = pair[1];
      
      // several references may be undefined
      // ok if both this and that are at the same time
      if(thisHe == -1 && thatHe == -1) continue;
      // but if only one is undefined, fail
      if((thisHe == -1) != (thatHe == -1)) return null; 
      
      // check if we've already traversed
      int thisMatch = thisVisited[thisHe];
      int thatMatch = thatVisited[thatHe];
      if(thisMatch == -1 && thatMatch == -1) {
        // remember the match, if new
        thisVisited[thisHe] = thatHe;
        thatVisited[thatHe] = thisHe;
        
        toVisit.add(new int[]{this.opposite(thisHe),  that.opposite(thatHe)});
        toVisit.add(new int[]{this.forward(thisHe),   that.forward(thatHe)});
        //toVisit.add(new int[]{this.clockwise(thisHe), that.clockwise(thatHe)});
      
      // check that we have the same match, if not new
      } else if(thisMatch != thatHe || thatMatch != thisHe) {
        // uh-oh, we should always be traversing in the same order,
        // and shouldn't ever change a match
        // no good...
        return null;
      }
    }
    
    return new int[][]{thisVisited,thatVisited};
  }


  int opposite(int index) {
    return halfedges.get(index).opposite;
  }
  int forward(int index) {
    return halfedges.get(index).forward;
  }
  int clockwise(int index) {
    return halfedges.get(index).clockwise;
  }
  
  static class StrokeGraphTraversal {
    int index;
    ArrayList<HalfEdge> halfedges;
    
    StrokeGraphTraversal(StrokeGraph graph, int index) {
      this.index = index;
      this.halfedges = graph.halfedges;
    }
    
    int clockwise() {
      index = halfedges.get(index).clockwise;
      return index;
    }
    
    int forward() {
      index = halfedges.get(index).forward;
      return index;
    }
    
    int opposite() {
      index = halfedges.get(index).opposite;
      return index;
    }
  }
  
  @Deprecated
  public static StrokeGraph build(pt[] junctions, int[][] strokes, vec[][] handles) {
    StrokeGraph graph = new StrokeGraph();
    for(int i = 0; i < strokes.length; i++) {
      vec[] tang = handles[i];
      int[] topo = strokes[i];
      if(topo.length > 2) {
        // for each circular span of four points
        for(int j = 0, pj = topo.length-1, ppj = pj-1, pppj = ppj-1; j < topo.length; pppj = ppj, ppj = pj, pj = j, j++) {
          int index = graph.halfedges.size();
          HalfEdge he = new HalfEdge();
          HalfEdge opp = new HalfEdge();
          // pair them
          opp.opposite = index;
          he.opposite = index+1;
          
          // make geometry
          he.disks = makeDisks(junctions[topo[pppj]], junctions[topo[ppj]], junctions[topo[pj]], junctions[topo[j]]);
          opp.disks = he.disks;
          opp.reversed = true;
          
          graph.halfedges.add(he);
          graph.halfedges.add(opp);
        }
      } else {
        int index = graph.halfedges.size();
        HalfEdge he = new HalfEdge();
        HalfEdge opp = new HalfEdge();
        // pair them
        opp.opposite = index;
        he.opposite = index+1;

        // make geometry
        he.disks = makeDisks(junctions[topo[0]], junctions[topo[1]], tang[0], tang[1]);
        opp.disks = he.disks;
        opp.reversed = true;
        
        graph.halfedges.add(he);
        graph.halfedges.add(opp);
      }
    }
    return graph;
  }
  
  private static ArrayList<Disk> makeDisks(pt a, pt b, vec va, vec vb) {
    return makeDisksBez(a, T(a,va), T(b,vb), b);
  }

  private static ArrayList<Disk> makeDisks(pt a, pt b, pt c, pt d) {
    vec ac = V(a,c), db = V(d,b);
    //float len = ac.norm()+db.norm()+V(b,c).norm();
    ac.normalize();
    db.normalize();
    float scale = V(b,c).norm()*0.33f;
    return makeDisksBez(b, T(b, scale, ac), T(c, scale, db), c);
  }
  
  private static ArrayList<Disk> makeDisksBez(pt a, pt b, pt c, pt d) {
    ArrayList<Disk> result = new ArrayList<Disk>();
    for(float t = 0; t < 1.1f; t += 0.1f) {
      pt p = bezierPoint(a, b, c, d, t);
      result.add(new Disk(p.x, p.y, 1));
    }
    return result;
  }
  
  public static StrokeGraph fromFile(String filename) {
    return StrokeGraph.fromRepr(loadStrings(new File(filename)));
  }
  
  public void toFile(String filename) {
    saveStrings(new File(filename), toRepr());
  }
  
  public void draw(Oplet p) {
    for(HalfEdge e : halfedges) {
      if(!e.reversed) {
        p.draw(e.disks);
      }
    }
  }
  
  public void draw(Oplet p, int halfEdgeIndex) {
    p.draw(halfedges.get(halfEdgeIndex).disks);
  }
  
  public void drawArrow(Oplet p, int halfEdgeIndex) {
    halfedges.get(halfEdgeIndex).arrow(p);
  }
  
  // adds number labels to each halfedge
  public void label(Oplet p) {
    for(int i = 0; i < halfedges.size(); i++) {
      HalfEdge e = halfedges.get(i);
      pt first = e.disks.get(0);
      pt middle = e.disks.get(e.disks.size()/2);
      pt last = e.disks.get(e.disks.size()-1);
      if(e.reversed) {
        // swap
        pt tmp = first;
        first = last;
        last = tmp;
      }
      vec perp = R(first,last);
      perp.normalize();
      
      pt labelLoc = T(middle, 15, perp); // should be related to font size
      p.text(i, labelLoc.x, labelLoc.y+8);
      
    }
  }
  
  public void arrows(Oplet p) {
    for(int i = 0; i < halfedges.size(); i++) {
      halfedges.get(i).arrow(p);
    }
  }

  public static StrokeGraph build(List<List<pt>> strokes) {
    StrokeGraphBuilder builder = new StrokeGraphBuilder();
    return builder.build(strokes);
  }
  

  public void drawRegions(Oplet p) {
    boolean[] visited = new boolean[halfedges.size()];
    for(int i = 0; i < halfedges.size(); i++) {
      int heIndex = i;
      List<HalfEdge> edges = new LinkedList<HalfEdge>();
      while(heIndex != -1 && !visited[heIndex]) {
        HalfEdge he = halfedges.get(heIndex);
        edges.add(he);
        visited[heIndex] = true;
        heIndex = he.clockwise;
      }
      if(heIndex != -1) {
        p.beginShape();
        for(HalfEdge he : edges) {
          if(he.reversed) {
            for(pt d : reversed(he.disks)) {
              p.vertex(d);
            }
          } else {
            for(pt d : he.disks) {
              p.vertex(d);
            }
          }
        }
        p.endShape();
      }
    }
  }
  
  public void drawClockwiseRefs(Oplet p) {
    for(HalfEdge he : halfedges) {
      if(he.clockwise > 0) {
        HalfEdge next = halfedges.get(he.clockwise);
        pt midpoint1 = he.midpoint();
        pt midpoint2 = next.midpoint();
        // TODO: arc between
        vec norm1 = he.headToTail().normalize();
        vec norm2 = next.headToTail().normalize();
        if(he.reversed) {
          norm1.turnLeft();
        } else {
          norm1.turnRight();
        }
        if(next.reversed) {
          norm2.turnRight();
        } else {
          norm2.turnLeft();
        }
        norm1.scaleBy(15);
        norm2.scaleBy(15);
        p.bezier(midpoint1, norm1, norm2, midpoint2);
      }
    }
  }

  // hei = half edge index
  public int n(int i) { if(i < 0) return -1; else return halfedges.get(i).clockwise; } // next
  public int f(int i) { if(i < 0) return -1; else return halfedges.get(i).forward; } // foward
  public int o(int i) { if(i < 0) return -1; else return halfedges.get(i).opposite; } // opposite
  public int p(int i) { return o(n(o(i))); } // previous
  public int b(int i) { return o(f(o(i))); } // backward

  
}
