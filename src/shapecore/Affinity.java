/**
 * 
 */
package shapecore;

import static shapecore.Fitting.*;
import static shapecore.Geometry.*;
import static shapecore.Oplet.*;
import static shapecore.MatrixUtils.*;

import java.util.Arrays;
import java.util.List;

import Jama.Matrix;


public class Affinity {
  
  public pt O;
  public vec I, J;
  
  public Affinity() {
    O = P(0,0);
    I = V(0,1);
    J = V(1,0);
  }
  
  public Affinity(pt O, vec I, vec J) {
    this.O = O;
    this.I = I;
    this.J = J;
  }
  
  public static Affinity fit(pt[] src, pt[] dst) {
    return fit(Arrays.asList(src), Arrays.asList(dst));
  }
  public static Affinity fit(pt[] src, pt[] dst, float[] weights) {
    return fit(Arrays.asList(src), Arrays.asList(dst), weights);
  }
  public static Affinity fit(pt[] src, pt[] dst, pt srcCenter, pt dstCenter, float[] weights) {
    return fit(Arrays.asList(src), Arrays.asList(dst), srcCenter, dstCenter, weights);
  }
  
  public static Affinity fit(List<pt> src, List<pt> dst) {
    return fit(src, dst, null);
  }
  
  public static Affinity fit(List<pt> src, List<pt> dst, float[] weights) {
    pt srcCenter,dstCenter;
    if(weights == null) {
      srcCenter = pt.average(src);
      dstCenter = pt.average(dst);
      weights = new float[src.size()];
      Arrays.fill(weights, 1);
    } else {
      srcCenter = weightedCenter(src, weights);
      dstCenter = weightedCenter(dst, weights);      
    }
    return fit(src, dst, srcCenter, dstCenter, weights);
  }

  public static Affinity fit(List<pt> src, List<pt> dst, pt srcCenter, pt dstCenter, float[] weights) {
    if(src.size() < 2) throw new IllegalArgumentException();
    
    vec[] pHat = relativeTo(srcCenter, src);
    vec[] qHat = relativeTo(dstCenter, dst);
    
    Matrix a = weightedOuterProductSum(pHat, pHat, weights).inverse();
    Matrix b = weightedOuterProductSum(pHat, qHat, weights);

    Matrix L = a.times(b).transpose();
    return makeAffinity(L, srcCenter, dstCenter);
  }

  public static Affinity makeSimilarity(
      float xScale, float yScale, float angle,
      pt srcCenter, pt dstCenter) {
    Matrix L = new Matrix(2,2);
    L.set(0,0, xScale);
    L.set(1,1, yScale);
    return makeAffinity(
      L.times(rotation2D(angle)),
      srcCenter, dstCenter
    );
  }
  private static Affinity makeAffinity(Matrix L, pt srcCenter, pt dstCenter) {
    Affinity aff = new Affinity();
    aff.O.set(0,0);
    aff.I = new vec(L.get(0, 0), L.get(1, 0));
    aff.J = new vec(L.get(0, 1), L.get(1, 1));
    
    srcCenter = srcCenter.clone();
    srcCenter.transform(aff);
    aff.O = new pt(dstCenter.x-srcCenter.x, dstCenter.y-srcCenter.y);
    return aff;
  }
  
  public pt fixedPt() {
    float a = I.x, b = J.x, c = O.x, d = I.y, e = J.y, f = O.y;
    float det = (a-1)*(e-1) - b*d;
    return P( (c*(1-e)+f*b)/det, (f*(1-a)+d*c)/det);
  }
  
  public pt local(pt p) {
    return Oplet.local(p, O, I, J);
  }
  public vec local(vec v) {
    return Oplet.local(v, I, J);
  }

  public Affinity composeInverse(Affinity a) {
    return new Affinity(
      a.local(O),
      a.local(I),
      a.local(J)
    );
  }
  
  public pt transform(pt p) {
    return p.transform(this);
  }
}