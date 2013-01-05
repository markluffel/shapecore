package shapecore.mesh;

import java.util.List;

import megamu.mesh.InteriorTest;
import processing.core.PGraphics;
import processing.core.PImage;
import shapecore.Oplet;
import shapecore.pt;
import static shapecore.Geometry.*;

abstract public class Mesh2D {

  public pt[] G;
  
  public static TriMesh2D delaunay(pt[] pts, InteriorTest pip) {
    CornerTable t = CornerTable.delaunay(pts, pip);
    TriMesh2D mesh = new TriMesh2D(pts, t);
    return mesh;
  }
  
  public static TriMesh2D delaunay(pt[] pts) {
    return delaunay(pts, null);
  }
  
  public static TriMesh2D delaunay(List<pt> pts, InteriorTest pip) {
    CornerTable t = CornerTable.delaunay(pts, pip);
    TriMesh2D mesh = new TriMesh2D(pts.toArray(new pt[0]), t);
    return mesh;
  }
  
  public static TriMesh2D delaunay(List<pt> pts) {
    return delaunay(pts, null);
  }
  
  public int numPoints() {
    return G.length;
  }

  /**
   * Split the faces of the mesh, don't preturb the location of the existing verticies
   */
  abstract public void subdivide();
  
  abstract public Mesh2D clone();
  
  
  /**
   * 
   * @return a (likely sparse) symmetric numPoints() x numPoints() matrix, containing a 1 where the associated vertices are connected by an edge
   *         and zero elsewhere
   */
  /*
  abstract public Matrix adjacencyMatrix();
  
  public Matrix cotanAdjacencyMatrix() {
    throw new UnsupportedOperationException();
  }*/
  
  /**
   * Returns the number of faces in this mesh
   * @return
   */
  abstract public int numFaces();
    
  /**
   * Returns true if this vertex is on the border of the mesh
   * @param v
   * @return
   */
  abstract public boolean isBorder(int v);
  
  /**
   * Expand the border along the normal of each edge
   * @param distance
   */
  abstract public void grow(float distance);
  
  /** 
   * Get a list of indicies of border vertiies
   * @return
   */
  abstract public List<Integer> getBorder();

  public void smoothInterior(int i, boolean[] mask) {
    
  }

  public void drawBorder(Oplet p, pt[] texInterp, boolean[] mask) {
    // TODO Auto-generated method stub
    
  }


  abstract public void drawUsingAlternateGeometry(Oplet p, pt[] G, boolean[] mask);
  abstract public void drawUsingAlternateGeometry(Oplet p, pt[] G, PImage texture, pt[] uv, boolean[] mask);
  abstract public void drawUsingAlternateGeometry(PGraphics p, pt[] G, PImage texture, pt[] uv, boolean[] mask);

  abstract public void draw(Oplet p);
  abstract public void drawEdges(Oplet p);
  
  abstract public void drawTextured(Oplet p, PImage texture);
  
  /**
   * 
   * You should only expect this to work when using the OPENGL renderer
   * 
   * @param p
   * @param colors
   */
  abstract public void drawColored(Oplet p, int[] colors);  
}
