package shapecore;

/**
 * A small helper for handling weighted sums.
 *
 */
public class pt3sum {
  
  float x,y,z;
  float weights;
  
  void add(pt3 p) {
    x += p.x;
    y += p.y;
    z += p.z;
    weights += 1;
  }
  void add(pt3 p, float weight) {
    x += p.x*weight;
    y += p.y*weight;
    z += p.z*weight;
    weights += weight;
  }
  
  pt3 getResult() {
    return new pt3(x/weights, y/weights, z/weights);
  }
  
}
