package shapecore.mesh;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import megamu.mesh.Delaunay;
import megamu.mesh.InteriorTest;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import quickhull3d.Face;
import quickhull3d.HalfEdge;
import quickhull3d.QuickHull3D;
import quickhull3d.Vertex;
import shapecore.Geometry;
import shapecore.Oplet;
import shapecore.pt;
import shapecore.pt3;
import shapecore.vec;
import shapecore.vec3;
import shapecore.iterators.PairIterator;
import shapecore.tuple.Pair;

/**
 * 
 * CornerTable stores the connectivity of a triangle mesh.
 * 
 * Usually used with a datastructure that records vertex and triangle properties too.
 * Given a corner, the associated vertex index can be found with the v(cornerId) method,
 * and the associated triangle index can be found with the t(cornerId) method.
 * 
 * Arrays of triangle properties and vertex properties can be indexed into using these ids.
 *
 */
public class CornerTable {

  public class Corner {
    int c;
    Corner(int c) { this.c = c; };
    
    public Corner n() { return new Corner(CornerTable.this.n(c)); }
    public Corner p() { return new Corner(CornerTable.this.p(c)); }
    public Corner o() { return new Corner(CornerTable.this.o(c)); }
    public Corner l() { return new Corner(CornerTable.this.l(c)); }
    public Corner r() { return new Corner(CornerTable.this.r(c)); }
    public Corner s() { return new Corner(CornerTable.this.s(c)); }
    
    public Corner n_() { c = CornerTable.this.n(c); return this; }
    public Corner p_() { c = CornerTable.this.p(c); return this; }
    public Corner o_() { c = CornerTable.this.o(c); return this; }
    public Corner l_() { c = CornerTable.this.l(c); return this; }
    public Corner r_() { c = CornerTable.this.r(c); return this; }
    public Corner s_() { c = CornerTable.this.s(c); return this; }
    
    public boolean b() { return CornerTable.this.b(c); }
    
    /*
    Vertex v() { return new Vertex(CornerTable.this.v(c)); }
    Triangle t() { return new Triangle(CornerTable.this.t(c)); }
    */
    public int v() { return CornerTable.this.v(c); }
    public int t() { return CornerTable.this.t(c); }
    
    public boolean eq(Corner cor) {
      return c == cor.c;
    }
    
    public Corner clone() { return new Corner(c); }
    public Corner d() { return new Corner(c); }

    public void setO(int newO) { CornerTable.this.O[c] = newO; }
    public void setV(int newV) { CornerTable.this.V[c] = newV; }
  }
  
  public Corner corner(int c) {
    return new Corner(c);
  }
  
  public Corner triCorner(int t) {
    return new Corner(t*3);
  }
  
  public int[] V;
  public int[] O;
  public int numVerts;
  
  public CornerTable() {}
  
  public CornerTable(int numTriangles) {
    V = new int[numTriangles*3];
    O = new int[numTriangles*3];
    numVerts = -1;
  }
  
  
  public CornerTable(int[][] triangleSoup) {
    V = new int[triangleSoup.length*3];
    O = new int[triangleSoup.length*3];
    int maxV = 0;
    for(int i = 0; i < triangleSoup.length; i++) {
      V[i*3+0] = triangleSoup[i][0];
      V[i*3+1] = triangleSoup[i][1];
      V[i*3+2] = triangleSoup[i][2];
      
      if(triangleSoup[i][0] > maxV) maxV = triangleSoup[i][0];
      if(triangleSoup[i][1] > maxV) maxV = triangleSoup[i][1];
      if(triangleSoup[i][2] > maxV) maxV = triangleSoup[i][2];
    }
    numVerts = maxV+1;
    computeO(numVerts); // wants the max+1 (the "length", though non-existent vertex ids are ok i think)
  }
  
  public CornerTable(List<int[]> triangleSoup) {
    V = new int[triangleSoup.size()*3];
    O = new int[triangleSoup.size()*3];
    int maxV = 0;
    for(int i = 0; i < triangleSoup.size(); i++) {
      int[] f = triangleSoup.get(i);
      V[i*3+0] = f[0];
      V[i*3+1] = f[1];
      V[i*3+2] = f[2];
      
      if(f[0] > maxV) maxV = f[0];
      if(f[1] > maxV) maxV = f[1];
      if(f[2] > maxV) maxV = f[2];
    }
    numVerts = maxV+1;
    computeO(maxV+1); // wants the max+1 (the "length", though non-existent vertex ids are ok i think)
  }
  
  
  public final int t(int c) {return (int)(c/3);}       // triangle of corner
  public final int n(int c) {if(c%3 == 2) return c-2; else return c+1;}   // next corner in the same t(c)
  public final int p(int c) {if(c%3 == 0) return c+2; else return c-1;}   // prev corner in the same t(c)
  public int v(int c) {return V[c];}             // id of the vertex of c
  public boolean b(int c) {return O[c]<0;}     // if faces a border (has no opposite)
  public int o(int c) {if (b(c)) return c; else return O[c];} // opposite (or self if it has no opposite)
  public final int l(int c) {return o(n(c));} // left neighbor (or next if n(c) has no opposite)
  public final int r(int c) {return o(p(c));} // right neighbor (or previous if p(c) has no opposite)
  public final int s(int c) {return n(l(c));} // swings around v(c) or around a border loop
  public final int _s(int c) {return p(r(c));} // unswings around v(c) or around a border loop
  public final int non(int c) { return n(o(n(c))); } // same as s
  public final int pop(int c) { return p(o(p(c))); } // same as _s
  
  public int vSpan() {
    int max = 0;
    for(int c = 0; c < V.length; c += 3) {
      max = max(max(max, abs(V[c]-V[c+1])), max(abs(V[c+1]-V[c+2]), abs(V[c+2]-V[c]))); 
    }
    return max;
  }
  
  public int tSpan() {
    // for clarity
    int max = 0;
    for(int v = 0; v < numVerts; v++) {
      int first = -1, last = -1;
      for(int c = 0; c < V.length; c += 3) {
        if(v == V[c]) {
          int t = c/3;
          last = t;
          if(first == -1) first = t;
        }
      }
      if(last != -1) max = max(max, last-first);
    }
    return max;
    //return new TSpan().calc(numVerts, V); // probably more efficient
  }
  
  static class TSpan {
    int[] first,last;
    int calc(int numVerts, int[] V) {
      first = new int[numVerts];
      last = new int[numVerts];
      Arrays.fill(first, -1);
      Arrays.fill(last, -1);
      for(int c = 0; c < V.length; c += 3) {
        int t = c/3;
        add(V[c+0], t);
        add(V[c+1], t);
        add(V[c+2], t);
      }
      int max = 0;
      for(int v = 0; v < numVerts; v++) {
        if(last[v] != -1) max = last[v]-first[v];
      }
      return max;
    }
    void add(int v, int t) {
      if(first[v] == -1) first[v] = t;
      last[v] = t;
    }
  }
  
  CornerTable flip() {
    CornerTable result = get();
    for(int t = 0; t < result.numFaces(); t++) {
      int c = t*3;
      result.V[c+1] = V[c+2];
      result.V[c+2] = V[c+1];
      result.O[c] = flipCorner(O[c]);
      result.O[c+1] = flipCorner(O[c+2]);
      result.O[c+2] = flipCorner(O[c+1]);
    }
    return result;
  }
  
  static int flipCorner(int c) {
    int cn = c%3;
    if(cn == 1) return c+1;
    else if(cn == 2) return c-1;
    else return c;
  }
  
  CornerTable merge(CornerTable that) {
    int cornerOffset = this.O.length;
    int vertexOffset = this.numVerts;
    
    CornerTable result = new CornerTable(this.numFaces()+that.numFaces());
    System.arraycopy(this.O, 0, result.O, 0, this.O.length);
    System.arraycopy(this.V, 0, result.V, 0, this.V.length);
    for(int i = 0, j = cornerOffset; i < that.O.length; i++) {
      result.O[j] = that.O[i]+cornerOffset;
      result.V[j] = that.V[i]+vertexOffset;
    }
    return result;
  }
  
  public void draw(PGraphics p, pt[] geom) {
    if(geom.length < numVerts) {
      throw new IllegalArgumentException("Not enough geometry given for this connectivity");
    }
    p.beginShape(PConstants.TRIANGLES);
    for(int i = 0; i < O.length; i += 3) {
      p.vertex(geom[V[i]].x, geom[V[i]].y);
      p.vertex(geom[V[i+1]].x, geom[V[i+1]].y);
      p.vertex(geom[V[i+2]].x, geom[V[i+2]].y);
    }
    p.endShape();
  }
  
  public void draw(Oplet p, pt[] geom) {
    if(geom.length < numVerts) {
      throw new IllegalArgumentException("Not enough geometry given for this connectivity");
    }
    p.beginShape(PConstants.TRIANGLES);
    for(int i = 0; i < O.length; i += 3) {
      p.vertex(geom[V[i]]);
      p.vertex(geom[V[i+1]]);
      p.vertex(geom[V[i+2]]);
    }
    p.endShape();
  }
  
  public void drawColored(Oplet p, pt[] geom, int[] colors) {
    if(geom.length < numVerts) {
      throw new IllegalArgumentException("Not enough geometry given for this connectivity");
    }
    p.beginShape(PConstants.TRIANGLES);
    for(int i = 0; i < O.length; i += 3) {
      p.fill(colors[V[i]]);   p.vertex(geom[V[i]]);
      p.fill(colors[V[i+1]]); p.vertex(geom[V[i+1]]);
      p.fill(colors[V[i+2]]); p.vertex(geom[V[i+2]]);
    }
    p.endShape();
  }
  
  public void draw(Oplet p, pt[] geom, boolean[] mask) {
    if(geom.length < numVerts) {
      throw new IllegalArgumentException("Not enough geometry given for this connectivity");
    }
    if(mask.length*3 != O.length) {
      throw new IllegalArgumentException("Mask length does not equal number of triangles");
    }
    p.beginShape(PConstants.TRIANGLES);
    int j = 0;
    for(int i = 0; i < O.length; i += 3) {
      if(mask[j]) {
        p.vertex(geom[V[i]]);
        p.vertex(geom[V[i+1]]);
        p.vertex(geom[V[i+2]]);
      }
      j++;
    }
    p.endShape();
  }
  
  
  public void draw(Oplet p, pt3[] geom) {
    if(geom.length < numVerts) {
      throw new IllegalArgumentException("Not enough geometry given for this connectivity");
    }
    p.beginShape(PConstants.TRIANGLES);
    for(int i = 0; i < O.length; i += 3) {
      p.vertex(geom[V[i]]);
      p.vertex(geom[V[i+1]]);
      p.vertex(geom[V[i+2]]);
    }
    p.endShape();
  }
  
  /**
   * 
   * @param p the applet to render onto
   * @param geom the points to render with
   */
  public void drawEdges(Oplet p, pt[] geom) {
    p.beginShape(LINES);
    for(int i = 0; i < O.length; i++) {
      if(i > O[i]) {
        p.vertex(geom[V[n(i)]]); p.vertex(geom[V[p(i)]]);
      }
    }
    p.endShape();
  }
  /**
   * 
   * @param p the applet to render onto
   * @param geom the points to render with
   * @param colors colors for each halfedge, only the greater indexed one will be used
   */
  public void drawEdges(Oplet p, pt[] geom, int[] colors) {
    p.beginShape(LINES);
    for(int i = 0; i < O.length; i += 3) {
      if(i > O[i]) {
        p.stroke(colors[i+2]);
        p.vertex(geom[V[i+0]]);
        p.vertex(geom[V[i+1]]);
        
        p.stroke(colors[i+0]);
        p.vertex(geom[V[i+1]]);
        p.vertex(geom[V[i+2]]);
        
        p.stroke(colors[i+1]);
        p.vertex(geom[V[i+2]]);
        p.vertex(geom[V[i+0]]);
      }
    }
    p.endShape();
  }
  
  public void drawAsymmetricEdges(Oplet p, pt[] geom, int[] colors) {
    p.beginShape(LINES);
    for(int i = 0; i < O.length; i += 3) {
      
      
      pt
      a = geom[V[i+0]], b = geom[V[i+1]], c = geom[V[i+2]],
      ab = average(a,b), bc = average(b,c), ca = average(c,a);
      
      if(!b(i+2)) {
        p.stroke(colors[i+2]);
        p.vertex(ab); p.vertex(b);
      }
      
      if(!b(i+0)) {
        p.stroke(colors[i+0]);
        p.vertex(bc); p.vertex(c);
      }
      
      if(!b(i+1)) {
        p.stroke(colors[i+1]);
        p.vertex(ca); p.vertex(a);
      }
    }
    p.endShape();
  }
  
  public void draw(Oplet p, pt3[] geom, int[] colors) {
    if(geom.length < numVerts) {
      throw new IllegalArgumentException("Not enough geometry given for this connectivity");
    }
    if(colors.length < numVerts) {
      throw new IllegalArgumentException("Not enough colors for the connectivity info");
    }
    p.beginShape(PConstants.TRIANGLES);
    for(int i = 0; i < O.length; i += 3) {
      p.fill(colors[V[i+0]]); p.vertex(geom[V[i+0]]);
      p.fill(colors[V[i+1]]); p.vertex(geom[V[i+1]]);
      p.fill(colors[V[i+2]]); p.vertex(geom[V[i+2]]);
    }
    p.endShape();
  }
  
  public void draw(Oplet p, pt3[] geom, int[] colors, boolean[] mask) {
    if(geom.length < numVerts) {
      throw new IllegalArgumentException("Not enough geometry given for this connectivity");
    }
    if(colors.length < numVerts) {
      throw new IllegalArgumentException("Not enough colors for the connectivity info");
    }
    if(mask.length*3 < V.length) {
      throw new IllegalArgumentException("Not enough mask values");
    }
    p.beginShape(PConstants.TRIANGLES);
    for(int i = 0, j = 0; i < V.length; j++, i += 3) {
      if(mask[j]) {
        p.fill(colors[V[i+0]]); p.vertex(geom[V[i+0]]);
        p.fill(colors[V[i+1]]); p.vertex(geom[V[i+1]]);
        p.fill(colors[V[i+2]]); p.vertex(geom[V[i+2]]);
      }
    }
    p.endShape();
  }
  
  public void drawTextured(Oplet p, pt[] geom, PImage texture, pt[] uv, boolean[] mask) {
    if(mask == null) {
      drawTextured(p, geom, texture, uv);
    } else {
      p.fill(0);
      p.beginShape(PConstants.TRIANGLES);
      p.texture(texture);
      int j = 0;
      for(int i = 0; i < O.length; i += 3) {
        if(mask[j]) {
          p.vertex(geom[V[i]], uv[V[i]]);
          p.vertex(geom[V[i+1]], uv[V[i+1]]);
          p.vertex(geom[V[i+2]], uv[V[i+2]]);
        }
        j++;
      }
      p.endShape();
    }
  }
  public void drawTextured(Oplet p, pt[] geom, PImage texture, pt[] uv) {
    p.fill(0);
    p.beginShape(PConstants.TRIANGLES);
    p.texture(texture);
    for(int i = 0; i < O.length; i += 3) {      
      p.vertex(geom[V[i]], uv[V[i]]);
      p.vertex(geom[V[i+1]], uv[V[i+1]]);
      p.vertex(geom[V[i+2]], uv[V[i+2]]);
    }
    p.endShape();
  }
  
  public void drawTextured(PGraphics p, pt[] geom, PImage texture, pt[] uv, boolean[] mask) {
    p.fill(0);
    p.beginShape(PConstants.TRIANGLES);
    p.texture(texture);
    for(int i = 0; i < O.length; i += 3) {
      pt loc, tex;
      loc = geom[V[i  ]]; tex = uv[V[i  ]]; p.vertex(loc.x, loc.y, tex.x, tex.y);
      loc = geom[V[i+1]]; tex = uv[V[i+1]]; p.vertex(loc.x, loc.y, tex.x, tex.y);
      loc = geom[V[i+2]]; tex = uv[V[i+2]]; p.vertex(loc.x, loc.y, tex.x, tex.y);
    }
    p.endShape();
  }

  
  public void draw(Oplet p, float[][] geom) {
    p.beginShape(PConstants.TRIANGLES);
    for(int i = 0; i < O.length; i += 3) {
      float[] a = geom[V[i]], b = geom[V[i+1]], c = geom[V[i+2]];
      p.vertex(a[0], a[1]);
      p.vertex(b[0], b[1]);
      p.vertex(c[0], c[1]);
    }
    p.endShape();
  }
  
  public void drawInset(Oplet p, pt[] geom, float distance) {
    p.beginShape(PConstants.TRIANGLES);
    for(int i = 0; i < O.length; i+=3) {
      pt a = geom[V[i]], b = geom[V[i+1]], c = geom[V[i+2]];
      pt center = average(a,b,c);
      
      p.vertex(a.get().translateTowardsBy(distance, center));
      p.vertex(b.get().translateTowardsBy(distance, center));
      p.vertex(c.get().translateTowardsBy(distance, center));
    }
    p.endShape();
  }
  
  public void drawWithTriangleColors(Oplet p, pt3[] geom, int[] triColors, boolean[] mask) {
    if(geom.length < numVerts) {
      throw new IllegalArgumentException("Not enough geometry given for this connectivity");
    }
    if(triColors.length*3 < V.length) {
      throw new IllegalArgumentException("Not enough colors for the connectivity info");
    }
    if(mask.length*3 < V.length) {
      throw new IllegalArgumentException("Not enough mask values");
    }
    p.beginShape(PConstants.TRIANGLES);
    for(int i = 0, j = 0; i < V.length; j++, i += 3) {
      if(mask[j]) {
        p.fill(triColors[t(i+0)]); p.vertex(geom[V[i+0]]);
        p.fill(triColors[t(i+1)]); p.vertex(geom[V[i+1]]);
        p.fill(triColors[t(i+2)]); p.vertex(geom[V[i+2]]);
      }
    }
    p.endShape();
  }
  
  public void drawWithNormals(Oplet p, pt3[] geom, vec3[] N) {
    if(geom.length < numVerts) {
      throw new IllegalArgumentException("Not enough geometry given for this connectivity");
    }
    if(N.length != geom.length) {
      throw new IllegalArgumentException("Length of normals and geometry does not match");
    }
    p.beginShape(PConstants.TRIANGLES);
    for(int i = 0; i < V.length; i += 3) {
      int v1 = V[i+0], v2 = V[i+1], v3 = V[i+2];
      p.normal(N[v1]); p.vertex(geom[v1]);
      p.normal(N[v2]); p.vertex(geom[v2]);
      p.normal(N[v3]); p.vertex(geom[v3]);
    }
    p.endShape();
  }
  
  public void drawWithTriangleColors(Oplet p, pt3[] geom, vec3[] N, int[] triColors, boolean[] mask) {
    if(geom.length < numVerts) {
      throw new IllegalArgumentException("Not enough geometry given for this connectivity");
    }
    if(N.length != geom.length) {
      throw new IllegalArgumentException("Length of normals and geometry does not match");
    }
    if(triColors.length*3 < V.length) {
      throw new IllegalArgumentException("Not enough colors for the connectivity info");
    }
    if(mask.length*3 < V.length) {
      throw new IllegalArgumentException("Not enough mask values");
    }
    p.beginShape(PConstants.TRIANGLES);
    for(int i = 0, j = 0; i < V.length; j++, i += 3) {
      if(mask[j]) {
        int v1 = V[i], v2 = V[i+1], v3 = V[i+2];
        int t = t(i);
        p.fill(triColors[t]); p.normal(N[v1]); p.vertex(geom[v1]);
        p.fill(triColors[t]); p.normal(N[v2]); p.vertex(geom[v2]);
        p.fill(triColors[t]); p.normal(N[v3]); p.vertex(geom[v3]);
      }
    }
    p.endShape();
  }
  
  public void drawButterfly(Oplet p, pt[] geom, int depth) {
    CornerTable that = get();
    while(depth > 0) {
      geom = that.butterfly(geom);
      depth--;
    }
    that.draw(p, geom);
  }
  
  public pt[] subdivide(pt[] g) {
    // a mapping from old
    int[] W = new int[O.length];
    
    g = splitEdges(W, g);
    splitTriangles(W);
    computeO(g.length);
    
    return g;
  }
  
  public pt3[] subdivide(pt3[] g) {
    // a mapping from old
    int[] W = new int[O.length];
    
    g = splitEdges(W, g);
    splitTriangles(W);
    computeO(g.length);
    
    return g;
  }
  
  public pt[] butterfly(pt[] g) {
    // a mapping from old
    int[] W = new int[O.length];
    
    g = splitEdges(W, g);
    butterflyBulge(W, g);
    splitTriangles(W);
    computeO(g.length);
    
    return g;
  }
  
  public pt3[] butterfly(pt3[] g) {
    // a mapping from old
    int[] W = new int[O.length];
    
    g = splitEdges(W, g);
    butterflyBulge(W, g);
    splitTriangles(W);
    computeO(g.length);
    
    return g;
  }

  
  /**
   * Subdivide the connectivity info, but don't manipulate geometry,
   * instead, record the indicies of verticies that would contribute to the butterfly mask for a given point.
   */
  // 
  public int[][] butterfly(int numOldVerts) {
    int[] W = new int[O.length];
    
    int numNewVerts = computeW(W, numOldVerts);
    
    int[][] cachedMasks = butterflyBulgeMask(W, numOldVerts, numNewVerts);
    splitTriangles(W);
    computeO(numNewVerts);
    
    return cachedMasks;
  }
  
  public pt[] modifiedButterfly(pt[] g) {
    // a mapping from old
    int[] W = new int[O.length];
    
    g = splitEdges(W, g);
    modifiedButterflyBulge(W, g);
    splitTriangles(W);
    computeO(g.length);
    
    return g;
  }
  
  public pt3[] modifiedButterfly(pt3[] g) {
    // a mapping from old
    int[] W = new int[O.length];
    
    g = splitEdges(W, g);
    modifiedButterflyBulge(W, g);
    splitTriangles(W);
    computeO(g.length);
    
    return g;
  }
  
  public int[] inverseV() {
    int[] inverseV = new int[numVerts];
    Arrays.fill(inverseV, -1); // for any unreferences vertices
    for(int c = 0; c < V.length; c++) {
      inverseV[V[c]] = c; // the last corner becomes the canonical one
    }
    return inverseV;
    /*
    // this seems like a speedier version, should write some tests
    C = new int[pts.length];
    for(int i = 0; i < C.length; i++) {
      C[i] = -1;
    }
    int numRemaining = pts.length;
    for(int i = 0; i < table.V.length && numRemaining > 0; i++) {
      if(C[table.V[i]] == -1) {
        C[table.V[i]] = i;
        numRemaining--;
      }
    }
     */
  }
  
  private int computeW(int[] W, int nv) {
    // record the index of the vertex created by splitting every halfedge
    for(int i = 0; i < O.length; i++) {  // for each corner i
      // if this halfedge is a border, just split it
      if(b(i)) {
        W[i]=nv++;
      
      } else if(i < o(i)) {
        // if this halfedge isn't a border split both it and its opposite
        // only split once per halfedge pair (the i < o(i)) test ensures this
        W[o(i)] = W[i] = nv;
        nv++;
      }
    }
    return nv;
  }
  
  private pt[] splitEdges(int[] W, pt[] geom) {

    int numOldVerts = geom.length;
    int numNewVerts = computeW(W, geom.length);    
    
    // expand the number of verticies
    pt[] newGeom = new pt[numNewVerts];
    System.arraycopy(geom, 0, newGeom, 0, geom.length);
    geom = newGeom;
    
    int j = numOldVerts;
    for(int i = 0; i < O.length; i++) {
      if(b(i)) {
        geom[j] = average(geom[V[n(i)]],geom[V[p(i)]]);
        j++;
      } else if(i < o(i)) { // only do once per edge
        geom[j] = average(geom[V[n(i)]],geom[V[p(i)]]);
        j++;
      }
    }
    numVerts = geom.length;
    
    return geom;
  }

  
  private pt3[] splitEdges(int[] W, pt3[] geom) {

    int numOldVerts = geom.length;
    numVerts = computeW(W, geom.length);    
    
    // expand the number of verticies
    pt3[] newGeom = new pt3[numVerts];
    System.arraycopy(geom, 0, newGeom, 0, geom.length);
    geom = newGeom;
    
    int j = numOldVerts;
    for(int i = 0; i < O.length; i++) {
      if(b(i)) {
        geom[j] = average(geom[V[n(i)]],geom[V[p(i)]]);
        j++;
      } else if(i < o(i)) { // only do once per edge
        geom[j] = average(geom[V[n(i)]],geom[V[p(i)]]);
        j++;
      }
    }
    numVerts = geom.length;
    
    return geom;
  }
  /**
   * Uses the temporary W mapping to construct the butterfly mask for new points
   * Mutates the G array
   */
  private void butterflyBulge(int[] W, pt[] g) {
    int nt = O.length/3;
    // tweaks the new mid-edge vertices according to the Butterfly mask
    for(int i = 0; i < 3*nt; i++) {
      // i < o(i) means: only tweak one of the half edges, arbitrarily the lower ordered one
      if(!b(i) && i < o(i)) {    // no tweak for mid-vertices of border edges
        if (!b(p(i))&&!b(n(i))&&!b(p(o(i)))&&!b(n(o(i)))) { // check that
          g[V[W[i]]].add(0.25f,average(average(g[V[l(i)]],g[V[r(i)]]),average(g[V[l(o(i))]],g[V[r(o(i))]])).to(average(g[V[i]],g[V[o(i)]])));
        }
      }
    }
  }
  
  private int[][] butterflyBulgeMask(int[] W, int numOldVerts, int numNewVerts) {
    int[][] masks = new int[numNewVerts-numOldVerts][8]; // only record the new stuff
    
    // tweaks the new mid-edge vertices according to the Butterfly mask
    for(int i = 0; i < O.length; i++) {
      // i < o(i) means: only tweak one of the half edges, arbitrarily the lower ordered one
      if(i < o(i) || b(i)) {    // no tweak for mid-vertices of border edges
        int[] mask = masks[W[i]-numOldVerts];
        // body axis verts
        mask[0] = V[n(i)];
        mask[1] = V[p(i)];
        if (!b(i) && !b(p(i)) && !b(n(i)) && !b(p(o(i))) && !b(n(o(i)))) { // check that we're not near a border
          // wing center verts
          mask[2] = V[i];
          mask[3] = V[o(i)];
          // wing tip verts
          mask[4] = V[l(i)];
          mask[5] = V[r(i)];
          mask[6] = V[l(o(i))];
          mask[7] = V[r(o(i))];
        } else {
          // ignore if we're on a boundary
          mask[2] = mask[3] = mask[4] = mask[5] = mask[6] = mask[7] = -1;
        }
      }
    }
    return masks;
  }
  
  private void butterflyBulge(int[] W, pt3[] g) {
    int nt = O.length/3;
    // tweaks the new mid-edge vertices according to the Butterfly mask
    for(int i = 0; i < 3*nt; i++) {
      // i < o(i) means: only tweak one of the half edges, arbitrarily the lower ordered one
      if(!b(i) && i < o(i)) {    // no tweak for mid-vertices of border edges
        if (!b(p(i))&&!b(n(i))&&!b(p(o(i)))&&!b(n(o(i)))) { // check that
          g[V[W[i]]].add(0.25f,average(average(g[V[l(i)]],g[V[r(i)]]),average(g[V[l(o(i))]],g[V[r(o(i))]])).to(average(g[V[i]],g[V[o(i)]])));
        }
      }
    }
  }

  private void modifiedButterflyBulge(int[] W, pt[] g) {
    int nt = O.length/3;
    // tweaks the new mid-edge vertices according to the Butterfly mask
    for(int i = 0; i < 3*nt; i++) {
      // i < o(i) means: only tweak one of the half edges, arbitrarily the lower ordered one
      if(!b(i) && i < o(i)) {    // no tweak for mid-vertices of border edges
        if (!b(p(i))&&!b(n(i))&&!b(p(o(i)))&&!b(n(o(i)))) { // check that
          g[V[W[i]]].add(0.25f,average(average(g[V[l(i)]],g[V[r(i)]]),average(g[V[l(o(i))]],g[V[r(o(i))]])).to(average(g[V[i]],g[V[o(i)]])));
        }
      }
    }
  }
  
  private void modifiedButterflyBulge(int[] W, pt3[] g) {
    int nt = O.length/3;
    // tweaks the new mid-edge vertices according to the Butterfly mask
    for(int i = 0; i < 3*nt; i++) {
      // i < o(i) means: only tweak one of the half edges, arbitrarily the lower ordered one
      if(!b(i) && i < o(i)) {    // no tweak for mid-vertices of border edges
        if (!b(p(i))&&!b(n(i))&&!b(p(o(i)))&&!b(n(o(i)))) { // check that
          g[V[W[i]]].add(0.25f,average(average(g[V[l(i)]],g[V[r(i)]]),average(g[V[l(o(i))]],g[V[r(o(i))]])).to(average(g[V[i]],g[V[o(i)]])));
        }
      }
    }
  }

  
  /**
   * Create new edges connecting the vertexes created 
   */
  private void splitTriangles(int[] W) {    // splits each triangle into 4
    int ne = O.length;
    int nv = V.length;
    
    V = expand(V, V.length*4);
    O = expand(O, O.length*4);
    
    for (int i = 0; i < ne; i += 3) {
      V[1*ne+i]=v(i);    V[n(1*ne+i)]=W[p(i)]; V[p(1*ne+i)]=W[n(i)];
      V[2*ne+i]=v(n(i)); V[n(2*ne+i)]=W[i];    V[p(2*ne+i)]=W[p(i)];
      V[3*ne+i]=v(p(i)); V[n(3*ne+i)]=W[n(i)]; V[p(3*ne+i)]=W[i];
      V[i]=W[i]; V[n(i)]=W[n(i)]; V[p(i)]=W[p(i)];
    }
  }
  
  public void recomputeO() {
    computeO(numVerts);
  }
  
  // this is from Jarek's code and I haven't studied it deeply
  // it's fairly fast, and it appears to handle the triangle-soup case
  // but I'm using it when repairing the connectivity after subdividing,
  // which seems like it would be amenable to a cheaper solution
  private void computeO(int numVerts) {
    int numCorners = O.length;
    
    int valence[] = new int[numVerts];
    for (int v=0; v<numVerts; v++) valence[v]=0;
    for (int c=0; c<numCorners; c++) valence[v(c)]++;   // valences
    
    int incidenceCorners[] = new int[numVerts];
    int rfic=0;
    for (int v=0; v<numVerts; v++) {incidenceCorners[v]=rfic; rfic+=valence[v];}  // head of list of incident corners
    
    for (int v=0; v<numVerts; v++) valence[v]=0;   // valences wil be reused to track how many incident corners were encountered for each vertex
    
    int [] C = new int[numCorners]; // for each vertex: the list of val[v] incident corners starts at C[fic[v]]
    for (int c=0; c<numCorners; c++) {
      C[incidenceCorners[v(c)]+valence[v(c)]++]=c;
    }
    
    for (int c=0; c<numCorners; c++) O[c] = -1;    // init O table to -1 meaning that a corner has no opposite (i.e. faces a border)
    
    for (int v = 0; v < numVerts; v++) {            // for each vertex...
      int numIters = incidenceCorners[v]+valence[v]-1;
      for (int a = incidenceCorners[v]; a < numIters; a++) {
        for (int b = a+1; b < numIters+1; b++)  { // for each pair (C[a],C[b[]) of its incident corners
          // if C[a] follows C[b] around v, then p(C[a]) and n(C[b]) are opposite
          if(v(n(C[a])) == v(p(C[b]))) {
            O[p(C[a])]=n(C[b]);
            O[n(C[b])]=p(C[a]); 
          } 
          if(v(n(C[b])) == v(p(C[a]))) {
            O[p(C[b])]=n(C[a]);
            O[n(C[a])]=p(C[b]);
          }
        }
      }
    }
  }

  public CornerTable get() {
    CornerTable cloned = new CornerTable();
    cloned.O = O.clone();
    cloned.V = V.clone();
    cloned.numVerts = numVerts;
    return cloned;
  }
  
  public static CornerTable delaunay(pt[] points, InteriorTest pip) {
    return new QuickHullToCornerTable().make(points, pip);
  }
  
  public static CornerTable delaunay(pt[] points) {
    return delaunay(points, null);
  }
  
  public static CornerTable delaunay(List<pt> points, InteriorTest pip) {
    return new QuickHullToCornerTable().make(points, pip);
  }
  
  public static CornerTable delaunay(List<pt> points) {
    return delaunay(points, null);
  }
  
  public static CornerTable delaunay(float[][] points, InteriorTest pip) {
    return new QuickHullToCornerTable().make(points, pip);
  }
  
  public static CornerTable delaunay(float[][] points) {
    return delaunay(points, null);
  }
  
  public static CornerTable delaunayInterior(pt[] points) {
    return delaunay(points, new InteriorTest(toPackedArray(points)));
  }
  
  public static CornerTable delaunayInterior(List<pt> points) {
    return delaunay(points, new InteriorTest(toPackedArray(points)));
  }
  
  public static CornerTable delaunayInterior(float[][] points) {
    return delaunay(points, new InteriorTest(points));
  }
  
  // odd idiom here, subclassing to get access to protected stuff,
  // but i'm already editing the quickhull package to override some permissions and such
  protected static class QuickHullToCornerTable extends QuickHull3D {

    QuickHullToCornerTable() {}
    
    CornerTable make(pt[] points, InteriorTest pip) {
      return make(Oplet.toPackedArray(points), pip);
    }
    
    CornerTable make(List<pt> points, InteriorTest pip) {
      return make(Oplet.toPackedArray(points), pip);
    }
    
    CornerTable make(float[][] points, InteriorTest pip) {
  
      if(points.length < 3) {
        return null;
      }
      double qPoints[] = Delaunay.preprocess(points);
      return make(qPoints, points.length, pip);
    }
    
    CornerTable make(double[] qPoints, int pointsLength, InteriorTest pip) {
  
      build(qPoints, qPoints.length/3);
      
      CornerTable table = new CornerTable(this.faces.size());
      table.numVerts = pointsLength;
      for(int i = 0; i < table.O.length; i++) {
        table.O[i] = -1; // border indicator
      }
        
      int i = 0;
      HashMap<String,Integer> visited = new HashMap<String,Integer>(); 
      for(Face face : this.faces) {
        // skip the artifacts from the preprocess step
        // (they all refer to points that didn't exist in the original input)
        // this must iterate the face once, maybe there's a way to avoid that,
        // through some clever loop fusion
        pt center = center(face);
        if(hasVertexGreaterThan(face, pointsLength)
        || (pip != null && !pip.contains(center.x, center.y))) {
          continue;
        }
        
        // loop around this face, creating new V,O table entries
        HalfEdge first = face.getFirstEdge();
        HalfEdge e = first;
        
        // the problem here only appeared when doing lloyd relaxtion, why???
        // maybe the result is degenerate in some special way?
        if(i >= table.V.length) continue;
        do {
          
          Integer opposite = visited.get(e.getVertexString());
          if(opposite == null) { // haven't yet visited pair
            // so remember the current edge index when we get back to it
            // the vertex string reversed relative to out pair,
            // so we store our pair's and lookup our own
            visited.put(e.getOpposite().getVertexString(), i);
          } else {
            // record the opposites of these two
            int oppValue = opposite.intValue();
            table.O[i] = oppValue;
            table.O[oppValue] = i;
          }
          // FIXME: this will only work for triangle meshes,
          // which is hopefully all we're dealing with here, but...
          table.V[i] = e.getNext().head().index;
          i++;
          e = e.getNext();
        } while(e != first);
      }
      
      table.trim((int)Math.ceil(i/3f)*3);

      return table;
    }

    private pt center(Face face) {
      pt result = new pt();
      int count = 0;
      HalfEdge first = face.getFirstEdge();
      HalfEdge e = first;
      do {
        Vertex v = e.getNext().head();
        result.add(v.pnt.x, v.pnt.y);
        e = e.getNext();
        count++;
      } while(e != first);
      result.scaleBy(1f/count);
      return result;
    }

    private boolean hasVertexGreaterThan(Face face, int length) {
      HalfEdge he = face.getFirstEdge(), he0;
      he0 = he;
      do {
        if(he.head().index >= length) return true;
        he = he.getNext();
      } while (he != he0);
      return false;
    }
  }

  /**
   * Throw away some corners,
   * this is dangerous.
   * 
   * It might be nice to preallocate extra space in the V,O tables, for mutability
   */
  void trim(int i) {
    int[] newV = new int[i];
    int[] newO = new int[i];
    System.arraycopy(V, 0, newV, 0, i);
    System.arraycopy(O, 0, newO, 0, i);
    V = newV;
    O = newO;
  }

  public ArrayList<int[]> getEdges() {
    ArrayList<int[]> edges = new ArrayList<int[]>();
    for(int i = 0; i < O.length; i += 3) {
      if(V[i] < V[i+1] || b(i+2)) {
        edges.add(new int[]{V[i],V[i+1]});
      }
      if(V[i+1] < V[i+2] || b(i)) {
        edges.add(new int[]{V[i+1],V[i+2]});
      }
      if(V[i+2] < V[i] || b(i+1)) {
        edges.add(new int[]{V[i+2],V[i]});
      }
    }
    return edges;
  }

  /**
   * Iterate over the corners around a vertex, given a corner at that vertex.
   * 
   */
  public VertexIterator vertexCorners(int corner) {
    return new VertexIterator(corner);
  }
  
  static public int maxVertexIterations = 100; // avoid infinite loops
  
  public class VertexIterator implements Iterator<Integer>, Iterable<Integer> {
    int c,i,firstC;
    
    public VertexIterator(int cOnV) {
      c = firstC = n(cOnV);
      i = 0;
    }
    
    public boolean hasNext() {
      return (i == 0 || c != firstC) && i < 100;
    }

    public Integer next() {
      i++;
      if(O[c] < 0) { // backtrack (should only happen once)
        c = n(c);
        while(O[c] > -1 && i < 100) {
          c = n(O[c]);
          i++;
        }
        c = p(c);        
      } else {
        c = p(O[c]);
      }
      return p(c);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public Iterator<Integer> iterator() {
      return this;
    }
  }

  /*
  class VertexIterator implements Iterator<Integer>, Iterable<Integer> {
    int firstCorner, currentCorner, iterations;
    
    VertexIterator(int corner) {
      firstCorner = corner;
      currentCorner = -1;
      iterations = 0;
    }

    public boolean hasNext() {
      return firstCorner != currentCorner && iterations < maxVertexIterations;
    }

    public Integer next() {
      if(currentCorner == -1) {
        currentCorner = firstCorner;
      }
      int result = currentCorner;
      currentCorner = n(currentCorner);
      if(O[currentCorner] > -1) {
        currentCorner = n(O[currentCorner]);
      } else {
        currentCorner = firstCorner; // terminate... not the best thing to do, would like to "hop" along the border to find the next... neighbor
      }
      // the opposite call here is making a mess of things
      //currentCorner = n(o(n(currentCorner)));
      iterations++;
      return result;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public Iterator<Integer> iterator() {
      return this;
    }
  }*/
  
  public OneRingIterator oneRing(int corner) {
    return new OneRingIterator(corner);
  }
  
  public class OneRingIterator implements Iterator<Integer>, Iterable<Integer> {
    int c,i,firstC;
    boolean backtracking;
    
    public OneRingIterator(int cOnV) {
      c = firstC = n(cOnV);
      i = 0;
      backtracking = false;
    }
    
    public boolean hasNext() {
      return (i == 0 || c != firstC) && i < 100;
    }

    public Integer next() {
      i++;
      if(O[c] < 0) {
        backtracking = true;
        c = n(c); // output the current one before backtracking
      } else if(backtracking) {
        backtracking = false;
        while(O[c] > -1 && i < 100) {
          c = n(O[c]);
          i++;
        }
        c = p(c); // output the found boundary        
      } else {
        c = p(O[c]);
      }
      return V[c];
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public Iterator<Integer> iterator() {
      return this;
    }

    public int getCurrentCorner() {
      return c;
    }
  }
  
  public static class StarLinkEdge {
    public int v; // the vertex
    public int c; // a corner at the vertex
    public StarLinkEdge(int v, int c) {
      this.v = v;
      this.c = c;
    }
  }
  
  class StarLinkIterator implements Iterator<StarLinkEdge>,Iterable<StarLinkEdge> {
    OneRingIterator ori;
    
    StarLinkIterator(int v) {
      ori = new OneRingIterator(v);
    }
    
    public boolean hasNext() {
      return ori.hasNext();
    }

    public StarLinkEdge next() {
      int v = ori.next();
      return new StarLinkEdge(v, ori.c);
    }

    public void remove() {
      throw new UnsupportedOperationException();
      
    }

    public Iterator<StarLinkEdge> iterator() {
      return this;
    }
  }
  
  Iterable<StarLinkEdge> starLink(int v) {
    return new StarLinkIterator(v);
  }
  
  
  
  public Iterable<Pair<Integer,Integer>> aroundVertexPairs(int corner) {
    return new PairIterator<Integer>((Iterator<Integer>)new VertexIterator(corner));
  }

  public vec edgeNormal(int i, pt[] pts) {
    return R(pts[V[p(i)]], pts[V[n(i)]]);
  }
  
  public vec edgeNormal(int i, List<pt> pts) {
    return R(pts.get(V[p(i)]), pts.get(V[n(i)]));
  }

  // TODO: write test cases for this
  public boolean flip(int c) {
    if(O[c] < 0) return false;
    int
    co = O[c],
    cn = n(c),
    cp = p(c),
    con = n(co),
    cop = p(co),
    cno = O[cn],
    cpo = O[cp],
    copo = O[cop],
    cono = O[con],
    
    va = V[c],
    vb = V[O[c]],
    vc = V[n(c)],
    vd = V[p(c)];
    
    V[c] = vc;
    V[co] = vd;
    V[cn] = vb;
    V[cop] = vb;
    V[cp] = va;
    V[con] = va;
    
    
    if(cpo >= 0) O[cpo] = cn;
    O[cn] = cpo;
    
    if(cno >= 0) O[cno] = cop;
    O[cop] = cno;
    
    if(cono >= 0) O[cono] = cp;
    O[cp] = cono;
    
    if(copo >= 0) O[copo] = con;
    O[con] = copo;
    
    return true;
  }

  public int numFaces() {
    return O.length/3;
  }
  
  // TODO: write test cases, document how to use these safely/efficiently 
  protected int rightwardBorder(int c) {
    c = n(c);
    int firstC = c;
    do {
      int oc = O[c];
      if(oc < 0) return c;
      c = p(oc);
    } while(c != firstC);
    return -1;
  }
  
  protected int leftwardBorder(int c) {
    c = p(c);
    int firstC = c;
    do {
      int oc = O[c];
      if(oc < 0) return c;
      c = n(oc);
    } while(c != firstC);
    return -1;
  }
  
  // also stop when hitting a hidden edge, mask.length == O.length/3
  protected int rightwardBorderMasked(int c, boolean[] mask) {
    c = n(c);
    int firstC = c;
    do {
      int oc = O[c];
      if(oc < 0 || (mask != null && !mask[t(oc)])) return c;
      c = p(oc);
    } while(c != firstC);
    return -1;
  }

  public boolean isVertexOnBorder(int c) {
    return rightwardBorder(c) >= 0;
  }
  
  public boolean isVertexOnBorder(int c, boolean[] mask) {
    return rightwardBorderMasked(c, mask) >= 0;
  }

  public List<Integer> getBorder() {
    return getBorder(null);
  }
  
  public List<Integer> getBorder(int[] C) {
    if(C == null) C = inverseV();
    
    List<Integer> result = new ArrayList<Integer>();
    
    int v = 0;
    while(!isVertexOnBorder(v) && v < numVerts) v++;
    // assumes one connecte component, otherwise we'd need another loop wrapping the stuff below
    
    if(v < numVerts) {
      
      int c = n(rightwardBorder(C[v]));
      int firstC = c;
      int iterations = 0;
      do {
        result.add(V[c]);
        
        c = n(rightwardBorder(c));
        
        iterations++;
      } while(c != firstC && iterations < 3000);
    }
    return result;
    }

  public class Path {
    List<Integer> verts = new ArrayList<Integer>();

    public void add(int vert) {
      verts.add(vert);
    }
    
    public void reverse() {
      Collections.reverse(verts);
    }

    public int size() {
      return verts.size();
    }
  }

  public boolean inTriangle(pt[] pts, int t, pt query) {
    return Geometry.inTriangle(pts[V[t*3+2]], pts[V[t*3+1]], pts[V[t*3+0]], query);
  }
  
  public boolean isDegenerate() {
    boolean degenerate = false;
    // check that corners of all triangles are unique
    for(int c = 0; c < V.length; c += 3) {
      if(V[c+0] == V[c+1]) {degenerate = true; System.err.println("V["+(c+0)+"] == V["+(c+1)+"]");}
      if(V[c+1] == V[c+2]) {degenerate = true; System.err.println("V["+(c+1)+"] == V["+(c+2)+"]");}
      if(V[c+2] == V[c+0]) {degenerate = true; System.err.println("V["+(c+2)+"] == V["+(c+0)+"]");}
      
      if(O[c+0] >= 0 && O[O[c+0]] != c+0) {degenerate = true; System.err.println("O[O["+(c+0)+"]] != "+(c+0));}
      if(O[c+1] >= 0 && O[O[c+1]] != c+1) {degenerate = true; System.err.println("O[O["+(c+1)+"]] != "+(c+1));}
      if(O[c+2] >= 0 && O[O[c+2]] != c+2) {degenerate = true; System.err.println("O[O["+(c+2)+"]] != "+(c+2));}
    }
    return degenerate;
  }
}
