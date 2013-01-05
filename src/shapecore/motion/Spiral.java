/**
 * 
 */
package shapecore.motion;

import static shapecore.Fitting.*;
import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import shapecore.Oplet;
import shapecore.pt;
import shapecore.vec;
import shapecore.interfaces.PointAnimator;
import shapecore.interfaces.PointPairAnimator;

import Jama.Matrix;



public class Spiral extends Field implements PointAnimator, PointPairAnimator {
  public float angle;
  public float scale;
  public pt center;
  
  public Spiral(float angle, float scale, pt center) {
    this.angle = angle;
    this.scale = scale;
    this.center = center;
  }
  
  public Spiral(pt start, pt end, pt center) {
    this.angle = angle(V(center, start), V(center, end));
    this.scale = center.disTo(end) / center.disTo(start);
    this.center = center;
  }
  
  public Spiral(pt start, pt end, float angle, float scale) {
    this.angle = angle;
    this.scale = scale;
    this.center = spiralCenter(angle, scale, start, end);
  }
  
  public Spiral(pt startHead, pt startTail, pt endHead, pt endTail) {
    angle = spiralAngle(startHead, startTail, endHead, endTail);
    scale = spiralScale(startHead, startTail, endHead, endTail);
    center = spiralCenter(angle, scale, startHead, endHead);
  }
  
  /**
   * Combine the centers of several spirals, and fit a spiral through the given start and end points
   * 
   * Results are not good
   */
  public static Spiral centerBlend(pt start, pt end, Spiral... spirals) {
    pt center = new pt();
    int numCenters = 0;
    for(Spiral s : spirals) {
      if(s.center != null) {
        center.addPt(s.center);
        numCenters++;
      }
    }
    center.setTo(center.x/numCenters, center.y/numCenters);
    
    return new Spiral(
      spiralAngle(center, start, center, end),
      spiralScale(center, start, center, end),
      center
    );
  }
  
  public static Spiral centerBlend(pt start, pt end, List<Spiral> spirals) {
    return centerBlend(start, end, spirals.toArray(new Spiral[0]));
  }
  
  public static Spiral blend(pt start, pt end, Spiral... spirals) {
    float angle = 0, scale = 1;
    for(int i = 0; i < spirals.length; i++) {
      angle += spirals[i].angle;
      scale *= spirals[i].scale;
    }
    angle /= spirals.length;
    scale = pow(scale, 1f/spirals.length);
    
    pt center = spiralCenter(angle, scale, start, end);
    return new Spiral(angle, scale, center);
  }
  
  public static Spiral blend(pt start, pt end, List<Spiral> spirals) {
    return blend(start, end, spirals.toArray(new Spiral[0]));
  }
  
  public pt apply(pt p, float t) {
    return spiralPt(p, center, scale, angle, t);
  }
  
  public pt apply(pt pStart, pt pEnd, float t) { // ignore the end
    return spiralPt(pStart, center, scale, angle, t);
  }
  
  public List<pt> apply(List<pt> P, float t) {
    if(center == null) return clonePoints(P); 
    List<pt> result = new ArrayList<pt>();
    /*
    float a = t * angle;
    float z = pow(scale, t);
    float cos = cos(a), sin = sin(a);
    */
    for(pt p : P) {
      //result.add(spiralPt(p, center, a, z, cos, sin));
      result.add(spiralPt(p, center, scale, angle, t));
    }
    return result; 
  }
  
  public pt[] apply(pt[] P, float t) {
    if(center == null) return clonePoints(P); 
    
    pt[] result = new pt[P.length];
    // FIXME: uncomment this code, to improve the performance
    //float a = t * angle;
    //float z = pow(scale, t);
    //float cos = cos(a), sin = sin(a);
    for(int i = 0; i < result.length; i++) {
      //result[i] = spiralPt(P[i], center, a, z, cos, sin);
      result[i] = spiralPt(P[i], center, scale, angle, t);
    }
    return result; 
  }
  
  public static enum Centering {
    POINT_CLOUD, CONVEX_CENTROID
  }
  
  public static Spiral registering(pt[] P, pt[] Q) {
    return registering(Arrays.asList(P), Arrays.asList(Q), Centering.POINT_CLOUD);
  }
  
  public static Spiral registering(pt[] P, pt[] Q, float[] weights) {
    return registering(Arrays.asList(P), Arrays.asList(Q), weights);
  }
  
  public static Spiral registering(pt[] P, pt[] Q, pt pCenter, pt qCenter, float[] weights) {
    return registering(Arrays.asList(P), Arrays.asList(Q), pCenter, qCenter, weights);
  }
  
  public static Spiral registering(List<pt> P, List<pt> Q) {
    return registering(P,Q,Centering.POINT_CLOUD);
  }
  
  public static Spiral registering(List<pt> P, List<pt> Q, Centering centering) {
    pt pCenter = null, qCenter = null;
    switch(centering) {
    case POINT_CLOUD:
      pCenter = centerV(P);
      qCenter = centerV(Q);
      break;
    case CONVEX_CENTROID:
      pCenter = convexCentroid(P);
      qCenter = convexCentroid(Q);
      break; 
    }
    
    float s = 0, c = 0;
    float scale = 1;
    int min = min(P.size(), Q.size());
    
    for (int i = 0; i < min; i++) {
      vec pEdge = V(pCenter, P.get(i));
      vec qEdge = V(qCenter, Q.get(i));
      s += dot(pEdge, R(qEdge));
      c += dot(pEdge, qEdge);
      scale *= qEdge.norm()/pEdge.norm();
    }
    scale = pow(scale, 1f/(min));
    float angle = atan2(s, c);
    return new Spiral(pCenter, qCenter, -angle, scale);
  }
  
  public static Spiral registering(List<pt> P, List<pt> Q, float[] weights) {
    pt pStar = weightedCenter(P, weights);
    pt qStar = weightedCenter(Q, weights);
    
    return registering(P, Q, pStar, qStar, weights);
  }
  
  public static Spiral registering(List<pt> P, List<pt> Q, pt pCenter, pt qCenter, float[] weights) {
    float angle,scale;
    
    if(false) { // MLS-derived registration (seemingly less stable)
      vec[] pHat = relativeTo(pCenter, P);
      vec[] qHat = relativeTo(qCenter, Q);
      
      Matrix L = weightedPerpProductSum(pHat, qHat, weights);
      L.timesEquals(1/similarityMu(pHat, qHat, weights));
  
      angle = asin((float)L.get(0, 1));
      scale = (float)L.get(0, 0)/cos(angle);
    
    } else {
      float s = 0, c = 0;
      scale = 0;
      int min = min(P.size(), Q.size());
      float wSum = 0;
      for (int i = 0; i < min; i++) {
        vec pEdge = V(pCenter, P.get(i));
        vec qEdge = V(qCenter, Q.get(i));
        s += dot(pEdge, R(qEdge))*weights[i];
        c += dot(pEdge, qEdge)*weights[i];
        float qn = qEdge.norm();
        float pn = pEdge.norm();
        if(qn != 0 && pn != 0) {
          scale += log(qn/pn)*weights[i]; // weighted geometric mean
          wSum += weights[i]; // should this be outside the condition? it's not used to normalize angle...
        }
      }
      scale = exp(scale / wSum);
      angle = -atan2(s, c); // no need to normalize
    }
    
    return new Spiral(pCenter, qCenter, angle, scale);
  }

  private static double similarityMu(vec[] pHat, vec[] qHat, float[] weights) {
    double mu = 0;
    for(int i = 0; i < pHat.length; i++) {
      mu += weights[i]*pHat[i].dot(pHat[i]);
    }
    return mu;
  }
    
  public vec tangent(pt p, float t) {
    float eps = 0.001f;
    vec v = V(spiralPt(p, center, scale, angle, t-eps),
              spiralPt(p, center, scale, angle, t+eps));
    v.normalize();
    return v;
  }
  
  /*
  public void draw(JPApplet p, pt pt) {
    draw(p,pt,0.1f);
  }
  
  public void draw(JPApplet p, pt pt, float step) {
    p.beginShape();
    for(float t = 0; t < 1; t += step) {
      p.vertex(apply(pt,t));
    }
    p.vertex(apply(pt,1));
    p.endShape();
  }
  
  public void draw(JPApplet p, pt pt, float step, float end) {
    p.beginShape();
    float start = 0;
    if(end < start) {
      float temp = end; end = start; start = temp;
    }
    step = abs(step);
    for(float t = start; t < end; t += step) {
      p.vertex(apply(pt,t));
    }
    p.vertex(apply(pt,end));
    p.endShape();
  }
  */
  
  public void arrowhead(Oplet p, pt start, float t, float size) {
    pt pt = apply(start, t);
    vec tang = tangent(start, t);
    p.arrowhead(pt, tang, size);
  }
  
  public Trajectory trajectory(pt start) {
    Trajectory t = new Trajectory();
    t.start = start;
    return t;
  }
  
  // this is sketchy naming
  public class Trajectory implements shapecore.motion.Trajectory {
    pt start;
    
    public pt at(float t) {
      return apply(start,t);
    }
    
    public void draw(Oplet p) {
      p.draw(Spiral.this, start);
    }

    public Trajectory compose(float angle, float scale, pt center) {
      // TODO Auto-generated method stub
      return null;
    }
  }

  public shapecore.motion.Trajectory compose(shapecore.motion.Trajectory start, shapecore.motion.Trajectory end) {
    return compose(start, end, this);
  }
  
  public static ComposedSpirals compose(shapecore.motion.Trajectory start, shapecore.motion.Trajectory end, Spiral warp) {
    if(start == null || end == null) throw new IllegalArgumentException();
    ComposedSpirals cs = new ComposedSpirals();
    cs.start = start;
    cs.end = end;
    cs.warp = warp;
    return cs;
  }
  
  public static class ComposedSpirals implements shapecore.motion.Trajectory {
    public shapecore.motion.Trajectory start, end;
    public Spiral warp;

    public pt at(float t) {
      pt startPoint = start.at(t);
      pt endPoint = end.at(t);
      if(warp != null) {
        Spiral composed = new Spiral(startPoint, endPoint, warp.angle, warp.scale);
        return composed.apply(startPoint, t);
      } else {
        return L(startPoint, t, endPoint);
      }
    }

    public void draw(Oplet p) {
      p.draw(this);
    }
  }
}