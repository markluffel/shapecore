package shapecore;

import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import shapecore.interfaces.PointSet;
import shapecore.tuple.Pair;

// TODO: make this feature comparible with Polygon 
public class Polyline implements PointSet {
  pt[] points;

  public List<pt> getPoints() {
    return Arrays.asList(points);
  }

  public static List<Edge> edges(List<pt> pts) {
    List<Edge> result = new ArrayList<Edge>();
    for(int i = 1; i < pts.size(); i++) {
      result.add(new Edge(pts.get(i-1), pts.get(i)));
    }
    return result;
  }
  
  public static List<Float> angleDiffs(List<Edge> edges) {
    List<Float> result = new ArrayList<Float>();
    for(Pair<Edge,Edge> pair : pairs(edges)) {
      result.add(angle_diff(
        pair.fst.dir().angle(),
        pair.snd.dir().angle()
      ));
    }
    return result;
  } 
}
