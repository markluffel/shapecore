package shapecore;

import static processing.core.PApplet.*;
import processing.core.PMatrix3D;
import Jama.Matrix;

public class MatrixUtils {
  
  public static vec3 column(PMatrix3D m, int i) {
    switch(i) {
    case 0: return new vec3(m.m00, m.m10, m.m20);
    case 1: return new vec3(m.m01, m.m11, m.m21);
    case 2: return new vec3(m.m02, m.m12, m.m22);
    case 3: return new vec3(m.m03, m.m13, m.m23);
    default: throw new IllegalArgumentException();
    }    
  }
  
  public static Matrix rotation2D(float angle) {
    Matrix R = new Matrix(2,2);
    R.set(0,0, Math.cos(angle)); R.set(0,1, -Math.sin(angle));
    R.set(1,0, Math.sin(angle)); R.set(1,1,  Math.cos(angle));
    return R;
  }
  // TODO: check if the PMatrix3D is doing exactly the same thing
  public static PMatrix3D getRotationP(vec3 axis, float angle) {
    // TODO: expand this out to make it more efficient
    //[R] = [I] + s*[~axis] + t*[~axis]^2
    PMatrix3D cr = cross(axis);
    PMatrix3D cr2 = cr.get(); cr2.apply(cr);
    cr.scale((float)Math.sin(angle));
    cr2.scale(1-(float)Math.cos(angle));
    
    PMatrix3D result = new PMatrix3D();
    plusEquals(result, cr);
    plusEquals(result, cr2);
    return result;
  }
  
  public static void plusEquals(PMatrix3D target, PMatrix3D source) {
    target.m00 += source.m00; target.m01 += source.m01; target.m02 += source.m02; target.m03 += source.m03;
    target.m10 += source.m10; target.m11 += source.m11; target.m12 += source.m12; target.m13 += source.m13;
    target.m20 += source.m20; target.m21 += source.m21; target.m22 += source.m22; target.m23 += source.m23;
    target.m30 += source.m30; target.m31 += source.m31; target.m32 += source.m32; target.m33 += source.m33;
  }
  
  public static vec3 xform(PMatrix3D t, vec3 source) {
    vec3 target = new vec3();
    target.x = t.m00*source.x + t.m01*source.y + t.m02*source.z;
    target.y = t.m10*source.x + t.m11*source.y + t.m12*source.z;
    target.z = t.m20*source.x + t.m21*source.y + t.m22*source.z;
    return target;
  }
  public static pt3 xform(PMatrix3D t, pt3 source) {
    pt3 target = new pt3();
    target.x = t.m00*source.x + t.m01*source.y + t.m02*source.z + t.m03;
    target.y = t.m10*source.x + t.m11*source.y + t.m12*source.z + t.m13;
    target.z = t.m20*source.x + t.m21*source.y + t.m22*source.z + t.m23;
    return target;
  }
    
  public static PMatrix3D cross(vec3 v) {
    return new PMatrix3D(
       0,  -v.z,  v.y, 0,
       v.z,   0, -v.x, 0,
      -v.y, v.x,    0, 0,
       0,     0,    0, 0
    );
  }
  
  public static Matrix toMatrix(PMatrix3D m) { // column/row major... don't know FIXME
    float[] vals = new float[16]; m.get(vals);
    Matrix result = new Matrix(4,4);
    int k = 0;
    for(int i = 0; i < 4; i++) {
      for(int j = 0; j < 4; j++) {
        result.set(i, j, vals[k++]);
      }
    }
    return result;
  }
  
  public static vec xform(Matrix t, vec v) { return V(t.times(toColumnMatrix(v))); }
  public static pt xform(Matrix t, pt p) { return P(t.times(toColumnMatrix(p))); }
  // the two below are different: vec3 has a 0 for the homogeneous coord, while pt3 has 1
  public static vec3 xform(Matrix t, vec3 v) { return V3(t.times(toColumnMatrix(v))); }
  public static pt3 xform(Matrix t, pt3 p) { return P3(t.times(toColumnMatrix(p))); }
  
  public static vec V(Matrix V) { return Geometry.V(V.get(0,0), V.get(1,0)); }
  public static vec3 V3(Matrix V) { return Geometry.V(V.get(0,0), V.get(1,0), V.get(2,0)); }
  public static pt P(Matrix V) { return Geometry.P(V.get(0,0), V.get(1,0)); }
  public static pt3 P3(Matrix V) { return Geometry.P(V.get(0,0), V.get(1,0), V.get(2,0)); }
  
  /** Homogenous column vector (3x1) */
  public static Matrix toColumnMatrix(pt p) {
    return new Matrix(new double[][] { {p.x}, {p.y}, {1} });
  }
  /** Make non-homogeneous column vector (2x1) */
  public static Matrix toMatrix(pt p) {
    return new Matrix(new double[][] { {p.x}, {p.y}});
  }
  /** Homogenous column vector (4x1) */
  public static Matrix toColumnMatrix(pt3 p) {
    return new Matrix(new double[][] { {p.x}, {p.y}, {p.z}, {1} });
  }
  /** Make non-homogeneous column vector (3x1) */
  public static Matrix toMatrix(pt3 p) {
    return new Matrix(new double[][] { {p.x}, {p.y}, {p.z}});
  }
  /** Homogenous column vector (3x1) */
  public static Matrix toColumnMatrix(vec v) {
    return new Matrix(new double[][] { {v.x}, {v.y}, {0} });
  }
  /** Make non-homogeneous column vector (2x1) */
  public static Matrix toMatrix(vec v) {
    return new Matrix(new double[][] { {v.x}, {v.y}});
  }
  /** Homogenous column vector (4x1) */
  public static Matrix toColumnMatrix(vec3 v) {
    return new Matrix(new double[][] { {v.x}, {v.y}, {v.z}, {0} });
  }
  /** Make non-homogeneous column vector (3x1) */
  public static Matrix toMatrix(vec3 v) {
    return new Matrix(new double[][] { {v.x}, {v.y}, {v.z}});
  }
  

  public static void printMatrix(Matrix M) {
    println("[");
    for(int i = 0; i < M.getRowDimension(); i++) {
      for(int j = 0; j < M.getColumnDimension(); j++) {
        print(M.get(i,j)+" ");
      }
      println();
    }
    println("]");
  }
  
  public static Matrix pseudoinverse(Matrix M) {
    Matrix Mt = M.transpose();
    Matrix S = Mt.times(M);
    // uses LU decomposition if square, QR if non-square
    return S.inverse().times(Mt);
  }
}
