/**
 * 
 */
package shapecore.mesh;

import processing.core.PImage;
import shapecore.Oplet;
import shapecore.pt;

public class ButterflyMesh {
  public CornerTable table;
  int[][] cachedMasks; // first entry is the first derived value after the inputVerts starts after the input/original
  public pt[] G; // "staging area" / cache, stores stuff between getting actual geometry, and drawing that geometry
  public pt[] uv;
  int numInputVerts;
  
  public ButterflyMesh(CornerTable table, int numVerts, int depth) {
    this.numInputVerts = numVerts;
    this.table = table.get();
    cachedMasks = new int[0][0];

    while(depth > 0) {
      int[][] newCachedMasks = this.table.butterfly(numInputVerts+cachedMasks.length);

      // copy them into the same large array
      // TODO: hold onto the different iterations of masks, and only copy them into the final array once
      int[][] combinedCachedMasks = new int[cachedMasks.length + newCachedMasks.length][];
      System.arraycopy(cachedMasks, 0, combinedCachedMasks, 0, cachedMasks.length);
      System.arraycopy(newCachedMasks, 0, combinedCachedMasks, cachedMasks.length, newCachedMasks.length);
      cachedMasks = combinedCachedMasks;
      
      depth--;
    }
    G = new pt[numInputVerts+cachedMasks.length];
    uv = new pt[numInputVerts+cachedMasks.length];
  }
  
  public void computeGeometry(pt[] inputVerts) {
    if(inputVerts.length != numInputVerts) throw new IllegalArgumentException("Requires "+numInputVerts+" original verticies to compute derived verticies");
    
    System.arraycopy(inputVerts, 0, G, 0, numInputVerts);
    for(int i = inputVerts.length; i < G.length; i++) {
      G[i] = computeFromMask(cachedMasks[i-inputVerts.length], G);
    }
  }
  
  /**
   * This uses the latest geometry passed to computeGeometry()
   * 
   * @param i the index of the point to compute
   * @return
   */
  public pt computeSinglePoint(int i) {
    return computeFromMask(cachedMasks[i-numInputVerts], G);
  }
  
  public void computeUv(pt[] inputUv) {
    if(inputUv.length != numInputVerts) throw new IllegalArgumentException("Requires "+numInputVerts+" original verticies to compute derived verticies");
    
    System.arraycopy(inputUv, 0, uv, 0, numInputVerts);
    for(int i = inputUv.length; i < uv.length; i++) {
      uv[i] = computeFromMask(cachedMasks[i-inputUv.length], uv);
    }
  }
  
  private static final double half = 1/2d, eighth = 1/8d, sixteenth = 1/16d;
  private static final pt zero = new pt();
  protected static pt computeFromMask(int[] mask, pt[] pts) {
    pt
    a1 = v(mask,0,pts), a2 = v(mask,1,pts),
    b1 = v(mask,2,pts), b2 = v(mask,3,pts),
    c1 = v(mask,4,pts), c2 = v(mask,5,pts), c3 = v(mask,6,pts), c4 = v(mask,7,pts);
    return new pt(
        half*(a1.x+a2.x) + eighth*(b1.x+b2.x) - sixteenth*(c1.x+c2.x+c3.x+c4.x),
        half*(a1.y+a2.y) + eighth*(b1.y+b2.y) - sixteenth*(c1.y+c2.y+c3.y+c4.y)
    );
  }
  
  // helper to avoid boundary cases
  private static final pt v(int[] mask, int index, pt[] pts) {
    if(mask[index] >= 0) {
      return pts[mask[index]];
    } else {
      return zero;
    }
  }
  
  
  public void draw(Oplet p) {
    table.draw(p, G);
  }
  
  public void drawTextured(Oplet p, PImage texture) {
    table.drawTextured(p, G, texture, uv);
  }
}