package shapecore.ink;

import static shapecore.Geometry.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import shapecore.BoundingBox;
import shapecore.pt;
import shapecore.ink.StrokeGraph.HalfEdge;



public class StrokeGraphBuilder {

  StrokeGraph graph;
  int numIntersections = 0;
  public float endExtension = 3;
  
  public StrokeGraph build(ArrayList<ArrayList<pt>> strokes) {
    graph = new StrokeGraph();
    numIntersections = 0;
    
    // goals: detect overlaps, detect near-overlaps, connect stuff up
    
    // version 1:
    //  record intersections as relationships between pairs of edges
    //  a) compute self-intersections for each stroke
    //  b) compute bounding boxes for strokes
    //  c) compute intersections between strokes
    //  d) split edges based on known intersections
    //  e) create nodes at each intersection
    //  f) connect forward and opposite pointers
    //  g) order outgoing edges at each intersection
    //  h) connect "clockwise" pointers
    
    // first index is one of the involved strokes
    // second index is not meaningful
    ArrayList<ArrayList<Intersection>> intersections = new ArrayList<ArrayList<Intersection>>();
    BoundingBox[] bounds = new BoundingBox[strokes.size()];
    for(int i = 0; i < strokes.size(); i++) {
      intersections.add(new ArrayList<Intersection>());
      bounds[i] = new BoundingBox(strokes.get(i));
    }
    
    for(int i = 0; i < strokes.size(); i++) {
      List<Intersection> intrs = selfIntersect(strokes.get(i), i);
      numIntersections += intrs.size();
      // add all self-intersections twice
      // because the underlying stroke needs to be cut at two points
      // when we sort them later, we'll have two copies of this intersection
      // one at the first location, and one at the second location
      if(!intrs.isEmpty()) {
        intersections.get(i).addAll(intrs);
        for(Intersection intr : intrs) {
          intersections.get(i).add(intr.flipped());
        }
      }
    }
    for(int i = 0; i < strokes.size(); i++) {
      for(int j = i+1; j < strokes.size(); j++) {
        if(bounds[i].intersects(bounds[j])) {
          List<Intersection> intrs = intersect(strokes.get(i), strokes.get(j), i, j);
          numIntersections += intrs.size();
          intersections.get(i).addAll(intrs);
          intersections.get(j).addAll(intrs);
        }
      }
    }
    
    // mapping from an intersection to all of the halfedges leaving it
    // halfedge represented by its index
    List<List<Integer>> outgoing = new ArrayList<List<Integer>>();
    for(int i = 0; i < numIntersections; i++) {
      outgoing.add(new LinkedList<Integer>());
    }
    
    // splitting loop
    for(int i = 0; i < strokes.size(); i++) {
      ArrayList<Intersection> intrs = intersections.get(i);
      final int index = i;
      Collections.sort(intrs, new Comparator<Intersection>() {
        public int compare(Intersection a, Intersection b) {
          int aIndex, bIndex;
          if(a.stroke1 == index) {
            aIndex = a.segment1;
          } else {
            aIndex = a.segment2;
          }
          if(b.stroke1 == index) {
            bIndex = b.segment1;
          } else {
            bIndex = b.segment2;
          }
          
          if(aIndex < bIndex)
            return -1;
          if(aIndex > bIndex)
            return 1;
          return 0;
        }
      });
      int firstHalfEdgeAlongStroke = graph.halfedges.size();
      ArrayList<pt> stroke = strokes.get(i);
      int fromIndex = 0;
      Intersection prevIntersection = null;
      for(int j = 0; j < intrs.size(); j++) {
        Intersection here = intrs.get(j);
        int segmentIndex;
        if(here.stroke1 == i) {
          segmentIndex = here.segment1;
        } else {
          segmentIndex = here.segment2;
        }
        
        List<pt> centers = new LinkedList<pt>(stroke.subList(fromIndex, segmentIndex));
        centers.add(here.location);
        if(prevIntersection != null) {
          centers.add(0, prevIntersection.location);
        }
        
        int halfEdgeIndex = graph.halfedges.size();
        makeHalfEdges(centers);
        
        outgoing.get(here.index).add(halfEdgeIndex+1);
        if(j > 0 && j+1 < intrs.size()) { // don't add the opposite of the first or last 
          Intersection next = intrs.get(j);
          outgoing.get(next.index).add(halfEdgeIndex);
        }
        
        // setup for the next loop
        prevIntersection = here;
        fromIndex = segmentIndex;
      }
      
      if(prevIntersection != null) {
        List<pt> centers = new LinkedList<pt>(stroke.subList(fromIndex, stroke.size()));
        centers.add(0, prevIntersection.location);
        
        int halfEdgeIndex = graph.halfedges.size();
        makeHalfEdges(centers);
        outgoing.get(prevIntersection.index).add(halfEdgeIndex);
        
      } else {
        // no intersections
        makeHalfEdges(stroke);
      }
      
      // connect the forward pointers
      for(int j = firstHalfEdgeAlongStroke; j < graph.halfedges.size()-2; j += 2) {
        graph.halfedges.get(j+0).forward = j+2;
        graph.halfedges.get(j+3).forward = j+1;
      }
    }
    
    for(int i = 0; i < outgoing.size(); i++) {
      List<Integer> incident = outgoing.get(i);
      Collections.sort(incident, new Comparator<Integer>() {
        public int compare(Integer ei1, Integer ei2) {
          HalfEdge e1 = graph.halfedges.get(ei1);
          HalfEdge e2 = graph.halfedges.get(ei2);
          return Float.compare(e1.tailDirection().angle(), e2.tailDirection().angle());
        }
      });
      for(int pj = incident.size()-1, j = 0; j < incident.size(); pj = j, j++) {
        int prevI = incident.get(pj);
        int nextI = incident.get(j);
        HalfEdge prev = graph.halfedges.get(graph.halfedges.get(prevI).opposite);
        prev.clockwise = nextI;
      }
    }
    
    graph.numIntersections = numIntersections;
    return graph;
  }  

  // this won't work for self intersections
  private List<Intersection> intersect(List<pt> A, List<pt> B, int aIndex, int bIndex) {
    List<Intersection> intersections = new LinkedList<Intersection>();
    for(int pi = 0, i = 1; i < A.size(); pi = i, i++) {
      for(int pj = 0, j = 1; j < B.size(); pj = j, j++) {
        pt a1 = A.get(pi), a2 = A.get(i);
        pt b1 = B.get(pj), b2 = B.get(j);
        if(pi == 0) {
          a1 = a1.get().translateTowardsBy(-endExtension, a2);
        }
        if(pj == 0) {
          b1 = b1.get().translateTowardsBy(-endExtension, b2);
        }
        pt intr = edgeIntersection(a1, a2, b1, b2);
        if(intr != null) {
          intersections.add(new Intersection(numIntersections, intr, aIndex, i, bIndex, j));
          numIntersections++;
        }
      }
    }
    return intersections;
  }
  
  private List<Intersection> selfIntersect(List<pt> A, int aIndex) {
    List<Intersection> intersections = new LinkedList<Intersection>();
    for(int pi = 0, i = 1; i < A.size()-1; pi = i, i++) {
      for(int pj = i+1, j = pj+1; j < A.size(); pj = j, j++) {
        pt a1 = A.get(pi), a2 = A.get(i);
        pt b1 = A.get(pj), b2 = A.get(j);
        pt intr = edgeIntersection(a1, a2, b1, b2);
        if(intr != null) {
          intersections.add(new Intersection(numIntersections, intr, aIndex, i, aIndex, j));
          numIntersections++;
        }
      }
    }
    return intersections;
  }
  
  private static class Intersection {
    int index;
    pt location;
    // a stroke has n points and n-1 segments
    // the first segment is 0 and touches point 0
    int stroke1, segment1, stroke2, segment2;
    
    public Intersection(int index, pt location, int stroke1, int segment1, int stroke2, int segment2) {
      this.index = index;
      this.location = location;
      this.stroke1 = stroke1;
      this.segment1 = segment1;
      this.stroke2 = stroke2;
      this.segment2 = segment2;
    }

    // for self-intersection, add each twice, once flipped
    public Intersection flipped() {
      return new Intersection(index, location, stroke2, segment2, stroke1, segment1);
    }
    
    public String toString() {
      return "#"+index+" {stroke1: "+stroke1+", seg1: "+segment1+", stroke2: "+stroke2+", seg2: "+segment2+"}";
    }
  }
  
  private void makeHalfEdges(List<pt> centers) {
    int halfEdgeIndex = graph.halfedges.size();
    
    HalfEdge he = new HalfEdge(centers);
    HalfEdge op = new HalfEdge();
    op.disks = he.disks;
    op.reversed = true;
    
    op.opposite = halfEdgeIndex;
    he.opposite = halfEdgeIndex+1;
    
    graph.halfedges.add(he);
    graph.halfedges.add(op);
  }
}
