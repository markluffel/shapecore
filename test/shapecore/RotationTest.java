package shapecore;

import static processing.core.PConstants.*;
import static shapecore.Geometry.*;
import shapecore.Rotation;
import shapecore.vec3;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class RotationTest extends TestCase {

  public void testRoundtrip1() {
    Rotation r = new Rotation(0,0,0); // euler
    r.toQuaternion();
    r.toAxisAngle();
    assertNearlyEqual(0, r.angle);
    assertTrue(r.angle == r.angle); // non nan
  }
  
  public void testRoundtrip2() {
    Rotation r = new Rotation(new vec3(0,0,0), Math.PI);
    Rotation q = new Rotation();
    q.set(r);
    q.toAxisAngle(); // q is already axis angle
    assertNearlyEqual(Math.PI, q.angle); // angle preserved (those axis zero)
    assertTrue(q.angle == q.angle); // non nan
  }
  
  public void testRoundtrip3() {
    Rotation r = new Rotation(new vec3(0,0,0), 0);
    Rotation q = new Rotation();
    q.set(r);
    q.toAxisAngle();
    q.toQuaternion();
    q.toAxisAngle();
    assertNearlyEqual(0, q.angle);
    assertTrue(q.angle == q.angle); // non nan
  }
  
  public void testRoundtrip4() {
    Rotation r = new Rotation(new vec3(0,0,0), Math.PI);
    Rotation q = new Rotation();
    r.set(q);
    r.toAxisAngle();
    assertNearlyEqual(0, r.angle);
    assertTrue(r.angle == r.angle); // non nan
  }

  public void testRoundtrip5() { // double conversion is safe
    Rotation r = new Rotation(new vec3(0,0,0), 0);
    r.toAxisAngle();
    r.toAxisAngle();
    assertNearlyEqual(0, r.angle);
    assertTrue(r.angle == r.angle); // non nan
  }
  
  public void testRoundtrip6() { // double conversion is safe
    Rotation r = new Rotation(new vec3(0,0,0), 0);
    r.toAxisAngle();
    r.toQuaternion();
    r.toQuaternion();
    r.toAxisAngle();
    assertNearlyEqual(0, r.angle);
    assertTrue(r.angle == r.angle); // non nan
  }
  
  public void testCombine() {
    vec3 v = randomUnitVector();
    Rotation a = Rotation.axisAngle(randomUnitVector(), Math.random()*TWO_PI);
    Rotation b = Rotation.axisAngle(randomUnitVector(), Math.random()*TWO_PI);
    vec3 v1 = a.transform(b.transform(v));
    a.combine(b);
    vec3 v2 = a.transform(v);
    assertNearlyEqual(v1,v2);
  }
  
  vec3 randomUnitVector() {
    return U(V(Math.random()*2-1,Math.random()*2-1,Math.random()*2-1));
  }
  
  void assertNearlyEqual(vec3 expected, vec3 actual) {
    assertNearlyEqual(expected.x, actual.x);
    assertNearlyEqual(expected.y, actual.y);
    assertNearlyEqual(expected.z, actual.z);
  }
  
  void assertNearlyEqual(double expected, double actual) {
    if(Math.abs(expected-actual) > 1e-6) {
      throw new AssertionFailedError("expected: "+expected+" got: "+actual+" (not equal enough)");
    }
  }
}
