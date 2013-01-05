/**
 * 
 */
package shapecore;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import shapecore.mesh.CornerTable;




public class Voronoi {
  
  /**
   * Connectivity infomation
   */
  public CornerTable table;
  
  public pt[] pts;
  
  /**
   * circumcenters of the triangles of the delaunay  
   */
  public pt[] dual;
  
  /**
   * compute an inverse for the V table, finds a corner given a vertex
   * each vertex corresponds to a cell of the voronoi diagram
   */
  public int[] C;

  public Voronoi(pt[] _pts) {
    pts = _pts;
    make();
  }

  public Voronoi(List<pt> points) {
    pts = points.toArray(new pt[points.size()]);
    make();
  }

  private void make() {
    table = CornerTable.delaunay(pts);
    dual = new pt[table.O.length/3];
    for(int i = 0; i < table.V.length; i += 3) {
      pt center = circumcenter(pts[table.V[i]], pts[table.V[i+1]], pts[table.V[i+2]]);
      dual[i/3] = center;
    }
    
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
  }
  
  public void draw(Oplet p) {
    for(int i = 0; i < table.O.length; i++) {
      if(i < table.O[i]) {
        int j = table.O[i];
        p.stroke(0);
        p.line(
          dual[i/3],
          dual[j/3]
        );
      } else if(table.O[i] < 0) {
        p.stroke(255,0,0);
        vec direction = table.edgeNormal(i, pts);
        p.line(dual[i/3], T(dual[i/3], 100, direction));
      }
    }
  }
  
  public int numCells() {
    return pts.length;
  }
  
  public void relax() {
    // recompute the V locations based on the voronoi centroid
    // if a region has infinite extent, don't change its center
    // don't move a center outside of the convex hull
    ConvexHull hull = ConvexHull.fromDelaunay(table, pts);
  }

  public void drawCell(Oplet p, int face) {
    p.polygon(region(face));
  }
  
  // based on the implementation of drawVoronoiCell in NaturalNeighborsViz
  public List<pt> region(int cell) {
    int LOOP_LIMIT = 20;
    List<pt> result = new ArrayList<pt>();
    int corner = C[cell];
    int firstCorner = corner;
    boolean hitBoundary = false;
    int loopCount = 0;
    do {
      result.add(dual[corner/3]);
      corner = table.n(corner);
      if(table.b(corner)) {
        hitBoundary = true;
        break; // TODO: traverse 
      }
      corner = table.n(table.o(corner));
      loopCount++;
    } while(corner != firstCorner && loopCount < LOOP_LIMIT);
    if(loopCount == LOOP_LIMIT) return Collections.emptyList();
    
    if(hitBoundary) {
      vec toInfinity = table.edgeNormal(corner, pts);
      result.add(T(dual[corner/3], 100, toInfinity));
      corner = firstCorner;
      while(!table.b(table.p(corner))) {
        corner = table.pop(corner);
      }
      corner = table.p(corner);
      toInfinity = table.edgeNormal(corner, pts);
      result.add(T(dual[corner/3], 100, toInfinity));
      corner = table.n(corner);
      loopCount = 0; 
      while(corner != firstCorner && loopCount < LOOP_LIMIT) {
        result.add(dual[corner/3]);
        corner = table.non(corner);
        loopCount++;
      }
      if(loopCount == LOOP_LIMIT) return Collections.emptyList();
    }
    return result;
  }
}