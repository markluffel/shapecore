package shapecore;

import static processing.core.PApplet.*;
import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import shapecore.interfaces.PointSet;




/**
 * Several utility methods for transforming collections of points as a whole group.
 * 
 */
public class PointSets {

  // -----------------------------------------------------------------------
  // PointSet versions
  public static void translate(PointSet ps, vec offset) {
    translate(ps.getPoints(), offset);
  }
  
  public static void scale(PointSet ps, pt center, float scale) {
    scale(ps.getPoints(), center, scale);
  }

  public static void rotate(PointSet ps, float angle) {
    List<pt> pts = ps.getPoints();
    rotate(pts, angle, centerV(pts));
  }

  public static void rotate(PointSet ps, float angle, pt center) {
    rotate(ps.getPoints(), angle, center);
  }
  
  public static void registerTo(PointSet src, PointSet dst) {
    registerTo(src.getPoints(), dst.getPoints());
  }
  
  public static void registerVolumeTo(PointSet src, PointSet dst) {
    registerVolumeTo(src.getPoints(), dst.getPoints());
  }
  
  public static void center(PointSet ps) {
    translate(ps, V(centerV(ps.getPoints()),new pt()));
  }
  
  // -----------------------------------------------------------------------
  // Array versions
  public static void translate(pt[] points, vec offset) {
    translate(Arrays.asList(points), offset);
  }
  
  public static void scale(pt[] points, pt center, float scale) {
    scale(Arrays.asList(points), center, scale);
  }

  public static void rotate(pt[] points, float angle) {
    rotate(points, angle, average(points));
  }

  public static void rotate(pt[] points, float angle, pt center) {
    rotate(Arrays.asList(points), angle, center);
  }
  
  public static void registerTo(pt[] src, pt[] dst) {
    registerTo(Arrays.asList(src), Arrays.asList(dst));
  }
  
  // -----------------------------------------------------------------------
  // List/Collection versions
  public static void translate(Collection<pt> points, vec offset) {
    for(pt pt : points) pt.translateBy(offset);
  }
  
  public static void scale(Collection<pt> points, pt center, float scale) {
    for(pt pt : points) pt.translateTowardsByRatio(scale, center);
  }

  public static void rotate(Collection<pt> points, float angle) {
    rotate(points, angle, centerV(points));
  }

  public static void rotate(Collection<pt> points, float angle, pt center) {
    for(pt pt : points) pt.rotateBy(angle, center);
  }
  
  // this has to be lists, not collections because we need a one-to-one correspondence
  public static void registerTo(List<pt> src, List<pt> dst) {
    pt thisCenter = centerV(src);
    pt thatCenter = centerV(dst);
    translate(src, V(thisCenter, thatCenter));
    
    float s = 0, c = 0;
    int min = min(src.size(), dst.size());
    
    for (int i = 0; i < min; i++) {
      vec srcV = V(thatCenter, src.get(i)), dstV = V(thatCenter, dst.get(i)); 
      s += dot(srcV, R(dstV));
      c += dot(srcV, dstV);
    }
    
    float angle = atan2(s, c);
    rotate(src, -angle, thatCenter);
  }
  
  public static void registerVolumeTo(List<pt> src, List<pt> dst) {
    pt thisCentroid = centroid(src);
    pt thatCentroid = centroid(dst);
    // this could be made more efficient by unrolling the area computing loops above into the below
    float s = 0, c = 0;
    int min = min(src.size(), dst.size());
    float areaSum = 0;
    // i have serious doubts about this code
    for (int i = 0, pi = min-1; i < min; pi = i, i++) {
      pt s1 = src.get(pi), s2 = src.get(i);
      pt d1 = dst.get(pi), d2 = dst.get(i);
      pt srcMid = A(s1,s2);
      pt dstMid = A(dst.get(pi), dst.get(i));
      vec srcV = V(thisCentroid, srcMid), dstV = V(thatCentroid, dstMid);
      float area = trapezoidArea(s1,s2)+trapezoidArea(d1,d2);
      s += dot(srcV, R(dstV))*area;
      c += dot(srcV, dstV)*area;
      areaSum += area;
    }
    
    translate(src, V(thisCentroid, thatCentroid));
    float angle = atan2(s/areaSum, c/areaSum); // this area 
    rotate(src, -angle, thatCentroid);
  }
}
