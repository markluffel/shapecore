package shapecore;

import java.util.List;

// lazy implementation, TODO: make this not copy data that it doesn't have to copy
public class SamplablePolyloop extends SamplablePolyline {
  
  public SamplablePolyloop(pt[] pts) {
    this.points = new pt[pts.length+1];
    System.arraycopy(pts, 0, this.points, 0, pts.length);
    this.points[points.length-1] = pts[0];
    recomputeDistances();
  }
  
  public SamplablePolyloop(List<? extends pt> pts) {
    this(pts.toArray(new pt[pts.size()]));
  }
}
