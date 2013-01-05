package shapecore;

import shapecore.rendering.MVector;
import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

public class Rotation {

  public double x;
  public double y;
  public double z;
  public double angle;

  public int coding = CODING_AXISANGLE;
  public static final int CODING_AXISANGLE = 0;
  public static final int CODING_QUATERNION = 1;

  /**
   * Axis-Angle constructor, must be normalized
   */
  public Rotation(MVector axis, double angle) {
    this.x = axis.x;
    this.y = axis.y;
    this.z = axis.z;
    this.angle = angle;
  }
  
  public Rotation(vec3 axis, double angle) {
    this.x = axis.x;
    this.y = axis.y;
    this.z = axis.z;
    this.angle = angle;
  }

  /** constructor which allows initial value to be suplied as axis angle,quaternion
    * or axis angle as defined by c1 whoes possible values are given by enum cde
    * @param x1 if quaternion or axis angle holds x dimention of normalised axis
    * @param y1 if quaternion or axis angle holds y dimention of normalised axis
    * @param z1 if quaternion or axis angle holds z dimention of normalised axis
    * @param a1 if quaternion holds w, if axis angle holds angle
    * @param c1 possible values are given by enum cde
    * */
  public Rotation(double x, double y, double z, double angle, int coding) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.angle = angle;
    this.coding = coding;
  }
  
  public static Rotation quaternion(double x, double y, double z, double w) {
    return new Rotation(x,y,z,w, CODING_QUATERNION);
  }
  public static Rotation axisAngle(float x, float y, float z, float angle) {
    return new Rotation(x,y,z,angle, CODING_AXISANGLE);
  }
  public static Rotation axisAngle(vec3 axis, double angle) {
    return new Rotation(axis.x,axis.y,axis.z,angle, CODING_AXISANGLE);
  }


  /** constructor to create Rotation from euler angles.
    * @param heading rotation about z axis
    * @param attitude rotation about y axis
    * @param bank rotation about x axis
    */
  public Rotation(double bank, double attitude, double heading) {
    double c1 = Math.cos(heading/2);
    double s1 = Math.sin(heading/2);
    double c2 = Math.cos(attitude/2);
    double s2 = Math.sin(attitude/2);
    double c3 = Math.cos(bank/2);
    double s3 = Math.sin(bank/2);
    double c1c2 = c1*c2;
    double s1s2 = s1*s2;
    angle =c1c2*c3 + s1s2*s3;
    x =c1c2*s3 - s1s2*c3;
    y =c1*s2*c3 + s1*c2*s3;
    z =s1*c2*c3 - c1*s2*s3;
    coding=CODING_QUATERNION;
  }

  /** copy constructor
    * @param in1 class to copy
    * */
  public Rotation(Rotation in1) {
    x=(in1!=null) ? in1.x : 0;
    y= (in1!=null) ? in1.y : 0;
    z= (in1!=null) ? in1.z : 1;
    angle= (in1!=null) ? in1.angle : 0;
    coding = (in1!=null) ? in1.coding : CODING_AXISANGLE;
  }

  public Rotation() {
  }

  /** calculates the effect of this rotation on a point
    * the new point is given by=q * P1 * q'
    * this version does not alter P1 but returns the result.
    *
    * for theory see:
  * http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/transforms/index.htm
  * @param point">point to be transformed</param>
    * @return translated point</returns>
    */
  // 49 multiplies and two trig functions
  public MVector transform(MVector p) {
    double wh = angle;
    double xh = x;
    double yh = y;
    double zh = z;
    
    if (coding == CODING_AXISANGLE) {
      double s = Math.sin(angle/2);
      xh = x * s;
      yh = y * s;
      zh = z * s;
      wh = Math.cos(angle/2);
    }
    
    double
    x2 = xh*xh, y2 = yh*yh, z2 = zh*zh, w2 = wh*wh,
    xy = xh*yh, yz = yh*zh, xz = xh*zh,
    xw = xh*wh, yw = yh*wh, zw = zh*wh;
    return new MVector(
      (float)(w2*p.x + 2*yw*p.z - 2*zw*p.y + x2*p.x + 2*xy*p.y    + 2*xz*p.z - z2*p.x - y2*p.x),
      (float)(2*xy*p.x + y2*p.y + 2*yz*p.z + 2*zw*p.x - z2*p.y    + w2*p.y - 2*xw*p.z - x2*p.y),
      (float)(2*xz*p.x + 2*yz*p.y + z2*p.z - 2*yw*p.x - y2*p.z    + 2*xw*p.y - x2*p.z + w2*p.z)
    );
  }
  
  public vec3 untransform(vec3 p) {
    minus();
    p = transform(p);
    minus();
    return p;
  }

  public vec3 transform(vec3 p) {
    double wh = angle;
    double xh = x;
    double yh = y;
    double zh = z;
    
    if (coding == CODING_AXISANGLE) {
      double s = Math.sin(angle/2);
      xh = x * s;
      yh = y * s;
      zh = z * s;
      wh = Math.cos(angle/2);
    }
    
    double
    x2 = xh*xh, y2 = yh*yh, z2 = zh*zh, w2 = wh*wh,
    xy = xh*yh, yz = yh*zh, xz = xh*zh,
    xw = xh*wh, yw = yh*wh, zw = zh*wh;
    return new vec3(
      (float)(w2*p.x + 2*yw*p.z - 2*zw*p.y + x2*p.x + 2*xy*p.y    + 2*xz*p.z - z2*p.x - y2*p.x),
      (float)(2*xy*p.x + y2*p.y + 2*yz*p.z + 2*zw*p.x - z2*p.y    + w2*p.y - 2*xw*p.z - x2*p.y),
      (float)(2*xz*p.x + 2*yz*p.y + z2*p.z - 2*yw*p.x - y2*p.z    + 2*xw*p.y - x2*p.z + w2*p.z)
    );
  }

  /** invert direction of rotation
    *
    */
  public void minus() {
    if (coding==CODING_AXISANGLE) {
      angle = -angle;
    } else {
      x=-x;
      y=-y;
      z=-z;
    }
  }

  /** get a clone of the rotation
    * @return a new array with value of minus this
    */
  public Rotation getMinus() {
    if (coding==CODING_AXISANGLE) return new Rotation(x,y,z,-angle,coding);
    else return new Rotation(-x,-y,-z,angle,coding);
  }

  /** set the axis of rotation
    * @param tx
    * @param ty
    * @param tz
    * */
  public void set(double tx,double ty,double tz) {
    angle = Math.sqrt(tx*tx + ty*ty + tz*tz);
    if (angle == 0) {x=1;y=z=0;return;}
    x = tx/angle;
    y = ty/angle;
    z = tz/angle;
  }

  public void set(double tx,double ty,double tz,double tangle){
    x = tx;
    y = ty;
    z = tz;
    angle = tangle;
  }
  /** returns axis in x dimention
    * @return axis in x dimention
    * */
  public double getTx() {
    return x*angle;
  }
  /** returns axis in y dimention
    * @return returns axis in y dimention
    * */
  public double getTy() {
    return y*angle;
  }
  /** returns axis in z dimention
    * @return returns axis in z dimention
    * */
  public double getTz() {
    return z*angle;
  }
  /**
   * Multiply this rotation with the given one, store result in this rotation. 
   * 
   * A.transform(B.transform(x)) == A.combine(B).transform(x);
   * 
   */
  public void combine(Rotation r) {
    toQuaternion();
    if (r==null) return;
    double qax = x;
    double qay = y;
    double qaz = z;
    double qaw = angle;
    double qbx;
    double qby;
    double qbz;
    double qbw;
    
    if (r.coding==CODING_QUATERNION) {
      qbx = r.x;
      qby = r.y;
      qbz = r.z;
      qbw = r.angle;
    } else {
      double s = Math.sin(r.angle/2);
      qbx = r.x * s;
      qby = r.y * s;
      qbz = r.z * s;
      qbw = Math.cos(r.angle/2);
    }
    // now multiply the quaternions
    angle =qaw*qbw - qax*qbx - qay*qby - qaz*qbz ;
    x=qax*qbw + qaw*qbx + qay*qbz - qaz*qby;
    y=qaw*qby - qax*qbz + qay*qbw + qaz*qbx;
    z=qaw*qbz + qax*qby - qay*qbx + qaz*qbw;
    coding=CODING_QUATERNION;
  }
  /** combine a rotation expressed as euler angle with current rotation.
    * first convert both values to quaternoins then combine and convert back to
    * axis angle. Theory about these conversions shown here:
  * http://www.euclideanspace.com/maths/geometry/rotations/conversions/index.htm
  * @param heading angle about x axis
    * @param attitude angle about y axis
    * @param bank angle about z axis
    * */
  public void combine(double heading,double attitude,double bank){
    // first calculate quaternion qb from heading, attitude and bank
    double c1 = Math.cos(heading/2);
    double s1 = Math.sin(heading/2);
    double c2 = Math.cos(attitude/2);
    double s2 = Math.sin(attitude/2);
    double c3 = Math.cos(bank/2);
    double s3 = Math.sin(bank/2);
    double c1c2 = c1*c2;
    double s1s2 = s1*s2;
    double qbw =c1c2*c3 + s1s2*s3;
    double qbx =c1c2*s3 - s1s2*c3;
    double qby =c1*s2*c3 + s1*c2*s3;
    double qbz =s1*c2*c3 - c1*s2*s3;
    // then convert axis-angle to quaternion if required
    toQuaternion();
    double qax = x;
    double qay = y;
    double qaz = z;
    double qaw = angle;
    // now multiply the quaternions
    angle =qaw*qbw - qax*qbx - qay*qby - qaz*qbz ;
    x=qax*qbw + qaw*qbx + qay*qbz - qaz*qby;
    y=qaw*qby - qax*qbz + qay*qbw + qaz*qbx;
    z=qaw*qbz + qax*qby - qay*qbx + qaz*qbw;
    coding=CODING_QUATERNION;
  }
  /** if this rotation is not already coded as axis angle then convert it to    axis angle */
  public void toAxisAngle(){
    if (coding==CODING_AXISANGLE) return;
    double s = Math.sqrt(1-angle*angle);
    if (Math.abs(s) < 0.001 || s != s) s=1;
    angle = 2 * Math.acos(angle);
    x = x / s;
    y = y / s;
    z = z / s;
    coding = CODING_AXISANGLE;
  }
  /** if this rotation is not already coded as quaternion then convert it to quaternion    */
  public void toQuaternion(){
    if (coding==CODING_QUATERNION) return;
    double s = Math.sin(angle/2);
    x = x * s;
    y = y * s;
    z = z * s;
    angle = Math.cos(angle/2);
    coding = CODING_QUATERNION;
  }

  public Rotation get() {
    return new Rotation(x,y,z,angle,coding);
  }

  public void set(Rotation that) {
    x = that.x;
    y = that.y;
    z = that.z;
    angle = that.angle;
    coding = that.coding;
  }

  public vec3 getAxis() {
    toAxisAngle();
    return U(V(x,y,z));
  }
  
  public float getAngle() {
    toAxisAngle();
    return (float)angle;
  }

  public void scale(float s) { // FIXME: make a quaternion version
    toAxisAngle();
    angle *= s;
  }

  public boolean isValid() { // check for NaN
    return !(x != x || y != y || z != z || angle != angle);
  }
}