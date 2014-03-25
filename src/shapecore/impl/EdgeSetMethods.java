package shapecore.impl;

import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.List;

import shapecore.Edge;
import shapecore.Line;
import shapecore.Ray;
import shapecore.pt;
import shapecore.interfaces.EdgeSet;
import shapecore.tuple.Pair;

public class EdgeSetMethods {

  public static pt project(EdgeSet es, pt q) {
    return project(es.getEdges(), q);
  }
  public static pt project(List<Edge> es, pt q) {
    float minSqDist = Float.MAX_VALUE;
    pt best = q;
    for(Edge e : es) {
      pt p = e.projection(q);
      float sqdist = p.sqdist(q);
      if(sqdist < minSqDist) {
        minSqDist = sqdist;
        best = p;
      }
    }
    return best;
  }
  
  public static pt intersect(EdgeSet es, Ray ray) {
    return intersect(es.getEdges(), ray);
  }
  public static pt intersect(List<Edge> es, Ray ray) {
    float minSqDist = Float.MAX_VALUE;
    pt best = null;
    for(Edge e : es) {
      pt p = e.intersection(ray);
      if(p == null) continue;
      float sqdist = p.sqdist(ray.start);
      if(sqdist < minSqDist) {
        minSqDist = sqdist;
        best = p;
      }
    }
    return best;
  }
  
  public static List<pt> intersections(EdgeSet es, Ray ray) {
    return intersections(es.getEdges(), ray);
  }
  public static List<pt> intersections(List<Edge> es, Ray ray) {
    List<pt> results = new ArrayList<pt>();
    for(Edge e : es) {
      pt p = e.intersection(ray);
      if(p == null) continue;
      results.add(p);
    }
    return results;
  }
  
  public static List<pt> intersections(EdgeSet es, Line line) {
    return intersections(es.getEdges(), line);
  }
  public static List<pt> intersections(List<Edge> es, Line line) {
    List<pt> results = new ArrayList<pt>();
    for(Edge e : es) {
      pt p = e.intersection(line);
      if(p == null) continue;
      results.add(p);
    }
    return results;
  }
  
  public static int closestIndex(EdgeSet es, pt q) {
    return closestIndex(es.getEdges(), q);
  }
  public static int closestIndex(List<Edge> es, pt q) {
    float minSqDist = Float.MAX_VALUE;
    int best = -1;
    int i = 0;
    for(Edge e : es) {
      pt p = e.projection(q);
      float sqdist = p.sqdist(q);
      if(sqdist < minSqDist) {
        minSqDist = sqdist;
        best = i;
      }
      i++;
    }
    return best;
  }
  
  public static float dist(EdgeSet es, pt q) {
    return project(es, q).dist(q);
  }
  
  /** Assumes that edges are are connected and provided */
  public static List<Float> angleDiffs(EdgeSet e) {
    List<Float> result = new ArrayList<Float>();
    for(Pair<Edge,Edge> pair : pairs(e.getEdges())) {
      result.add(angleDiff(
        pair.fst.dir().angle(),
        pair.snd.dir().angle()
      ));
    }
    return result;
  }
}
