package shapecore;

import static shapecore.Geometry.*;

/** a transform between two frames */
public class FrameDiff {
  public vec offset = new vec();
  public float angle;
  
  public static FrameDiff make(Frame src, Frame dst) {
    Frame f = src.toLocal(dst);
    FrameDiff x = new FrameDiff();
    x.offset = V(f.pos);
    x.angle = f.angle;
    return x;
  }
  
  public FrameDiff inverted() {
    FrameDiff x = new FrameDiff();
    x.offset = offset.get().back().rotateBy(-angle);
    x.angle = -angle;
    return x;
  }
  
  public Frame transform(Frame f) {
    f.translateLocal(offset);
    f.angle += angle;
    return f;
  }
}
