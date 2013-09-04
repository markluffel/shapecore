package shapecore;

import static shapecore.Oplet.*;

public class BoundingBox3 {

  pt3 lo,hi;
  public BoundingBox3(pt3[] pts) {
    lo = new pt3( Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    hi = new pt3(-Float.MAX_VALUE,-Float.MAX_VALUE,-Float.MAX_VALUE);
    for(pt3 p : pts) {
      lo.x = min(lo.x, p.x); hi.x = max(hi.x, p.x);
      lo.y = min(lo.y, p.y); hi.y = max(hi.y, p.y);
      lo.z = min(lo.z, p.z); hi.z = max(hi.z, p.z);
    }
  }
  
  public pt3 center() {
    return new pt3((lo.x+hi.x)/2, (lo.y+hi.y)/2, (lo.z+hi.z)/2);
  }
}
