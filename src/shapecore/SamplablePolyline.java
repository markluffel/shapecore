package shapecore;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.Arrays;
import java.util.List;

public class SamplablePolyline {

  public pt[] points;
  float[] distances; // prefix sum of the arc length at each point
  
  protected SamplablePolyline() {}
  
  public SamplablePolyline(pt[] pts) {
    this.points = pts;
    recomputeDistances();
  }
  
  public SamplablePolyline(List<? extends pt> pts) {
    this(pts.toArray(new pt[pts.size()]));
  }
  
  public float getLength() {
    return distances[distances.length-1];
  }

  int sampleIndex(float t) {
    return sampleIndexByDistance(t*getLength());
  }
  
  int sampleIndexByDistance(float distance) {
    float length = getLength();
    
    if(distance <= 0) return 0;
    if(distance >= length) return points.length-1;
    
    int left = Arrays.binarySearch(distances, distance);
    
    if(left > 0) {
      return left; // exactly on a point
    } else {
      return -left-2; // between two points, return the index of the prior point
    }
  }
  
  /**
   * 
   * @param t a value between
   * @return
   */
  public pt sample(float t) {
    return sampleByDistance(t*getLength());
  }
  
  void recomputeDistances() {
    distances = new float[points.length];
    distances[0] = 0;
    for(int i = 1; i < distances.length; i++) {
      distances[i] = distances[i-1] + points[i].dist(points[i-1]);
    }
  }
  
  public pt sampleByDistance(float distance) {
    if(distance < 0) distance = getLength()+distance; // allow negative indexes
    if(distance >= getLength()) return points[points.length-1].get(); // if it's too big, clamp it
    
    int left = sampleIndexByDistance(distance);
    int right = left+1;
    if(right > points.length) return points[left].get(); // at endpoint 
    float lDist = distances[left], rDist = distances[right];
    pt lPoint = points[left], rPoint = points[right];
    
    if(rDist == lDist) return lPoint.get(); //  for coincident points
    float localT = (distance-lDist)/(rDist-lDist);
    
    return Oplet.lerp(lPoint, rPoint, localT);
  }
  
  // TODO: we can make this lookup faster with some other kind of acceleration structure
  // it's about time that i implemented a nice spatial tree
  public pt localCoord(pt a) {
    float minDist = Float.MAX_VALUE;
    float perp = 0;
    float bestLenSum = 0; // the arclength at the closest point to A on the polyline
    // 
    for(int i = 1; i < points.length; i++) {
      pt p = points[i-1];
      pt q = points[i];
      pt closest = closestPointOnEdge(a, p, q);
      vec axis = R(V(p,q)); axis.normalize();
      // V(closest, A) should be close to parallel to axis
      // this tells us which side, and how far
      float proj = V(closest,a).dot(axis);
      float d = d2(closest,a);
      if(d < minDist) {
        minDist = d;
        perp = proj;
        
        // everything up to this point, plus the bit along this edge
        // we'll use this to find the y value
        bestLenSum = distances[i-1]+d(p,closest);
      }
    }
    if(minDist < Float.MAX_VALUE) {
      // x is distance from the spine,
      // y contains four ranges:
      //    -pi,0 (clockwise angle relative to base of the head endcap)
      //    0,1 (normalized arclength from head to tail, clockwise)
      //    1,pi+1 (clockwise angle relative to base of the tail endcap)
      //    pi+1,pi+1 (normalized arclength from the tail to head, clockwise)
      float y = bestLenSum/distances[distances.length-1];
      if(y < 0.001) {
        // endcap at head
        vec base = V(points[0],points[1]);
        base.turnRight();
        vec offset = V(points[0],a);
        float theta = acos(base.dot(offset)/(base.norm()*offset.norm()));
        y = theta-PI;
      } else if(y > 0.99999) {
        // endcap at tail
        vec base = V(points[points.length-1],points[points.length-2]);
        base.turnRight();
        vec offset = V(points[points.length-1],a);
        float theta = acos(base.dot(offset)/(base.norm()*offset.norm()));
        y = theta+1;
      } else {
        // along the center, need to pick value of y based on sign of x
        if(perp < 0) {
          y = PI+2-y;
        }
      }
      return new pt(sqrt(minDist), y);
    }
    return null;
  }
  
  public pt globalCoord(pt a) {
    return globalCoord(a.x, a.y);
  }
  
  // this is a bit uncomfortably complex
  public pt globalCoord(float Ax, float Ay) {
    if(Ay < 0) {
      // first range
      float theta = PI+Ay;
      vec base = points[0].to(points[1]);
      base.turnRight();
      return radial(points[0], Ax, base.angle()-theta);
      
    } else if(Ay < PI+1 && Ay > 1) {
      // third range
      float theta = Ay-1;
      vec base = points[points.length-1].to(points[points.length-2]);
      base.turnRight();
      return radial(points[points.length-1], Ax, base.angle()-theta);
      
    } else {
      float y,x;
      if(Ay > 1) {
        // fourth range 
        y = PI+2-Ay;
        x = -Ax;
      } else {
        // second range
        y = Ay;
        x = Ax;
      }
    
      // y value is distance along spine
      int left = sampleIndex(y);
      int right = left+1;
      if(right >= distances.length) {
        left--; right--; // lame!
      }
        
      float lDist = distances[left], rDist = distances[right];
      pt lPoint = points[left], rPoint = points[right];
      
      float distance = distances[distances.length-1]*y;
      float localT = (distance-lDist)/(rDist-lDist);
      
      pt M = Oplet.lerp(lPoint, rPoint, localT);
      // the axis is the segment that we're on
      vec axis = R(V(lPoint,rPoint)); axis.normalize();
      // x value is signed distance away from spine
      return T(M, x, axis); 
    }
  }
  
  private static pt radial(pt center, float r, float theta) {
    return T(center, radial(r,theta));
  }
  private static vec radial(float r, float theta) {
    return new vec(r*cos(theta), r*sin(theta));
  }
}
