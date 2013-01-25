package shapecore;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

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
  public pt midpoint() { return A(a,b); }
  
  public pt put(pt C) {
    return T(this.a, C.x,dir(), C.y,R(dir())); 
  }

  // For the spirals:
  public static pt spiralCenter(pt A, pt B, pt C, pt D) {         // new spiral center
    float m=d(C,D)/d(A,B); float n=d(C,D)*d(A,B);
    vec AB=V(A,B);   vec CD=V(C,D); vec AC=V(A,C);
    float c=dot(AB,CD)/n;  float s=dot(R(AB),CD)/n;
    float AB2 = dot(AB,AB);  float a=dot(AB,AC)/AB2;  float b=dot(R(AB),AC)/AB2;
    float x=(a-m*( a*c+b*s)); float y=(b-m*(-a*s+b*c));
    float d=1+m*(m-2*c);  if((c!=1)&&(m!=1)) { x/=d; y/=d; };
    return T(T(A,x,AB),y,R(AB));
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

  public pt projection(pt q) {
    return closestPointOnEdge(q, a, b);
  }
}