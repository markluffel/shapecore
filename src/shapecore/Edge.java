package shapecore;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.List;

import shapecore.tuple.IntPair;

public class Edge {
  public pt a, b;
  
  public Edge(pt a, pt b) {
    this.a = a; 
    this.b = b;
  }
  
  public vec dir() { return V(a,b); }
  public vec right() { return dir().turnRight(); }
  public vec left() { return dir().turnLeft(); }
  public float lengthSq() { return dot(dir(),dir()); }
  public float length() { return d(a,b); }
  public pt midpoint() { return average(a,b); }
  
  public pt put(pt C) {
    return T(this.a, C.x,dir(), C.y,R(dir())); 
  }

  /** new spiral center */
  public static pt spiralCenter(pt p0, pt q0, pt p1, pt q1) {
    vec ab = V(p0,q0),
        cd = V(p1,q1),
        ac = V(p0,p1);
    
    float
      m = d(p1,q1)/d(p0,q0),
      n = d(p1,q1)*d(p0,q0),
      c = dot(ab,cd)/n,
      s = dot(R(ab),cd)/n,
      abab = dot(ab,ab),
      angle = dot(ab,ac) / abab,
      b = dot(R(ab), ac) / abab,
      x = (angle - m*(angle*c + b*s)),
      y = (b-m*(-angle*s+b*c)),
      d = 1+m*(m-2*c);
    
    if(c != 1 && m != 1) {
      x /= d;
      y /= d;
    }
    return T(T(p0,x,ab),y,R(ab));
  }

  public static Edge Sspiral(Edge E0, float t, Edge E1) {
    float a = a(E0.dir(),E1.dir());
    float s = E1.length() / E0.length();
    float u = pow(s,t); //u=t;
    pt G = spiralCenter(E0.a,E0.b,E1.a,E1.b);
    pt M = T(G,u,R(E0.a,t*a,G)); 
    pt N = T(G,u,R(E0.b,t*a,G));  
    return E(M,N);
  }
  
  public boolean intersects(Edge that) {
    return intersection(that) != null;
  }
  public pt intersection(Edge that) {
    return Geometry.edgeIntersection(this.a, this.b, that.a, that.b);
  }
  public pt intersection(Ray that) {
    return Geometry.edgeRayIntersection(this.a, this.b, that.start, that.dir);
  }
  public pt intersection(Line that) {
    return Geometry.lineEdgeIntersection(that.on, that.dir, this.a, this.b);
  }

  public pt projection(pt q) {
    return closestPointOnEdge(q, a, b);
  }

  public List<IntPair> rasterize() {
    List<IntPair> result = new ArrayList<IntPair>();
    
    float dx = b.x-a.x, dy = b.y-a.y;  
    float length = max(abs(dx),abs(dy));
    if(length == 0) return result; // avoid divide by zero
    // put increments into range: [-1, 1]
    float xinc = dx/length, yinc = dy/length;
    
    float x = a.x, y = a.y;
    for(int t = 0; t <= length; t++) {
      result.add(new IntPair(round(x),round(y)));
      x += xinc;
      y += yinc;
    }
    return result;
  }
}