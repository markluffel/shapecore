package shapecore;


import static shapecore.Geometry.*;

public class Edge3 {
  public pt3 start,end;

  public Edge3(pt3 start, pt3 end) {
    this.start = start;
    this.end = end;
  }
  
  public vec3 dir() {
    return V(start,end);
  }
  
  /**
   * The the pair of points on this edge and that edge that have the minimum distance apart.
   * The first point in the result array is on this edge, the second is on the passed edge. 
   */
  public pt3[] nearest(Edge3 that) {
    pt3 p = this.start, q = that.start;
    vec3 u = this.dir(), v = that.dir();
    vec3 w = V(q,p);// P(p.x-q.x, p.y-q.y, p.z-q.z);
      
    float
    a = dot(u,u), b = dot(u,v), c = dot(v,v), d = dot(u,w), e = dot(v, w),
    s = (b*e - c*d)/(a*c - b*b), // on this
    t = (a*e - b*d)/(a*c - b*b); // on that
    if(s > 0 && s < 1 && t > 0 && t < 1) {
      return new pt3[]{T(p, s, u), T(q, t, v)};
    } else {
      float minD = Float.MAX_VALUE;
      Proj best = null;
      for(Proj proj : new Proj[]{
        that.proj(this.start, false),
        that.proj(this.end, false),
        this.proj(that.start, true),
        this.proj(that.end, true)
      }) {
        if(proj.sqdist < minD) {
          minD = proj.sqdist;
          best = proj;
        }
      }
      if(best != null) {
        return new pt3[] {best.src, best.dst};
      } else { // in the event of NaNs, I assume
        throw new IllegalArgumentException();
      }
    }
  }

  private Proj proj(pt3 here, boolean flip) {
    pt3 p = projectOnto(here, this);
    if(flip) {
      return new Proj(p, here);
    } else {
      return new Proj(here, p);
    }
  }
  
  private static final class Proj {
    private Proj(pt3 src, pt3 dst) {
      this.src = src;
      this.dst = dst;
      this.sqdist = src.sqdist(dst);
    }
    private pt3 src,dst;
    private float sqdist;
  }
}
