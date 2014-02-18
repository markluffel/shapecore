
package shapecore;

import static shapecore.Fitting.*;
import static shapecore.Oplet.*;

import java.util.Arrays;
import java.util.List;

import com.sun.tools.internal.ws.processor.modeler.wsdl.PseudoSchemaBuilder;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class Rigid3D {
  public pt3 O;
  public vec3 I, J, K;
  
  public static Rigid3D fit(pt3[] src, pt3 dst[]) {
    return fit(Arrays.asList(src), Arrays.asList(dst));
  }
  
  public static Rigid3D fit(List<pt3> src, List<pt3> dst) {
    return fit(src, dst, null);
  }
  
  public static Rigid3D fit(List<pt3> src, List<pt3> dst, float[] weights) {
    pt3 srcCenter,dstCenter;
    if(weights == null) {
      srcCenter = pt3.average(src);
      dstCenter = pt3.average(dst);
      weights = new float[src.size()];
      Arrays.fill(weights, 1);
    } else {
      srcCenter = weightedCenter3(src, weights);
      dstCenter = weightedCenter3(dst, weights);      
    }
    return fit(src, dst, srcCenter, dstCenter, weights);
  }
  
  public static Rigid3D fit(List<pt3> src, List<pt3> dst, pt3 srcCenter, pt3 dstCenter, float[] weights) {
    if(src.size() < 2) throw new IllegalArgumentException();
    
    vec3[] pHat = relativeTo(srcCenter, src);
    vec3[] qHat = relativeTo(dstCenter, dst);
    
    Matrix M = weightedOuterProductSum(pHat, qHat, weights);
    SingularValueDecomposition svd = M.svd();
    Matrix U = svd.getU(), Vt = svd.getV().transpose(), UVt = U.times(Vt);
    
    float sign = sgn(UVt.det());
    if(false && sign < 0) {
      double[] s = svd.getSingularValues();
      int minI = -1; double minS = Float.MAX_VALUE;
      for(int i = 0; i < 3; i++) {
        if(s[i] < minS) {
          minI = i;
          minS = s[i];
        }
      }
      // flip min row
      for(int i = 0; i < 3; i++) {
        U.set(minI, i, -U.get(minI, i));
      }
      println("flip");
      UVt = U.times(Vt);
    }
    return makeRigid(UVt, srcCenter, dstCenter);
  }
  
  private static Rigid3D makeRigid(Matrix R, pt3 srcCenter, pt3 dstCenter) {
    Rigid3D rig = new Rigid3D();
    // see: http://en.wikipedia.org/wiki/Orthogonal_Procrustes_problem
    
    rig.O = new pt3(); // set to zero for xform below
    rig.I = new vec3(R.get(0, 0), R.get(1, 0), R.get(2, 0));
    rig.J = new vec3(R.get(0, 1), R.get(1, 1), R.get(2, 1));
    rig.K = new vec3(R.get(0, 2), R.get(1, 2), R.get(2, 2));
    
    srcCenter = srcCenter.get(); // don't distrub the input
    srcCenter.transform(rig);
    rig.O.add(srcCenter.to(dstCenter));
    return rig;
  }
  
}
