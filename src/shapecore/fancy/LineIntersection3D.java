package shapecore.fancy;

import shapecore.Oplet;
import shapecore.pt3;
import shapecore.vec3;
import static shapecore.Geometry.*;

public class LineIntersection3D extends Oplet {

  pt3 start1, end1, start2, end2;
  
  public void draw() {
    background(0);
    
    translate(width/2, height/2);
    rotateY(frameCount*0.01f);
    translate(-width/2, -height/2);
    
    strokeWeight(3);
    end2.x = mouseX;
    end2.y = mouseY;
    
    stroke(255);
    line(start1, end1);
    line(start2, end2);
    /*
    stroke(200);
    line(start1, start2);
    line(end1, end2);
    line(start1, end2);
    line(start2, end1);
    */
    //pt3[] near = nearest(start1,V(start1,end1),start2,V(start2,end2));
    pt3[] near = nearest(start1,end1,start2,end2);
    
    stroke(255,255,0);
    line(near[0],near[1]);
  }
  
  float EPS = Float.MIN_VALUE*4;
  
  //pt3[] nearest(pt3 start1, vec3 dir1, pt3 start2, vec3 dir2) {
  
  pt3[] nearest(pt3 start1, pt3 end1, pt3 start2, pt3 end2) {
    
    vec3
    p13 = V(start2,start1),
    p43 = V(start2,end2);
    
    float d1343,d4321,d1321,d4343,d2121;
    float numer,denom;
    
    if(abs(p43.x) < EPS && abs(p43.y) < EPS && abs(p43.z) < EPS) {
       return new pt3[0]; // fail
    }
    vec3 p21 = V(start1,end1);
    
    if(abs(p21.x) < EPS && abs(p21.y) < EPS && abs(p21.z) < EPS) {
      return new pt3[0]; // fail
    }
    
    d1343 = p13.x * p43.x + p13.y * p43.y + p13.z * p43.z;
    d4321 = p43.x * p21.x + p43.y * p21.y + p43.z * p21.z;
    d1321 = p13.x * p21.x + p13.y * p21.y + p13.z * p21.z;
    d4343 = p43.x * p43.x + p43.y * p43.y + p43.z * p43.z;
    d2121 = p21.x * p21.x + p21.y * p21.y + p21.z * p21.z;

    denom = d2121 * d4343 - d4321 * d4321;
    if(abs(denom) < EPS) {
      return new pt3[0];
    }
    numer = d1343 * d4321 - d1321 * d4343;

    // parameterized distance from start
    float mua = numer / denom;
    float mub = (d1343 + d4321 * mua) / d4343;
    
    pt3 pa = T(start1, mua, p21);
    pt3 pb = T(start2, mub, p43);
    
    return new pt3[]{pa,pb};
  }
  
  public void setup() {
    size(640,480, P3D);
    start1 = new pt3(50,50,0);
    end1 = new pt3(200,400,100);
    start2 = new pt3(400,60,100);
    end2 = new pt3(350, 400, 0);
  }
}
