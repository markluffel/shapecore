package shapecore;

public class Plane {
  /** A point on the plane */
  public pt3 center;
  /** A normal to the plane */
  public vec3 normal;
  
  public Plane(pt3 center, vec3 normal) {
    this.center = center;
    this.normal = normal;
  }

}
