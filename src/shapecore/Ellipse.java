package shapecore;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

public class Ellipse {
  
  public static float otherAxisWidth(pt near, pt far, pt wide) {
    pt center = average(near, far);
    vec axis1 = new vec(center, far);
    float len1 = axis1.norm();
    
    vec off = new vec(center, wide);
    float offLen = off.norm();
    float theta = acos(axis1.dot(off)/(len1*offLen));
    return 2*offLen*sin(theta)/sqrt(1-sq(offLen*cos(theta)/len1));    
  }
  
  public static float offFocusWidth(pt f1, pt f2, pt wide) {
    return sqrt(sq(f1.dist(wide)+f2.dist(wide)) - f1.sqdist(f2) );    
  }
}
