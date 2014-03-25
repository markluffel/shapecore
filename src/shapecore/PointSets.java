package shapecore;

import static processing.core.PApplet.*;
import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

import shapecore.interfaces.PointSet;
import shapecore.motion.Rigid;




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
  
  @Deprecated // rename this
  public static void center(PointSet ps) {
    translate(ps, centerV(ps.getPoints()).to(new pt()));
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
    pt thisCentroid = centroidOfPolygon(src);
    pt thatCentroid = centroidOfPolygon(dst);
    // this could be made more efficient by unrolling the area computing loops above into the below
    float s = 0, c = 0;
    int min = min(src.size(), dst.size());
    float areaSum = 0;
    // i have serious doubts about this code
    for (int i = 0, pi = min-1; i < min; pi = i, i++) {
      pt s1 = src.get(pi), s2 = src.get(i);
      pt d1 = dst.get(pi), d2 = dst.get(i);
      pt srcMid = average(s1,s2);
      pt dstMid = average(dst.get(pi), dst.get(i));
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
  
  /**
   * Basically a brute force search for the minimum area bounding rectangle.
   * Constructs the convex hull, then for each face of the hull,
   * computes the area of a rectangle using that as one side.
   * This is O(n^2) in the number of faces of the convex hull. 
   * @return
   */
  public static OrientedBoundingBox calcBoundsConvexMin(List<pt> points) {
    if(points.size() < 3) return specialCase(points); 
    
    List<pt> hull = convexHull(points);
    float minArea = Float.MAX_VALUE;
    vec bestDir = new vec(0,1);
    for(Edge edge : Polygon.edges(hull)) {
      vec dir = U(edge.dir());
      if(dir.isNull()) continue;
      float area = area(hull, dir);
      if(area < minArea) {
        minArea = area;
        bestDir = dir;
      }
    }
    return new OrientedBoundingBox(mostVertical(bestDir), points); // could pass hull here
  }
  
  // pick box with the biggest major axis to minor axis ratio
  public static OrientedBoundingBox calcBoundsConvexAligned(List<pt> points) {
    if(points.size() < 3) return specialCase(points);
    
    List<pt> hull = convexHull(points);
    float bestAspect = 0;
    vec bestDir = new vec(0,1);
    for(Edge edge : Polygon.edges(hull)) {
      vec dir = U(edge.dir());
      if(dir.isNull()) continue;
      float aspect = aspect(hull, dir);
      if(aspect > bestAspect) {
        bestAspect = aspect;
        bestDir = dir;
      }
    }
    return new OrientedBoundingBox(mostVertical(bestDir), points); // could pass hull here

  }
  
  public static OrientedBoundingBox specialCase(List<pt> points) {
    switch(points.size()) {
    case 0:
      return null; // really, what can we do here?
    case 1:
      return new OrientedBoundingBox(new vec(0,1), points);
    case 2:
      pt a = points.get(0), b = points.get(1);
      return new OrientedBoundingBox(V(a,b), points);
    default:
      throw new IllegalStateException();
    }
  }
  
  
  static List<pt> convexHull(List<pt> points) {
    if(points.size() == 3) { //  || Polygon.isConvex(points) // TODO: add this clause after the code is tested/optimized
      return points;
    } else {
      pt[] pts = points.toArray(new pt[0]);
      return Arrays.asList(new GrahamScan(pts).getPoints());
    }
  }
  
  static vec mostVertical(vec v) {
    vec goal = new vec(0,-1);
    float max = -1;
    vec best = v;
    // try the four possible configurations
    for(int i = 0; i < 4; i++) {
      // take the one most like our goal
      float d = v.dot(goal);
      if(d > max) {
        max = d;
        best = v;
      }
      v = R(v); // rotate 90
    }
    return best;
  }
  
  
  static float[] sideLengths(List<pt> pts, vec dir) {
    vec side = R(dir);
    float minDir = Float.MAX_VALUE, maxDir = -Float.MAX_VALUE;
    float minSide = Float.MAX_VALUE, maxSide = -Float.MAX_VALUE;
    for(int i = 0; i < pts.size(); i++) {
      vec off = V(pts.get(i));
      float dd = dot(off, dir);
        if(dd < minDir) minDir = dd;
        if(dd > maxDir) maxDir = dd;
      float ds = dot(off, side);
        if(ds < minSide) minSide = ds;
        if(ds > maxSide) maxSide = ds;
    }
    return new float[]{maxSide-minSide, maxDir-minDir};
  }
  /**
   * Calculate the area of an oriented bounding box with one axis in the direction dir 
   * @param pts
   * @param dir
   * @return
   */
  static float area(List<pt> pts, vec dir) {
    float[] sides = sideLengths(pts, dir);
    return sides[0]*sides[1];
  }
  
  static float aspect(List<pt> pts, vec dir) {
    float[] sides = sideLengths(pts, dir);
    return max(sides)/min(sides);
  }
  

  // it would be nice if java's type system smiled upon generalization
  // but as it is, we'll have to settle for some code duplication
  public static pt[] clonePoints(pt[] items) {
    pt[] cloned = items.clone();
    for(int i = 0; i < items.length; i++) {
      cloned[i] = items[i].clone();
    }
    return cloned;
  }
  
  /**
   * Inplace clone, for when the original array was created in a unique way, but the points are aliased
   * @param items
   */
  public static void _clonePoints(pt[] items) {
    for(int i = 0; i < items.length; i++) {
      items[i] = items[i].clone();
    }
  }
  
  public static List<pt> clonePoints(List<pt> items) {
    List<pt> result = new ArrayList<pt>();
    for(pt p : items) {
      result.add(p.get());
    }
    return result;
  }

  public static void transform(pt[] pts, Rigid xform, float t) {
    for(pt p : pts) {
      p.set(xform.apply(p, t));
    }
  }

  public static OrientedBoundingBox calcBoundsSvdVertices(List<pt> points) {
    pt c = centroidOfPolygon(points);
    Matrix m = new Matrix(2, points.size());
    for(int i = 0; i < points.size(); i++) {
      pt p = points.get(i);
      m.set(0, i, p.x-c.x);
      m.set(1, i, p.y-c.y);
    }
    
    SingularValueDecomposition svd = m.svd();
    Matrix U_ = svd.getU();
    Matrix V_ = svd.getV();
    //println("v U: "+U_.getRowDimension()+" "+U_.getColumnDimension());
    //println("v S: "+svd.getSingularValues().length);
    //println("v V: "+V_.getRowDimension()+" "+V_.getColumnDimension());
    return new OrientedBoundingBox(V(U_.get(0,0),U_.get(1,0)), points);
  }
  
  public static OrientedBoundingBox calcBoundsSvdEdges(List<pt> points) {
    Matrix m = new Matrix(2, points.size());
    for(int li = points.size()-1, ri = 0; ri < points.size(); li = ri, ri++) {
      pt l = points.get(li), r = points.get(ri);
      m.set(0, ri, l.x-r.x);
      m.set(1, ri, l.y-r.y);
    }
    //pt c = centroid(pts);
    SingularValueDecomposition svd = m.svd();
    Matrix U_ = svd.getU();
    Matrix V_ = svd.getV();
    //println("e U: "+U_.getRowDimension()+" "+U_.getColumnDimension());
    //println("e S: "+svd.getSingularValues().length);
    //println("e V: "+V_.getRowDimension()+" "+V_.getColumnDimension());
    return new OrientedBoundingBox(V(U_.get(0,0),U_.get(1,0)), points);
  }
}
