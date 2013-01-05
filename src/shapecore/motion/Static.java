package shapecore.motion;

import shapecore.Oplet;
import shapecore.pt;

public class Static extends Field implements Trajectory {

  pt p;
  public Static(pt p) {
    this.p = p;
  }

  public pt at(float t) {
    return p.clone();
  }

  public void draw(Oplet p) {
  }

  public Trajectory compose(Trajectory t1, Trajectory t2) {
    return t1; // LOL
  }

  @Override
  public Trajectory trajectory(pt p) {
    return new Static(p);
  }
}
