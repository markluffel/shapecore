package shapecore;

/**
 * A simple struct for holding three consecutive points on a polyline.
 */
public class Corner {

  public pt a,b,c;
  
  public Corner(pt a, pt b, pt c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public float angle() {
    return Geometry.turnAngle(a, b, c);
  }
}
