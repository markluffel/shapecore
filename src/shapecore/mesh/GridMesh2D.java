package shapecore.mesh;

import java.util.List;



import processing.core.PGraphics;
import processing.core.PImage;
import shapecore.Oplet;
import shapecore.pt;

public class GridMesh2D extends Mesh2D {

  int minX, minY, maxX, maxY;
  public int numRows, numCols;
  
  public GridMesh2D(int minX, int minY, int maxX, int maxY, int numRows, int numCols) {
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
    this.numRows = numRows;
    this.numCols = numCols;
    G = new pt[(numRows+1)*(numCols+1)];
    
    float xStep = (maxX-minX)/(float)numCols;
    float yStep = (maxY-minY)/(float)numRows;
    int i = 0;
    for(int y = 0; y <= numRows; y++) {
      for(int x = 0; x <= numCols; x++) {
        G[i] = new pt(minX + x*xStep, minY + y*yStep); i++;
      }
    }
  }

  public TriMesh2D toTriMesh() {
    int numTris = numCols*numRows*2;
    int[][] soup = new int[numTris][];
    int colSpan = numCols+1;
    int vert = 0; // upperleft vertex of current face
    int tri = 0;
    for(int y = 0; y < numRows; y++) {
      for(int x = 0; x < numCols; x++) {
        soup[tri] = new int[]{vert, vert+1+colSpan, vert+colSpan}; tri++; 
        soup[tri] = new int[]{vert, vert+1, vert+1+colSpan}; tri++;
        vert++;
      }
      vert++; // skip the vertex at the end of each row
    }
    CornerTable ct = new CornerTable(soup);
    return new TriMesh2D(G, ct);
  }
  
  @Override
  public Mesh2D clone() {
    return new GridMesh2D(minX, minY, maxX, maxY, numRows, numCols);
  }

  @Override
  public boolean isBorder(int v) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int numFaces() {
    return numRows*numCols;
  }

  @Override
  public void subdivide() {
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * G should be in row-major order, of length (numRows+1)*(numCols+1)
   * mask should be in row-major order, of length numRows*numCols
   * 
   */
  @Override
  public void drawUsingAlternateGeometry(Oplet p, pt[] G, boolean[] mask) {
    int colSpan = numCols+1;
    int vert = 0; // upperleft vertex of current face
    int face = 0; // current face
    for(int y = 0; y < numRows; y++) {
      for(int x = 0; x < numCols; x++) {
        if(mask[face]) {
          p.vertex(G[vert]);
          p.vertex(G[vert+1]);
          p.vertex(G[vert+1+colSpan]);
          p.vertex(G[vert+colSpan]);
        }
        vert++;
        face++;
      }
      vert++; // skip the vertex at the end of each row
    }
  }

  @Override
  public void drawUsingAlternateGeometry(Oplet p, pt[] G, PImage texture, pt[] uv, boolean[] mask) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public List<Integer> getBorder() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void grow(float distance) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void draw(Oplet p) {
    int colSpan = numCols+1;
    int vert = 0; // upperleft vertex of current face
    int face = 0; // current face
    p.beginShape(p.QUADS);
    for(int y = 0; y < numRows; y++) {
      for(int x = 0; x < numCols; x++) {
        p.vertex(G[vert]);
        p.vertex(G[vert+1]);
        p.vertex(G[vert+1+colSpan]);
        p.vertex(G[vert+colSpan]);
        vert++;
        face++;
      }
      vert++; // skip the vertex at the end of each row
    }
    p.endShape();
  }

  public void drawColored(Oplet p, int[] colors) {
    int colSpan = numCols+1;
    int vert = 0; // upperleft vertex of current face
    int face = 0; // current face
    p.beginShape(p.QUADS);
    for(int y = 0; y < numRows; y++) {
      for(int x = 0; x < numCols; x++) {
        p.fill(colors[vert]); p.vertex(G[vert]);
        p.fill(colors[vert+1]); p.vertex(G[vert+1]);
        p.fill(colors[vert+1+colSpan]); p.vertex(G[vert+1+colSpan]);
        p.fill(colors[vert+colSpan]); p.vertex(G[vert+colSpan]);
        vert++;
        face++;
      }
      vert++; // skip the vertex at the end of each row
    }
    p.endShape();
  }

  public void drawTextured(Oplet p, PImage texture) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void drawUsingAlternateGeometry(PGraphics p, pt[] G, PImage texture, pt[] uv, boolean[] mask) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void drawEdges(Oplet p) {
    int colSpan = numCols+1;
    int vert = 0; // upperleft vertex of current face
    int face = 0; // current face
    p.beginShape(p.LINES);
    for(int y = 0; y < numRows; y++) {
      for(int x = 0; x < numCols; x++) {
        p.vertex(G[vert]);
        p.vertex(G[vert+1]);
        p.vertex(G[vert]);
        p.vertex(G[vert+1+colSpan]);
        // TODO: handle the last row and column
        vert++;
        face++;
      }
      vert++; // skip the vertex at the end of each row
    }
    p.endShape();
  }

}
