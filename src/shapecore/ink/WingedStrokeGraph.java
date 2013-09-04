package shapecore.ink;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import shapecore.Oplet;
import shapecore.pt;

/**
 * A planar multigraph. 
 * 
 * Represented as a winged-edge structure. More or less.
 */
public class WingedStrokeGraph {

  
  ArrayList<Stroke> strokes; // edge
  ArrayList<Junction> junctions; // vertex
  ArrayList<Region> regions; // face
  
  public static class Stroke {
    ArrayList<pt> points;
    int headJunction = -1, tailJunction = -1;
    int headCwStroke = -1, headCcwStroke = -1;
    int tailCwStroke = -1, tailCcwStroke = -1;
    int headContinuationStroke = -1, tailContinuationStroke = -1; // TODO: implement these
    int id = -1; // TODO: remove
  }
  
  public static class Junction {
    pt location;
    int stroke = -1; // one of many
    int valence = -1;
    JunctionType type; // TODO: implement this
  }
  
  public static enum JunctionType {
    T, Y, X
  }
  
  public static class Region {
    int stroke = -1; // one of many
  }
  
  public static WingedStrokeGraph build(pt[] junctionLocations, int[][] connectivity) {
    
    if(connectivity.length != junctionLocations.length) {
      throw new IllegalArgumentException("Connectivity info must be the same length as junctions array");
    }
    
    final WingedStrokeGraph g = new WingedStrokeGraph();
    g.strokes = new ArrayList<Stroke>();
    g.junctions = new ArrayList<Junction>();
    g.regions = new ArrayList<Region>();
    
    Map<Integer,ArrayList<Stroke>> strokeMap = new HashMap<Integer,ArrayList<Stroke>>(); 
    
    // create junctions
    for(int i = 0; i < connectivity.length; i++) {
      Junction j = new Junction();
      j.valence = connectivity[i].length;
      j.location = junctionLocations[i];
      g.junctions.add(j);
    }
    
    // create halfedges to connect junctions
    for(int i = 0; i < connectivity.length; i++) {
      int firstStroke = g.strokes.size();
      if(connectivity[i].length > 0) {
        g.junctions.get(i).stroke = firstStroke;
      }
      for(int j = 0; j < connectivity[i].length; j++) {
        int otherEnd = connectivity[i][j];
        Stroke s = makeStroke(junctionLocations[i], junctionLocations[otherEnd]);
        s.headJunction = i;
        s.tailJunction = otherEnd;
        s.id = g.strokes.size(); // TODO: remove, needed because of lazy
        g.strokes.add(s);
        
        // remember these halfedges connected to this junction
        // we'll take a second pass and sort them to figure out cw ccw
        addOrCreate(strokeMap, i, s);
        addOrCreate(strokeMap, otherEnd, s);
      }
    }
    
    // for each junction
    for(int i = 0; i < connectivity.length; i++) {
      
      // sort the halfedges at this junction into clockwise order around the junction
      final int j = i;
      final pt junction = junctionLocations[i];
      ArrayList<Stroke> strokes = strokeMap.get(i);
      Collections.sort(strokes, new Comparator<Stroke>() {

        public int compare(Stroke a, Stroke b) {
          pt aOther, bOther;
          // check which end of the stroke is away from the junction
          if(a.headJunction == j) {
            aOther = g.junctions.get(a.tailJunction).location;
          } else {
            aOther = g.junctions.get(a.headJunction).location;
          }
          if(b.headJunction == j) {
            bOther = g.junctions.get(b.tailJunction).location;
          } else {
            bOther = g.junctions.get(b.headJunction).location;
          }
          
          // pick the other end of a, and of b, compare their angle relative to junction
          return Float.compare(angle(V(junction,aOther)), angle(V(junction,bOther)));
        }
      });
      
      // link all incoming/outgoing halfedges to this junction up with id references
      // each pass through this loop sets two references
      // at the end of the loop, ever stroke at this junction will have both of its head or tail references set
      // each reference will only be set once in this function
      for(int k = 0, pk = strokes.size()-1; k < strokes.size(); pk = k, k++) {
        Stroke prev = strokes.get(pk), next = strokes.get(k);
        if(prev.headJunction == i) {
          prev.headCcwStroke = next.id;
        } else {
          prev.tailCcwStroke = next.id;
        }
        if(next.headJunction == i) {
          next.headCwStroke = prev.id;
        } else {
          next.tailCwStroke = prev.id;
        }
      }
    }
    
    return g;
  }
  
  // sample a straight line between the points
  // to visualize a multigraph we'll need something better
  private static Stroke makeStroke(pt a, pt b) {
    Stroke s = new Stroke();
    s.points = new ArrayList<pt>();
    float stepSize = 0.25f;
    for(float t = 0; t < 1+stepSize; t += stepSize) {
      s.points.add(lerp(a,b,t));
    }
    return s;
  }
  
  /*
  private static int[][] makeSymmetric(int[][] connectivity) {
    Map<Integer, ArrayList<Integer>> symm = new HashMap<Integer, ArrayList<Integer>>();
    for(int i = 0; i < connectivity.length; i++) {
      for(int j = 0; j < connectivity[i].length; j++) {
        addOrCreate(symm, i, j);
        addOrCreate(symm, j, i);
      }
    }
    int[][] result = new int[connectivity.length][];
    for(int i = 0; i < result.length; i++) {
      ArrayList<Integer> values = symm.get(i);
      if(values != null) {
        result[i] = new int[values.size()];
      } else {
        result[i] = new int[0];
      }
    }
    return connectivity;
  }
  */
  
  private static <S,T> void addOrCreate(Map<S, ArrayList<T>> map, S key, T value) {
    ArrayList<T> values = map.get(key);
    if(values == null) {
      values = new ArrayList<T>(); 
      map.put(key, values);
    }
    values.add(value);
  }
  
  
  public void draw(Oplet p) {
    for(Stroke s : strokes) {
      p.beginShape();
      p.draw(s.points);
      p.endShape();
    }
  }
  
  public void drawReferences(Oplet p) {
    
  }
  
  
  
  /**
   * Match two graphs exactly, given a single starting match.
   * 
   * 
   * @param that another stroke graph to match with
   * @return an array such that this.strokes.get(i) matches that.strokes.get(result[i]), or null if a perfect match isn't possible
   */
  int[] perfectMatch(WingedStrokeGraph that, int thisMatch, int thatMatch) {
    if(this.strokes.size() != that.strokes.size()) return null;
    if(this.junctions.size() != that.junctions.size()) return null;
    
    int[] matches = new int[this.strokes.size()];
    
    return new int[0];
  }
}
