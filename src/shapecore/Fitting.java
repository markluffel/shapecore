package shapecore;

import static java.util.Arrays.asList;
import java.util.List;

import shapecore.interfaces.Ring;

import Jama.Matrix;




public class Fitting {

  public static Matrix weightedOuterProductSum(vec[] a, vec[] b, float[] w) {
    double[][] m = new double[][] {{0,0},{0,0}};
    for(int i = 0; i < a.length; i++) {
      m[0][0] += a[i].x*b[i].x*w[i];
      m[0][1] += a[i].x*b[i].y*w[i];
      m[1][0] += a[i].y*b[i].x*w[i];
      m[1][1] += a[i].y*b[i].y*w[i];
    }
    return new Matrix(m);
  }
  
  public static Matrix weightedOuterProductSum(vec3[] a, vec3[] b, float[] w) {
    double[][] m = new double[][] {{0,0,0},{0,0,0},{0,0,0}};
    for(int i = 0; i < a.length; i++) {
      m[0][0] += a[i].x*b[i].x*w[i];
      m[0][1] += a[i].x*b[i].y*w[i];
      m[0][2] += a[i].x*b[i].z*w[i];
      
      m[1][0] += a[i].y*b[i].x*w[i];
      m[1][1] += a[i].y*b[i].y*w[i];
      m[1][2] += a[i].y*b[i].z*w[i];
      
      m[2][0] += a[i].z*b[i].x*w[i];
      m[2][1] += a[i].z*b[i].y*w[i];
      m[2][2] += a[i].z*b[i].z*w[i];
    }
    return new Matrix(m);
  }
  
  public static Matrix outerProductSum(vec[] a, vec[] b) {
    double[][] m = new double[][] {{0,0},{0,0}};
    for(int i = 0; i < a.length; i++) {
      m[0][0] += a[i].x*b[i].x;
      m[0][1] += a[i].x*b[i].y;
      m[1][0] += a[i].y*b[i].x;
      m[1][1] += a[i].y*b[i].y;
    }
    return new Matrix(m);
  }
  
  public static Matrix weightedPerpProductSum(vec[] a, vec[] b, float[] w) {
    Matrix result = new Matrix(2,2);
    for(int i = 0; i < a.length; i++) {
      Matrix aa = new Matrix(new double[][] {{a[i].x,a[i].y},{a[i].y,-a[i].x}});
      Matrix bb = new Matrix(new double[][] {{b[i].x,b[i].y},{b[i].y,-b[i].x}});
      result.plusEquals(aa.times(bb).timesEquals(w[i]));
    }
    return result;
  }
  
  public static float weight(pt control, pt query) {
    return 1/control.sqDisTo(query); // can change the power here
  }
  
  public static pt weightedCenter(pt[] pts, float[] weights) {
    return weightedCenter(new pt(), asList(pts), weights);
  }
  public static pt weightedCenter(List<pt> pts, float[] weights) {
    return weightedCenter(new pt(), pts, weights);
  }
  public static pt3 weightedCenter(pt3[] pts, float[] weights) {
    return weightedCenter(new pt3(), asList(pts), weights);
  }
  public static pt3 weightedCenter3(List<pt3> pts, float[] weights) {
    return weightedCenter(new pt3(), pts, weights);
  }
  
  public static <T extends Ring<T>> T weightedCenter(T zero, List<T> pts, float[] weights) {
    float weightSum = 0;
    T ptSum = zero;
    for(int i = 0; i < pts.size(); i++) {
      ptSum.addScaledBy(weights[i], pts.get(i));
      weightSum += weights[i];
    }
    ptSum.scaleBy(1/weightSum);
    return ptSum;
  }
}
