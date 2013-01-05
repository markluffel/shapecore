/**
 * 
 */
package shapecore;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import processing.core.PConstants;
import shapecore.fancy.LloydRelaxation;
import shapecore.interfaces.PointSet;
import shapecore.mesh.Mesh2D;
import shapecore.mesh.MeshingSettings;
import shapecore.mesh.TriMesh2D;

import megamu.mesh.InteriorTest;

public class Polygon implements PointSet {
  pt[] points;
  
  public Polygon() {
    this.points = new pt[0];
  }
  
  public Polygon(pt[] points) {
    if(points == null) throw new IllegalArgumentException();
    for(pt p : points) if(p == null) throw new IllegalArgumentException();
    this.points = points;
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
    return Arrays.asList(points);
  }
  
  public pt[] getPointsArray() {
    return points;
  }
  
  public int numPoints() {
    return points.length;
  }

  public void copyFrom(Polygon that) {
    this.points = new pt[that.points.length];
    for (int i = 0; i < this.points.length; i++) {
      this.points[i] = new pt(that.points[i]);
    }
  }
  
  public void refine(float s) {
    pt[] Q = refine(this.points, s);
    this.points = Q;
  }

  public static pt[] refine(pt[] P, float s) {
    int n = P.length;
    pt[] Q = new pt[2 * n];
    for (int i = 0; i < n; i++) {
      Q[2 * i] = b(next(P, i), P[i], next(P, i), s);
      Q[2 * i + 1] = f(prev(P, i), P[i], next(P, i), next(P, nextI(P, i)), s);
    }
    return Q;
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
    pt[] ps = this.points;
    pt[] qs = that.points;
    
    pt thisCenter = centerE(ps, false);
    pt thatCenter = centerE(qs, false);
    
    translate(V(thisCenter, thatCenter));
    
    float s = 0, c = 0;
    int len = min(ps.length, qs.length) - 1;
    for (int i = 0; i < len; i++) {
      final pt nextP = next(ps, i);
      final pt nextQ = next(qs, i);
      float d = (d(ps[i], nextP) + d(qs[i], nextQ)) / 2;
      
      s += d * dot(V(thisCenter, A(ps[i], nextP)),
                 R(V(thatCenter, A(qs[i], nextQ))));
      c += d * dot(V(thisCenter, A(ps[i], nextP)),
                   V(thatCenter, A(qs[i], nextQ)));
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
    Polygon result = new Polygon();
    result.points = clonePoints(this.points);
    return result;
  }

  public InteriorTest interiorTest() {
    return new InteriorTest(toPackedArray(points));
  }
  
  BoundingBox boundingBox() {
    return new BoundingBox(points);
  }
  
  public void clear() {
    points = new pt[0];
  }
  
  public void add(float x, float y) {
    pt[] newPoints = new pt[points.length+1];
    System.arraycopy(points, 0, newPoints, 0, points.length);
    newPoints[points.length] = new pt(x,y);
    points = newPoints;
  }
  
  public Mesh2D mesh() {
    return mesh(new MeshingSettings());
  }
  
  public TriMesh2D mesh(MeshingSettings settings) {
    List<pt> resampled = Oplet.clonePoints(Arrays.asList(points));
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
    
    int numSamples = (int) (area(Arrays.asList(points))/(Math.PI*sq(approxRadius)));
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
    vec[] result = new vec[points.length];
    for(int i = 0, pi = points.length-1; i < points.length; pi = i, i++) {
      result[i] = normal(points[pi],points[i]);
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
}
