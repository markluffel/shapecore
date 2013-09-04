package shapecore;

import java.util.List;

import shapecore.interfaces.PointAnimator;

public class Animate {

  public static void apply(PointAnimator xform, float t, List<pt> pts) {
    for(pt p : pts) {
      p.set(xform.apply(p, t));
    }
  }
  
}
