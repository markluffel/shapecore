package shapecore;

import static shapecore.Geometry.*;

import java.util.ArrayList;
import java.util.List;

import shapecore.impl.EdgeSetMethods;
import shapecore.interfaces.EdgeSet;
import shapecore.interfaces.PointSet;

// TODO: make this feature comparable with Polygon 
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
  
  public static List<Corner> getCorners(List<pt> pts) {
    List<Corner> result = new ArrayList<Corner>();
    int len = pts.size()-1;
    for(int i = 1; i < len; i++) {
      result.add(new Corner(pts.get(i-1), pts.get(i), pts.get(i+1)));
    }
    return result;
  }
  
  public pt project(pt q) {
    return EdgeSetMethods.project(this, q);
  }
  
  public static List<vec> laplacian(List<pt> vecs) {
    List<vec> result = new ArrayList<vec>();
    result.add(new vec(0,0));
    for(int i = 1; i < vecs.size()-1; i++) {
      pt a = vecs.get(i-1), b = vecs.get(i), c = vecs.get(i+1);
      vec v = (b.to(a)).add(b.to(c));
      result.add(v.scaleBy(0.5f));
    }
    result.add(new vec(0,0));
    return result;
  }
  

  public static void smooth(List<? extends pt> pts) { smooth(pts, 0.5f); }
  
  public static void smooth(List<? extends pt> pts, double t) {
    smooth(pts, (float)t);
  }
  
  public static void smooth(List<? extends pt> pts, float t) {
    // FIXME: this needs to be optimized in all kinds of ways
    t /= 2;
    int endex = pts.size()-1;
    List<vec> change = new ArrayList<vec>();
    change.add(new vec(0,0));
    for(int i = 1; i < endex; i++) {
      pt
      A = pts.get(i-1),
      B = pts.get(i),
      C = pts.get(i+1);
      change.add(S(t, S(B.to(A), B.to(C))));
    }
    change.add(new vec(0,0));
    
    for(int i = 0; i < pts.size(); i++) {
      pts.get(i).add(change.get(i));
    }
  }
  
  public static void smooth(pt[] pts, float t) {
    // this needs to be optimized in all kinds of ways
    if(pts.length < 3) return;
    t /= 2;
    int endex = pts.length-1;
    ArrayList<vec> change = new ArrayList<vec>();
    change.add(new vec(0,0));
    for(int i = 1; i < endex; i++) {
      pt
      A = pts[i-1],
      B = pts[i],
      C = pts[i+1];
      vec BA = V(B,A), BC = V(B,C);
      change.add(S(t, S(BA, BC)));
    }
    change.add(new vec(0,0));
    
    for(int i = 0; i < pts.length; i++) {
      pts[i].add(change.get(i));
    }
  }
  
  /**
   * smoothing subject to a data term (as in s.thrun's smoothing)
   */
  public static void smoothing(List<pt> pts, float dataWeight, int iterations) {
    List<pt> original = new ArrayList<pt>();
    for(pt p : pts) original.add(p.get());
    
    for(int k = 0; k < iterations; k++) {
      float t = 0.5f;
      int endex = pts.size()-1;
      List<vec> change = new ArrayList<vec>();
      change.add(new vec(0,0));
      for(int i = 1; i < endex; i++) {
        pt
        A = pts.get(i-1),
        B = pts.get(i),
        C = pts.get(i+1);
        vec BA = V(B,A), BC = V(B,C);
        change.add(S(t, S(BA, BC)));
      }
      change.add(new vec(0,0));
      
      for(int j = 0; j < pts.size(); j++) {
        pts.get(j).add(change.get(j));
        pts.get(j).translateTowardsByRatio(dataWeight, original.get(j));
      }
    }
  }

  /**
   * smoothing subject to a data term (as in s.thrun's smoothing)
   * pinned is a list of indices to which the data term should be applied, others are skipped
   */
  public static void smoothing(List<pt> pts, List<Integer> pinned, float dataWeight, int iterations) {
    List<pt> original = new ArrayList<pt>();
    for(pt p : pts) original.add(p.get());
    
    for(int k = 0; k < iterations; k++) {
      float t = 0.5f;
      int endex = pts.size()-1;
      List<vec> change = new ArrayList<vec>();
      change.add(new vec(0,0));
      for(int i = 1; i < endex; i++) {
        pt
        A = pts.get(i-1),
        B = pts.get(i),
        C = pts.get(i+1);
        vec BA = V(B,A), BC = V(B,C);
        change.add(S(t, S(BA, BC)));
      }
      change.add(new vec(0,0));
      
      for(int j = 0; j < pts.size(); j++) {
        pts.get(j).add(change.get(j));
      }
      for(int j : pinned) {
        pts.get(j).translateTowardsByRatio(dataWeight, original.get(j));
      }
    }
  }
  
  public static float arclength(List<pt> pts) {
    if(pts.isEmpty()) return 0;
    float arcLen = 0;
    pt prev = pts.get(0);
    for(int i = 1; i < pts.size(); i++) {
      pt cur = pts.get(i);
      arcLen += cur.dist(prev);
      prev = cur;
    }
    return arcLen;
  }
}
