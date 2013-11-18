/**
 * 
 */
package shapecore;

import static processing.core.PApplet.*;
import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import megamu.mesh.InteriorTest;
import processing.core.PConstants;
import shapecore.fancy.LloydRelaxation;
import shapecore.impl.EdgeSetMethods;
import shapecore.interfaces.EdgeSet;
import shapecore.interfaces.PointSet;
import shapecore.mesh.Mesh2D;
import shapecore.mesh.MeshingSettings;
import shapecore.mesh.TriMesh2D;
import shapecore.tuple.Pair;

public class Polygon implements PointSet, EdgeSet {
  List<pt> points;
  
  public Polygon() {
    this.points = new ArrayList<pt>();
  }
  
  public Polygon(pt[] _points) {
    this();
    if(_points == null) throw new IllegalArgumentException();
    for(pt p : _points) {
      if(p == null) throw new IllegalArgumentException();
      points.add(p);
    }
  }
  
  public Polygon(List<pt> points) {
    this(points.toArray(new pt[0]));
  }
  
  public void draw(Oplet p) {
    p.beginShape();
    for(pt pt : points) {
      p.vertex(pt);
    }
    p.endShape(PConstants.CLOSE);
  }

  public List<pt> getPoints() {
    return points;
  }
  
  public pt[] getPointsArray() {
    pt[] pts = new pt[numPoints()];
    for(int i = 0; i < pts.length; i++) {
      pts[i] = points.get(i);
    }
    return pts;
  }
  
  public int numPoints() {
    return points.size();
  }

  public void copyFrom(Polygon that) {
    points.clear();
    for(pt p : that.points) {
      points.add(p.get());
    }
  }
  
  public void refine(float s) {
    points = refine(points, s);
  }

  public static List<pt> refine(List<pt> P, float s) {
    int n = P.size();
    List<pt> result = new ArrayList<pt>(2 * n);
    for (int i = 0; i < n; i++) {
      result.add(b(next(P, i), P.get(i), next(P, i), s));
      result.add(f(prev(P, i), P.get(i), next(P, i), next(P, nextI(P, i)), s));
    }
    return result;
  }

  // TODO: convert to lerp, come up with human names
  static pt s(pt a, float s, pt b) {
    return (new pt(a.x + s * (b.x - a.x), a.y + s * (b.y - a.y)));
  }
  protected static pt b(pt a, pt b, pt c, float s) {
    return (s(s(b, s / 4f, a), 0.5f, s(b, s / 4f, c)));
  }
  protected static pt f(pt a, pt b, pt c, pt d, float s) {
    return (s(s(a, 1f + (1f - s) / 8f, b), 0.5f, s(d, 1f + (1f - s) / 8f, c)));
  }

  
  // helpers on top of helpers
  public void translate(vec offset) { PointSets.translate(this, offset); }
  public void scale(pt center, float scale) { PointSets.scale(this, center, scale); }
  public void rotate(float angle) { PointSets.rotate(this, angle); }
  public void rotate(float angle, pt center) { PointSets.rotate(this, angle, center); }
  public void registerVerticiesTo(Polygon that) { PointSets.registerTo(this, that); }

  // register the polygon points via edge midpoints onto end
  // weighted by edge length
  public void registerMidpointsTo(Polygon that) {
    pt[] ps = this.getPointsArray(); // FIXME: replace with lists
    pt[] qs = that.getPointsArray();
    
    pt thisCenter = centerE(ps, false);
    pt thatCenter = centerE(qs, false);
    
    translate(V(thisCenter, thatCenter));
    
    float s = 0, c = 0;
    int len = min(ps.length, qs.length) - 1;
    for (int i = 0; i < len; i++) {
      final pt nextP = next(ps, i);
      final pt nextQ = next(qs, i);
      float d = (d(ps[i], nextP) + d(qs[i], nextQ)) / 2;
      
      s += d * dot(V(thisCenter, average(ps[i], nextP)),
                 R(V(thatCenter, average(qs[i], nextQ))));
      c += d * dot(V(thisCenter, average(ps[i], nextP)),
                   V(thatCenter, average(qs[i], nextQ)));
    }
    float a = atan2(s, c);
    rotate(-a, thatCenter);
  }
  
  public void spiralTransform(Polygon start, float t, Polygon end) {
    Polygon RQ = new Polygon(), RP = new Polygon();
    RQ.copyFrom(this);
    RP.copyFrom(this);
    
    RQ.registerMidpointsTo(end);
    RP.registerMidpointsTo(start);
    
    this.copyFrom(RQ);
    spiral(this, RQ, t, RP);
  }

  public Polygon copy() {
    return new Polygon(PointSets.clonePoints(points));
  }

  public InteriorTest interiorTest() {
    return new InteriorTest(toPackedArray(points));
  }
  
  BoundingBox boundingBox() {
    return new BoundingBox(points);
  }
  
  public void clear() {
    points.clear();
  }
  
  public void add(pt p) {
    points.add(p);
  }
  
  public void add(float x, float y) {
    add(new pt(x,y));
  }
  
  public Mesh2D mesh() {
    return mesh(new MeshingSettings());
  }
  
  public TriMesh2D mesh(MeshingSettings settings) {
    List<pt> resampled = PointSets.clonePoints(points);
    if(settings.processBoundary) {
      //resampled = resampleLoopLazy(resampled, settings.minBoundaryEdgeLength, settings.maxBoundaryEdgeLength);
      resampled = resamplePolylineToList(resampled, (int)(arclengthOfLoop(resampled)/settings.minBoundaryEdgeLength));
      //resampled = resample(resampled, 120);
      for(int i = 0; i < settings.numBoundarySmoothingSteps; i++) smoothPolygon(resampled, settings.boundarySmoothingAmount);
    } 
    
    InteriorTest pip = new InteriorTest(toPackedArray(resampled));
    List<pt> interior = sampleInterior(settings.interiorSampleSpacing);
    LloydRelaxation lr = new LloydRelaxation(interior, resampled, pip);
    for(int i = 0; i < settings.numLloydSteps; i++) {
      if(lr.step() == null) {
        return mesh(settings.fewerLloydSteps());
      }
    }
    TriMesh2D mesh = Mesh2D.delaunay(lr.pts, pip);
    return mesh;
  }
  
  public List<pt> removeColocated(List<pt> pts) {
    ArrayList<pt> result = new ArrayList<pt>(pts.size());
    pt prev = pts.get(pts.size()-1);
    for(pt p : pts) {
      if(!colocated(p,prev)) {
        result.add(p);
        prev = p;
      }
    }
    return result;
  }
  
  // rejection sampling
  public List<pt> sampleInterior(float approxRadius) {
    InteriorTest pip = new InteriorTest(toPackedArray(points));
    BoundingBox bb = pip.getBoundingBox();
    
    int numSamples = (int) (area(points)/(Math.PI*sq(approxRadius)));
    List<pt> result = new ArrayList<pt>();
    for(int i = 0; i < numSamples; i++) {
      pt pt;
      do {
        pt = bb.sample();
      } while(!pip.contains(pt.x, pt.y));
      result.add(pt);
    }
    return result;
  }
  
  public pt centroid() {
    return Oplet.centroid(points);
  }
  

  /**
   * AKA: regular polygon
   * @param radius
   * @param numVertices
   * @return
   */
  public static Polygon circle(float radius, int numVertices) {
    return Polygon.circle(radius, numVertices, new pt(0,0));
  }
  
  public static Polygon circle(float radius, int numVertices, pt center) {
    return Polygon.ellipse(radius, radius, numVertices, center);
  }

  public static Polygon ellipse(int radiusX, int radiusY, int numVertices) {
    return Polygon.ellipse(radiusX, radiusY, numVertices, new pt(0,0));
  }

  public static Polygon ellipse(float radiusX, float radiusY, int numVertices, pt center) {
    pt[] points = new pt[numVertices];
    for(int i = 0; i < points.length; i++) {
      float theta = i*TWO_PI/numVertices;
      points[i] = new pt(center.x + radiusX*cos(theta), center.y + radiusY*sin(theta));
    }
    return new Polygon(points);
  }
  
  public vec[] edgeNormals() {
    vec[] result = new vec[points.size()];
    for(int i = 0, pi = points.size()-1; i < points.size(); pi = i, i++) {
      result[i] = normal(points.get(pi),points.get(i));
    }
    return result;
  }

  public boolean contains(pt mouse) {
    return interiorTest().contains(mouse.x, mouse.y);
  }
  
  public boolean overlaps(Polygon that) {
    BoundingBox thisBB = this.boundingBox();
    BoundingBox thatBB = that.boundingBox();
    if(thisBB.intersects(thatBB)) {
      InteriorTest thisIT = this.interiorTest();
      InteriorTest thatIT = that.interiorTest();
      for(pt p : this.points) if(thatIT.contains(p.x, p.y)) return true;
      for(pt p : that.points) if(thisIT.contains(p.x, p.y)) return true;
      
    }
    return false;
  }

  public List<Edge> getEdges() {
    return edges(points);
  }
  public static List<Edge> edges(pt[] pts) {
    return edges(Arrays.asList(pts));
  }
  public static List<Edge> edges(List<pt> pts) {
    List<Edge> result = new ArrayList<Edge>();
    for(int pi = pts.size()-1, i = 0; i < pts.size(); pi = i, i++) {
      result.add(new Edge(pts.get(pi), pts.get(i)));
    }
    return result;
  }

  public static List<Float> angles(List<pt> pts) {
    List<Float> result = new ArrayList<Float>();
    for(Edge e : edges(pts)) {
      result.add(e.dir().angle());
    }
    return result;
  }
  
  @Deprecated // TODO: optimize this, write tests for it 
  public static boolean isConvex(List<pt> pts) {
    int sgn = 0;
    for(Pair<Float,Float> pair : pairs(angles(pts))) {
      float diff = angle_diff(pair.fst, pair.snd);
      if(sgn == 0) sgn = (int) sgn(diff);
      if(sgn != sgn(diff)) return false;
    }
    return true;
  }
  
  public pt project(pt q) { return EdgeSetMethods.project(this, q); }
  public float dist(pt p) { return project(p).dist(p); }

  public pt get(int i) {
    return points.get(i);
  }
  
  public static float arclength(List<pt> pts) {
    if(pts.isEmpty()) return 0;
    float arcLen = 0;
    pt prev = pts.get(pts.size()-1);
    for(int i = 0; i < pts.size(); i++) {
      pt cur = pts.get(i);
      arcLen += cur.dist(prev);
      prev = cur;
    }
    return arcLen;
  }
}
