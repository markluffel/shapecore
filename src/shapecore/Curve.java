package shapecore;

public abstract class Curve {
  public abstract pt sample(float t);
  public abstract vec tangent(float t);
}
