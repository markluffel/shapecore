package shapecore.impl;

import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.List;

import shapecore.Edge;
import shapecore.pt;
import shapecore.interfaces.EdgeSet;
import shapecore.tuple.Pair;

public class EdgeSetMethods {

  public static pt project(EdgeSet es, pt q) {
    float minSqDist = Float.MAX_VALUE;
    pt best = q;
    for(Edge e : es.getEdges()) {
      pt p = e.projection(q);
      float sqdist = p.sqdist(q);
      if(sqdist < minSqDist) {
        minSqDist = sqdist;
        best = p;
      }
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
      result.add(angle_diff(
        pair.fst.dir().angle(),
        pair.snd.dir().angle()
      ));
    }
    return result;
  }
}
