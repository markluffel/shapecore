package shapecore;

import java.util.ArrayList;
import java.util.List;

import shapecore.impl.EdgeSetMethods;
import shapecore.interfaces.EdgeSet;
import shapecore.interfaces.PointSet;

// TODO: make this feature comparible with Polygon 
public class Polyline implements PointSet, EdgeSet {
  List<pt> points = new ArrayList<pt>();

  public List<pt> getPoints() {
    return points;
  }
  
  public void add(pt p) {
    points.add(p);
  }
  
  public int size() {
    return points.size();
  }
  
  public pt get(int i) {
    return points.get(i);
  }
  
  public List<Edge> getEdges() { return getEdges(getPoints()); }

  public static List<Edge> getEdges(List<pt> pts) {
    List<Edge> result = new ArrayList<Edge>();
    for(int i = 1; i < pts.size(); i++) {
      result.add(new Edge(pts.get(i-1), pts.get(i)));
    }
    return result;
  }
  
  public pt project(pt q) { return EdgeSetMethods.project(this, q); }
}
