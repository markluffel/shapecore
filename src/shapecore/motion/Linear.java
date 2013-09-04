package shapecore.motion;

import static shapecore.Geometry.*;
import shapecore.pt;
import shapecore.interfaces.PointAnimator;
import shapecore.interfaces.PointPairAnimator;

public class Linear extends Field implements Trajectory, PointAnimator, PointPairAnimator {
  pt start,end;

  public Linear(pt start, pt end) {
    this.start = start;
    this.end = end;
  }

  public pt at(float t) {
    return lerp(start, end, t);
  }

  public pt apply(pt p, float time) {
    return T(p,time,V(start,end));
  }

  public pt apply(pt pStart, pt pEnd, float time) {
    return lerp(pStart, pEnd, time); // totally ignores the contents of the class ... whatever FIXME
  }
  
  public Trajectory compose(Trajectory t1, Trajectory t2) {
    return new LinearlyCombinedTrajectory(t1,t2);
  }
  
  class LinearlyCombinedTrajectory implements Trajectory {
    Trajectory t1,t2;
    
    public LinearlyCombinedTrajectory(Trajectory t1, Trajectory t2) {
      this.t1 = t1;
      this.t2 = t2;
    }

    public pt at(float t) {
      return lerp(t1.at(t), t2.at(t), t);
    }    
  }

  public Trajectory trajectory(pt p) {
    return new Linear(p, T(p,V(start,end)));
  }
}
