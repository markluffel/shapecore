package shapecore;


public class Circle  {
  
  public pt center;
  public float radius;
  
  public Circle(float x, float y, float radius) {
    this.center = new pt(x,y);
    this.radius = radius;
  }
  
  public Circle(pt center, float radius) {
    this.center = center;
    this.radius = radius;
  }

  public boolean contains(pt q) {
    return center.sqdist(q) < radius*radius;
  }
}