package shapecore.mesh;

import shapecore.pt3;

/**
 * Tetrahedral mesh, store in a corner-table style format.
 * 
 * Each tet has 4 corners.
 * Unlike the 2D case, we need to be aware of orientation changes when doing an O flip.
 * 
 */
public class TetMesh {
  
  
  // references to geometry
  int[] V;
  // references to opposites of this tet corner
  int[] O;
  pt3[] G;
}
