package shapecore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import shapecore.pt;
import shapecore.vec;
import shapecore.mesh.CornerTable;



import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

public class ConvexHull {
  
  // several half-spaces
  public pt[] boundary; // pt on boundary
  public vec[] direction; // inward facing, apparently
  public int[] indices; // the vertex indices of the source points
  
  public static ConvexHull fromPoints(pt[] pts) {
    return fromPoints(Arrays.asList(pts));
  }
  
  public static ConvexHull fromPoints(List<pt> pts) {
    return ConvexHull.fromDelaunay(CornerTable.delaunay(pts), pts);
  }
  
  
  public static ConvexHull fromDelaunay(CornerTable table, pt[] pts) {
    return fromDelaunay(table, Arrays.asList(pts));
  }
  
  /**
   * If you pass something other than a CornerTable representing a Delaunay triangulation, you'll end up with nonsense on the other end.
   */
  public static ConvexHull fromDelaunay(CornerTable table, List<pt> pts) {
    ArrayList<pt> boundary = new ArrayList<pt>();
    ArrayList<vec> direction = new ArrayList<vec>();
    ArrayList<Integer> verts = new ArrayList<Integer>();
    
    for(int i = 0; i < table.O.length; i++) {
      if(table.b(i)) {
        pt a = pts.get(table.V[table.n(i)]);
        pt b = pts.get(table.V[table.p(i)]);
        verts.add(table.V[table.n(i)]);
        boundary.add(a);
        direction.add(normal(a,b)); // TODO: test that this is in the right direction
      }
    }
    // TODO: optimize this by shuffling the decision plane so that the amoritized time is minimized
    // in particular, something like sort by angle,
    // the pick the two most antiparallel planes
    // then the most orthogonal to those, then the most antiparallel to it,
    // the recursively onward
    // N,S,E,W,NW,SE,NE,SW
    // after the first 4-8 entries, there's probably not much to be gained by optimizing
    Object[][] tuples = new Object[direction.size()][];
    for(int i = 0; i < tuples.length; i++) {
      tuples[i] = new Object[]{ direction.get(i).angle(), boundary.get(i), direction.get(i), verts.get(i)};
    }
    
    Arrays.sort(tuples, new Comparator<Object[]>() {
      public int compare(Object[] o1, Object[] o2) {
        return Float.compare((Float)o1[0], (Float)o2[0]);
      }
    });
    ConvexHull hull = new ConvexHull();
    hull.boundary = new pt[tuples.length];
    hull.direction = new vec[tuples.length];
    hull.indices = new int[tuples.length];
    for(int i = 0; i < tuples.length; i++) {
      hull.boundary[i] = (pt)tuples[i][1];
      hull.direction[i] = (vec)tuples[i][2];
      hull.indices[i] = (Integer)tuples[i][3];
    }
    
    return hull;
  }
 
  public boolean contains(pt p) {
    for(int i = 0; i < boundary.length; i++) {
      if(!isInFrontOf(p, boundary[i], direction[i])) {
        return false;
      }
    }
    return true;
  }

  public void draw(Oplet p) {
    for(int i = 0; i < boundary.length; i++) {
      pt b = boundary[i]; vec dir = direction[i];
      p.beginShape();
      p.vertex(T(b,-200,R(dir)));
      p.vertex(T(b,200,R(dir)));
      p.vertex(T(b,200,R(dir), -200, dir));
      p.vertex(T(b,-200,R(dir), -200, dir));
      p.endShape();
    }
  }

  public void grow(float size) {
    for(int i = 0, pi = boundary.length-1; i < boundary.length; pi = i, i++) {
      vec norm = S(direction[i], direction[pi]);
      norm.toLength(-size);
      boundary[i].add(norm);
    }
  }
 
}
