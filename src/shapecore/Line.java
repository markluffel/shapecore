package shapecore;

public class Line {
  public pt on;
  public vec dir;
  
  public Line(pt on, vec dir) {
    this.on = on;
    this.dir = dir;
  }
  
  public Line(pt a, pt b) {
    this.on = a.get();
    this.dir = a.to(b);
  }
}
