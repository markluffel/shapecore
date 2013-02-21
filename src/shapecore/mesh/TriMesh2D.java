package shapecore.mesh;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import processing.core.PGraphics;
import processing.core.PImage;
import shapecore.Oplet;
import shapecore.PointSets;
import shapecore.pt;
import shapecore.vec;

public class TriMesh2D extends Mesh2D {

  public CornerTable corner;
  public int[] C;
  
  public TriMesh2D(pt[] geom, CornerTable corner) {
    this.G = geom;
    this.corner = corner;
    corner.numVerts = geom.length; // very trusting, we could scan the V table to make sure
  }

  @Override
  public void subdivide() {
    if(corner != null) G = corner.subdivide(G);    
  }


  // doesn't handle any topology changes,
  // only moves points that are on the border
  // so if there are any points within 'length' of the border
  // they will not be contracted but will cause fold-over
  public void grow(float length) {
    length = -length;
    C = corner.inverseV();
    Map<Integer,vec> borderOffset = new HashMap<Integer,vec>();
    for(int i = 0; i < G.length; i++) {
      vec bo = borderOffset(i);
      if(bo != null) {
        borderOffset.put(i, bo);
      }
    }
    for(Map.Entry<Integer,vec> e : borderOffset.entrySet()) {
      vec dir = e.getValue();
      dir.scaleBy(length);
      G[e.getKey()].add(dir);
    }
  }
  
  public vec borderOffset(int v) {
    int rb = corner.rightwardBorder(C[v]);
    if(rb >= 0) {
      int lb = corner.leftwardBorder(C[v]);
      
      pt a = G[corner.V[corner.p(lb)]];
      pt b = G[v];
      pt c = G[corner.V[corner.n(rb)]];
      
      
      vec l = R(b,a).normalize();
      vec r = R(b,c).normalize(); r.back();
      return S(l,r).normalize();
      /*
      vec L = R(a,b);
      vec R = R(b,c);
      vec result = S(L, R).normalize();
      result.back();
      return result;
      */
    }
    return null; // return null if not on the border
    // maybe can fix our bugginess with near-but-not-on-edge points here?
    // probably not the simplest thing to do though
  }
  
  // TODO: needs to consider concave shapes with hidden parts
  // eventually I'll just have to remove hidden stuff
  public void smoothInterior(float t, boolean[] mask) {
    if(C == null || C.length != corner.numVerts) C = corner.inverseV();
    int[] v = corner.V;
    pt[] L = new pt[G.length];

    for(int i = 0; i < G.length; i++) {
      if(!isBorder(i, mask)) {
        pt l = new pt();
        int count = 0;
        for(int c : corner.vertexCorners(C[i])) {
          pt p = G[v[corner.p(c)]];
          l.add(p.x,p.y);
          count++;
        }
        l.scaleBy(1f/count);
        L[i] = l;
      }
    }
    for(int i = 0; i < G.length; i++) {
      if(L[i] != null) {
        G[i].translateTowardsByRatio(t, L[i]);
      }
    }
  }
  
  public void smoothField(float[] F, float t) {
    if(C == null || C.length != corner.numVerts) C = corner.inverseV();
    int[] v = corner.V;
    float[] F_ = new float[F.length];
    assert(G.length == F.length);
    for(int i = 0; i < G.length; i++) {
      float sum = 0;
      int count = 0;
      for(int c : corner.vertexCorners(C[i])) {
        int otherV = v[corner.p(c)];
        sum += F[otherV]; // TODO: cotan
        count++;
      }
      sum /= count;
      F_[i] = sum;
    }
    for(int i = 0; i < G.length; i++) {
      F[i] += t * F_[i];
    }    
  }
  
  public float cotan(int c) {
    return cotAlpha(G[corner.V[p(c)]], G[corner.V[c]], G[corner.V[n(c)]]);
  }
  
  // the cotangent weight for the edge associated with this corner
  public float cotanSum(int c) {
    if(corner.O[c] < 0) {
      return cotan(c); // x2 ??
    } else {
      return cotan(c)+cotan(corner.O[c]);
    }
  }
  
  
  public boolean isBorder(int v) {
    return corner.isVertexOnBorder(C[v]);
  }
  
  public boolean isBorder(int v, boolean[] mask) {
    return corner.isVertexOnBorder(C[v], mask);
  }
  
  public void draw(Oplet p) {
    corner.draw(p, G);
  }
  
  public void draw(PGraphics p) {
    corner.draw(p, G);
  }
  
  public void drawEdges(Oplet p) {
    corner.drawEdges(p, G);
  }
  
  public void drawBorder(Oplet p) {
    drawBorder(p,G,null);
  }
  
  public void drawBorder(Oplet p, pt[] geom, boolean[] mask) {
    if(C == null) C = corner.inverseV();
    
    int v = 0;
    while(!isBorder(v) && v < numPoints()) v++;
    
    if(v < numPoints()) {
      pt prev = geom[v];
      int c = corner.n(corner.rightwardBorderMasked(C[v],mask));
      int firstC = c;
      int iterations = 0;
      do {
        pt cur = geom[corner.V[c]];
        p.line(prev, cur);
        prev = cur;
        
        c = corner.n(corner.rightwardBorderMasked(c, mask));
        
        iterations++;
      } while(c != firstC && iterations < 3000);
    }
  }
  
  public List<Integer> getBorder(boolean[] mask) {
    if(C == null) C = corner.inverseV();
    
    List<Integer> result = new ArrayList<Integer>();
    
    int v = 0;
    while(!isBorder(v) && v < numPoints()) v++;
    // assumes one connecte component, otherwise we'd need another loop wrapping the stuff below
    
    if(v < numPoints()) {
      
      int c = corner.n(corner.rightwardBorderMasked(C[v],mask));
      int firstC = c;
      int iterations = 0;
      do {
        result.add(corner.V[c]);
        
        c = corner.n(corner.rightwardBorderMasked(c, mask));
        
        iterations++;
      } while(c != firstC && iterations < 3000);
    }
    return result;
  }

  @Override
  public List<Integer> getBorder() {
    corner.numVerts = G.length;
    if(C == null) C = corner.inverseV();
    return corner.getBorder(C);
  }
  
  @Override
  public TriMesh2D clone() {
    return new TriMesh2D(PointSets.clonePoints(G), corner.get());
  }
  
  @Override
  public int numFaces() {
    return corner.O.length/3;
  }
  
  @Override
  public void drawUsingAlternateGeometry(Oplet p, pt[] geom, boolean[] mask) {
    corner.draw(p, geom, mask);
  }

  @Override
  public void drawUsingAlternateGeometry(Oplet p, pt[] geom, PImage texture, pt[] uv, boolean[] mask) {
    corner.drawTextured(p, geom, texture, uv, mask);
  }
  
  @Override
  public void drawUsingAlternateGeometry(PGraphics p, pt[] geom, PImage texture, pt[] uv, boolean[] mask) {
    corner.drawTextured(p, geom, texture, uv, mask);
  }
  
  /**
   * Use the geometry as the UV coords for texturing
   */
  @Override
  public void drawTextured(Oplet p, PImage texture) {
    corner.drawTextured(p, G, texture, G);
  }

  @Override
  public void drawColored(Oplet p, int[] colors) {
    corner.drawColored(p, G, colors);
  }
  
  /**
   * Iterate over the one-ring vertex neighbors of a vertex, given the vertex inde
   * 
   * Doesn't handle verticies on boundaries correctly
   */
  public Iterable<Integer> oneRing(int vertex) {
    return corner.oneRing(cornerForVertex(vertex));
  }
  
  public Iterable<Integer> vertexCorners(int vertex) {
    return corner.vertexCorners(cornerForVertex(vertex));
  }
  
  public int cornerForVertex(int v) {
    if(C == null) {
      C = corner.inverseV();
    }
    return C[v];
  }

  public void drawCorner(Oplet p, int c) {
    drawCorner(p, c, 8, 5);
  }

  public void drawCorner(Oplet p, int c, float dist) {
    drawCorner(p, c, dist, 5);
  }

  public void drawCorner(Oplet p, int c, float dist, float radius) {
    int[] V = corner.V;
    pt here = G[V[c]];
    vec a = V(here, G[V[n(c)]]);
    vec b = V(here, G[V[p(c)]]);
    float aa = angle(a);
    float ba = angle(b);
    vec dir = fromRadial(dist, angleAverage(aa,ba));
    p.circle(T(here, dir), radius);
  }
  
  // untested, is this the right thing?
  static float angleAverage(float a, float b) {
    float direct = abs(a-b);
    float spinA = abs((a+TWO_PI)-b);
    float spinB = abs(a-(b+TWO_PI));
    if(direct < spinA && direct < spinB) {
      return (a+b)/2;
    } else {
      return ((a+TWO_PI)+b)/2;
    }
  }
}
 