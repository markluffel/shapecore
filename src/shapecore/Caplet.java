/**
 * 
 */
package shapecore;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

/**
 * Compute several properties for a caplet,
 * particularly the midpoint and midradius,
 * useful for skinning a medial axis
 *
 */
public class Caplet {
  private pt3 center1, center2;
  private float radius1, radius2;
  
  private float d1, d2;
  private float h1, h2;
  
  private pt3 coneCenter1, coneCenter2;
  private pt3 midpoint;
  private vec3 offset;
  private float midpointRadius;

  public Caplet(pt3 center1, float radius1, pt3 center2, float radius2) {
    this.center1 = center1;
    this.radius1 = radius1;
    
    this.center2 = center2;
    this.radius2 = radius2;
    
    calc();
  }
  
  public Caplet(float[] edge) {
    this(edge[0], edge[1], edge[2], edge[3], edge[4], edge[5]);
  }
  
  public Caplet(float[] cr1, float[] cr2) {
    this(cr1[0],cr1[1],cr1[2],cr2[0], cr2[1], cr2[2]);
  }
  
  public Caplet(float cx1, float cy1, float r1, float cx2, float cy2, float r2) {
    center1 = new pt3(cx1, cy1, 0);
    center2 = new pt3(cx2, cy2, 0);
    radius1 = r1;
    radius2 = r2;

    calc();
  }
  
  public void calc() {
    offset = V(center1,center2);
    float distance = offset.norm();
    if(distance != 0) offset.div(distance);
    
    // distance from the sphere centers to the centers of the cone bases
    d1 = (radius1-radius2)*radius1/distance;
    d2 = (radius1-radius2)*radius2/distance;
    
    // distance from the cone base centers to the circle radius at the point
    h1 = sqrt(sq(radius1) - sq(d1));
    h2 = sqrt(sq(radius2) - sq(d2));
    
    coneCenter1 = T(center1, d1, offset);
    coneCenter2 = T(center2, d2, offset);
    midpoint = average(coneCenter1, coneCenter2);
    midpointRadius = lerp(h1,h2,0.5f);
  }
  
  public boolean contains2D(float x, float y) {
    pt here = new pt(x,y);
    pt start = coneCenter1.as2D(), end = coneCenter2.as2D();
    float t = closestArcLengthOnEdge(here, start, end);
    pt p = lerp(start, end, t);
    float perpDist = lerp(h1,h2,t); // width of the cone at this point
    
    return p.dist(here) < perpDist 
    || center1.as2D().dist(here) < radius1
    || center2.as2D().dist(here) < radius2;
  }
  
  /**
   * Surface normal at the point that a ray in the Z direction from this point would hit
   */
  public vec3 normal(float x, float y) {
    // this is painfully 2D
    pt here = new pt(x,y);
    pt start = coneCenter1.as2D(), end = coneCenter2.as2D();
    float t = closestArcLengthOnEdge(here, start, end); // it's wrong to do this in 2D
    if(t == 0) {
      float dsq = sq(x-center1.x)+sq(y-center1.y);
      return new vec3(x-center1.x, y-center1.y, sqrt(sq(radius1)+dsq)).normalize();
      
    } else if(t == 1) {
      float dsq = sq(x-center2.x)+sq(y-center2.y);
      return new vec3(x-center2.x, y-center2.y, sqrt(sq(radius2)+dsq)).normalize();
      
    } else {
      pt3 closest = lerp(coneCenter1, coneCenter1, t);
      float perpDist = lerp(h1,h2,t); // width of the cone at this point
      pt3 normalCenter = T(closest, -(d1*perpDist/h1), offset);
      return null;
      //return V(normalCenter,);
    }
  }
    
  public void draw(Oplet p) {
    p.ellipse(center1.x, center1.y, radius1, radius1);
    p.ellipse(center2.x, center2.y, radius2, radius2);
    vec perp = V(center1,center2).as2D().turnLeft();
    perp.normalize();
    p.beginShape();
    p.vertex(T(coneCenter1.as2D(), h1, perp)); p.vertex(T(coneCenter1.as2D(), -h1, perp));
    p.vertex(T(coneCenter2.as2D(), h2, perp)); p.vertex(T(coneCenter2.as2D(), -h2, perp));
    p.endShape();
  }

  public pt3 getCenter1() {
    return center1;
  }
  public pt3 getCenter2() {
    return center2;
  }
  public float getRadius1() {
    return radius1;
  }
  public float getRadius2() {
    return radius2;
  }
  public pt3 getConeCenter1() {
    return coneCenter1;
  }
  public pt3 getConeCenter2() {
    return coneCenter2;
  }
  public pt3 getMidpoint() {
    return midpoint;
  }
  public vec3 getOffset() {
    return offset;
  }
  public float getMidpointRadius() {
    return midpointRadius;
  }
  public float getH1() {
    return h1;
  }
  public float getH2() {
    return h2;
  }
  public float getD1() {
    return d1;
  }
  public float getD2() {
    return d2;
  }
}