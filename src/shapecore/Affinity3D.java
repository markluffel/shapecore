package shapecore;

import static shapecore.Fitting.*;
import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.Arrays;
import java.util.List;

import Jama.Matrix;

public class Affinity3D {

  public pt3 O;
  public vec3 I, J, K;
  
  public Affinity3D() {
    O = new pt3(0,0,0);
    I = new vec3(0,0,1);
    J = new vec3(0,1,0);
    J = new vec3(1,0,0);
  }
  
  public Affinity3D(pt3 O, vec3 I, vec3 J) {
    this.O = O;
    this.I = I;
    this.J = J;
  }
  
  public static Affinity3D fit(pt3[] src, pt3[] dst) {
    return fit(Arrays.asList(src), Arrays.asList(dst));
  }
  public static Affinity3D fit(pt3[] src, pt3[] dst, float[] weights) {
    return fit(Arrays.asList(src), Arrays.asList(dst), weights);
  }
  public static Affinity3D fit(pt3[] src, pt3[] dst, pt3 srcCenter, pt3 dstCenter, float[] weights) {
    return fit(Arrays.asList(src), Arrays.asList(dst), srcCenter, dstCenter, weights);
  }
  
  public static Affinity3D fit(List<pt3> src, List<pt3> dst) {
    return fit(src, dst, null);
  }
  
  public static Affinity3D fit(List<pt3> src, List<pt3> dst, float[] weights) {
    pt3 srcCenter,dstCenter;
    if(weights == null) {
      srcCenter = averagep3(src);
      dstCenter = averagep3(dst);
    } else {
      srcCenter = weightedCenter3(src, weights);
      dstCenter = weightedCenter3(dst, weights);      
    }
    return fit(src, dst, srcCenter, dstCenter, weights);
  }

  public static Affinity3D fit(List<pt3> src, List<pt3> dst, pt3 srcCenter, pt3 dstCenter, float[] weights) {
    if(src.size() < 2) throw new IllegalArgumentException();
    
    vec3[] pHat = relativeTo(srcCenter, src);
    vec3[] qHat = relativeTo(dstCenter, dst);
    
    Matrix A = weightedOuterProductSum(pHat, pHat, weights).inverse();
    Matrix B = weightedOuterProductSum(pHat, qHat, weights);

    Matrix L = A.times(B).transpose();
    return makeAffinity(L, srcCenter, dstCenter);
  }

  private static Affinity3D makeAffinity(Matrix L, pt3 srcCenter, pt3 dstCenter) {
    Affinity3D aff = new Affinity3D();
    aff.O.setTo(0,0,0);
    aff.I = new vec3(L.get(0, 0), L.get(1, 0), L.get(2, 0));
    aff.J = new vec3(L.get(0, 1), L.get(1, 1), L.get(2, 1));
    aff.J = new vec3(L.get(0, 2), L.get(1, 2), L.get(2, 2));
    
    srcCenter = srcCenter.get();
    srcCenter.transform(aff);
    aff.O = new pt3(dstCenter.x-srcCenter.x, dstCenter.y-srcCenter.y, dstCenter.z-srcCenter.z);
    return aff;
  }
  
  public pt3 fixedPt() {
    /*
    float a = I.x, b = J.x, c = O.x, d = I.y, e = J.y, f = O.y;
    float det = (a-1)*(e-1) - b*d;
    return P( (c*(1-e)+f*b)/det, (f*(1-a)+d*c)/det);
    */
    return null;
  }

}
