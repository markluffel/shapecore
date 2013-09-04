package shapecore.motion;

import static shapecore.Fitting.*;
import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.Arrays;
import java.util.List;

import shapecore.pt;
import shapecore.vec;
import shapecore.interfaces.PointAnimator;
import shapecore.interfaces.PointPairAnimator;


public class Rigid extends Field implements PointAnimator, PointPairAnimator {

  pt srcCenter, dstCenter;
  float angle;
  Rigid(pt srcCenter, pt dstCenter, float angle) {
    this.srcCenter = srcCenter;
    this.dstCenter = dstCenter;
    this.angle = angle;
  }

  @Override
  public shapecore.motion.Trajectory trajectory(pt p) {
    return new Trajectory(p);
  }
  
  // TODO: fixed point motion
  public pt apply(pt p, float t) {
    vec v = V(srcCenter, p);
    pt c = lerp(srcCenter, dstCenter, t);
    float a = lerp(0, angle, t);
    return T(c, R(v, a));
  }
  
  public vec apply(vec v, float t) {
    float a = lerp(0, angle, t);
    return R(v, a);
  }
  
  public pt apply(pt pStart, pt pEnd, float time) {
    return apply(pStart, time); // ignore the end (like Spiral does... but .. this make me uneasy)
    // because the rigid is not guaranteed to interpolate this point, is it?
  }

  public float getAngle() { return angle; }
  
  public static Rigid registering(pt[] P, pt[] Q) {
    float[] weights = new float[P.length];
    Arrays.fill(weights, 1);
    return registering(Arrays.asList(P), Arrays.asList(Q), weights);
  }
  
  public static Rigid registering(pt[] P, pt[] Q, float[] weights) {
    return registering(Arrays.asList(P), Arrays.asList(Q), weights);
  }
  
  public static Rigid registering(List<pt> P, List<pt> Q) {
    float[] weights = new float[P.size()]; Arrays.fill(weights, 1);
    return registering(P, Q, weights);
  }
  
  public static Rigid registering(List<pt> P, List<pt> Q, List<Float> weights) {
    float[] W = new float[weights.size()];
    for(int i = 0; i < weights.size(); i++) W[i] = weights.get(i);
    return registering(P, Q, W);
  }
  
  public static Rigid registering(List<pt> P, List<pt> Q, float[] weights) {
    pt pStar = weightedCenter(P, weights);
    pt qStar = weightedCenter(Q, weights);
    
    return registering(P, Q, pStar, qStar, weights);
  }
  
  
  public static Rigid registering(pt[] P, pt[] Q, pt pCenter, pt qCenter, float[] weights) {
    return registering(Arrays.asList(P), Arrays.asList(Q), pCenter, qCenter, weights);
  }
  
  public static Rigid registering(List<pt> P, List<pt> Q, pt pCenter, pt qCenter, float[] weights) {
    float angle;
  
    float s = 0, c = 0;
    int min = min(P.size(), Q.size());
    for (int i = 0; i < min; i++) {
      vec pEdge = V(pCenter, P.get(i));
      vec qEdge = V(qCenter, Q.get(i));
      s += dot(pEdge, R(qEdge))*weights[i];
      c += dot(pEdge, qEdge)*weights[i];
    }
    angle = -atan2(s, c); // no need to normalize
  
    return new Rigid(pCenter, qCenter, angle);
  }

  class Trajectory implements shapecore.motion.Trajectory {
    pt p;
    Trajectory(pt p) {
      this.p = p;
    }
    public pt at(float t) {
      return apply(p, t);
    }    
  }
}
