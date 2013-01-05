package shapecore;

import static shapecore.Geometry.*;


import java.applet.AppletContext;
import java.awt.FileDialog;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.TreeSet;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Jama.Matrix;


import megamu.mesh.Delaunay;
import megamu.mesh.InteriorTest;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix3D;
import processing.opengl.PGraphicsOpenGL;
import shapecore.interact.ArcBall;
import shapecore.interact.CameraInterface;
import shapecore.interfaces.PointAnimator;
import shapecore.iterators.CircularPairIterator;
import shapecore.iterators.PairIterator;
import shapecore.mesh.CornerTable;
import shapecore.motion.Spiral;
import shapecore.motion.Trajectory;
import shapecore.tuple.Pair;
import sun.applet.AppletViewer;

public class Oplet extends PApplet {
  // for access to static methods with less typing
  Geometry Geo = new Geometry();
  MatrixUtils Mat = new MatrixUtils();

  public void init() {
    super.init();
    try {
      AppletContext ctxt = getAppletContext();
      // special case for eclipse
      if(ctxt instanceof AppletViewer) {
        online = false;
      }
    } catch(NullPointerException e) {
    } catch(AccessControlException e) {
      // apprently doing this check can be a no-no in certain jvm/plugin situations
    }
  }
  
  public void dotted_line(pt3 a, pt3 b) {
    float step = max(0.02f/a.dist(b),0.00001f);
    for(float t = 0; t < 1; t += step*2) {
      line(lerp(a,b,t), lerp(a,b,t+step));
    }
  }
  public void line(pt A, pt B) {
    line(A.x, A.y, B.x, B.y);
  }
  public void line(pt3 A, pt3 B) {
    line(A.x, A.y, A.z, B.x, B.y, B.z);
  }
  public void line(pt p, vec v) {
    line(p,T(p,v));
  }
  public void line(pt3 p, vec3 v) {
    line(p,T(p,v));
  }
  
  // *************************************************************
  // **** 2D GEOMETRY CLASSES AND UTILITIES, Jarek Rossignac *****
  // **** REVISED October 3, 2008 *****
  // *************************************************************

  public void translate(vec V) { translate(V.x, V.y); }
  public void translate(pt P) { translate(P.x, P.y); }
  public void untranslate(pt P) { translate(-P.x, -P.y); }
  public void translate(vec3 V) { translate(V.x, V.y, V.z); }
  public void translate(pt3 P) { translate(P.x, P.y, P.z); }
  

  public void rotate(float a, pt P) {
    translate(P);
    rotate(a);
    untranslate(P);
  } // rotates view by a around point startCurve

  public void scale(float s, pt P) {
    translate(P);
    scale(s);
    untranslate(P);
  } // scales view wrt point startCurve
  
  public void rotate(Rotation r) {
    r.toAxisAngle();
    if(r.angle != 0) {
      rotate((float)r.angle, (float)r.x, (float)r.y, (float)r.z);
    }
  }

  public pt screenCenter() {
    return (new pt(height / 2, height / 2));
  } // returns point at center of screen

  public boolean mouseIsInWindow() {
    return (((mouseX > 0) && (mouseX < height) && (mouseY > 0) && (mouseY < height)));
  } // returns true if mouse is in window

  public pt mouse() {
    return new pt(mouseX, mouseY);
  } // returns point with current mouse location

  public pt pMouse() {
    return new pt(pmouseX, pmouseY);
  } // returns point with previous mouse location

  public vec MouseDrag() {
    return new vec(mouseX - pmouseX, mouseY - pmouseY);
  } // vector representing recent mouse displacement

  public float angleTracedWithMouse() {
    vec V1 = U(screenCenter(), pMouse());
    vec V2 = U(screenCenter(), mouse());
    return a(V1, V2);
  }

  public float scaleTracedWithMouse() {
    float d1 = d(screenCenter(), pMouse());
    float d2 = d(screenCenter(), mouse());
    return d2 / d1;
  }
  
  
  
  
  public void draw(PointAnimator a, pt pt) {
    draw(a,pt,0.1f);
  }
  
  public void draw(PointAnimator a, pt pt, float step) {
    beginShape();
    for(float t = 0; t < 1; t += step) {
      vertex(a.apply(pt,t));
    }
    vertex(a.apply(pt,1));
    endShape();
  }
  
  public void draw(PointAnimator a, pt pt, float step, float end) {
    beginShape();
    float start = 0;
    if(end < start) {
      float temp = end; end = start; start = temp;
    }
    step = abs(step);
    for(float t = start; t < end; t += step) {
      vertex(a.apply(pt,t));
    }
    vertex(a.apply(pt,end));
    endShape();
  }
  
  
  

  // FIXME: dist is define in PApplet,
  // so anyone subclassing PApplet and "import static core.Geometry" can't see core.Geometry's definition
  public static float dist(pt P, pt Q) {
    return PApplet.dist(P.x, P.y, Q.x, Q.y);
  } // ||AB||
  
  public static float dist(pt3 P, pt3 Q) {
    return PApplet.dist(P.x, P.y, P.z, Q.x, Q.y, Q.z);
  } // ||AB||                                          
  
  // display

  void v(pt P) {
    vertex(P.x, P.y);
  } // next point when drawing polygons between beginShape(); and endShape();

  void showCross(pt P, float r) {
    line(P.x - r, P.y, P.x + r, P.y);
    line(P.x, P.y - r, P.x, P.y + r);
  } // shows startCurve as cross of length rotation

  void showCross(pt P) {
    showCross(P, 2);
  } // shows startCurve as small cross

  void show(pt P, float r) {
    ellipse(P.x, P.y, 2 * r, 2 * r);
  } // draws circle of center rotation around point

  void show(pt P) {
    ellipse(P.x, P.y, 4, 4);
  } // draws small circle around point

  void show(pt P, pt Q) {
    line(P.x, P.y, Q.x, Q.y);
  } // draws edge (startCurve,end)

  void show(pt P, vec V) {
    line(P.x, P.y, P.x + V.x, P.y + V.y);
  } // show line from startCurve along V

  void show(pt P, float s, vec V) {
    show(P, S(s, V));
  } // show line from startCurve along sV

  public void arrow(pt P, pt Q) { arrow(P, V(P, Q)); }

  public void arrow(pt P, float s, vec V) {
    arrow(P, S(s, V));
  } // show arrow from startCurve along sV

  public void arrow(pt P, vec V) {
    show(P, V);
    float n = V.norm();
    float s = max(min(0.2f, 20f / n), 6f / n); // show arrow from startCurve along V
    pt Q = T(P, V);
    vec U = S(-s, V);
    vec W = R(S(0.3f, U));
    beginShape();
    v(T(T(Q, U), W));
    v(Q);
    v(T(T(Q, U), -1, W));
    endShape(CLOSE);
  }
  
  
  public void arrow(pt3 P, float s, vec3 V) {
    V = V.get().scaleBy(s);
    pt3 head = T(P,V);
    pt3 neck = T(P, 0.9f, V);
    line(P, head);
    //vec3 O = new vec3(1,1,(V.x+V.y)/-V.z);
    vec3 O = new vec3(random(-1,1),random(-1,1),random(-1,1));
    O.normalize();
    vec3 Vn = V.normalized();
    vec3 U = O.cross(Vn);
    vec3 W = U.cross(Vn);
    float r = V.mag()/10;
    int numSteps = 5;
    float step = TWO_PI/numSteps;
    pt3 here,next = T(neck, r*cos(0), U, r*sin(0), W);
    for(float i = 0; i < numSteps+1; i++) {
      float theta = i*numSteps;
      here = next;
      next = T(neck, r*cos(theta+step), U, r*sin(theta+step), W);
      
      line(neck, here);
      line(here, head);
      line(here, next);
    }
  }
  
  public void arrowhead(pt pt, vec tang, float size) {
    vec head = R(tang);
    polygon(
      T(pt, 8*size, tang),
      T(pt,  7*size, head, -4*size, tang),
      T(pt, -7*size, head, -4*size, tang)
    );
  }

  // ************************************************************************
  // **** ANGLES
  // ************************************************************************
  public static float angle(vec V) {
    return atan2(V.y, V.x);
  }

  public static float angle(vec U, vec V) {
    return atan2(dot(R(U), V), dot(U, V));
  }
  
  public static float angle(pt a, pt b) {
    return angle(V(a,b));
  }
  
  public static float angle(pt a, pt b, pt c) {
    return angle(V(a,b),V(b,c));
  }

  static float mPItoPIangle(float a) {
    if (a > PI)
      return mPItoPIangle(a - 2 * PI);
    if (a < -PI)
      return mPItoPIangle(a + 2 * PI);
    return (a);
  }

  float toDeg(float a) {
    return a * 180 / PI;
  }

  float toRad(float a) {
    return a * PI / 180;
  }

  // ************************************************************************
  // **** VECTORS
  // ************************************************************************

  // fitting
    vec vecToCubic(pt A, pt B, pt C, pt D, pt E) {
    return V((-A.x + 4 * B.x - 6 * C.x + 4 * D.x - E.x) / 6, (-A.y + 4 * B.y
        - 6 * C.y + 4 * D.y - E.y) / 6);
  }
//---- biLaplace fit
  vec fitVec (pt B, pt C, pt D) { return A(V(C,B),V(C,D)); }
  pt fitPt (pt B, pt C, pt D) {return A(B,D);};  
  pt fitPt (pt B, pt C, pt D, float s) {return T(C,s,fitVec(B,C,D));};  
  pt fitPt(pt A, pt B, pt C, pt D, pt E, float s) {pt PB = fitPt(A,B,C,s); pt PC = fitPt(B,C,D,s);  pt PD = fitPt(C,D,E,s); return fitPt(PB,PC,PD,-s);}
  pt fitPt(pt A, pt B, pt C, pt D, pt E) {float s=sqrt(2f/3f); pt PB = fitPt(A,B,C,s); pt PC = fitPt(B,C,D,s);  pt PD = fitPt(C,D,E,s); return fitPt(PB,PC,PD,-s);}
  
  //---- proportional biLaplace fit
  vec proVec (pt B, pt C, pt D) { return L(V(C,B), d(C,B)/(d(C,B)+d(C,D)),V(C,D)); }
  pt proPt (pt B, pt C, pt D) {return T(B,d(C,B)/(d(C,B)+d(C,D)),V(B,D));};  
  pt proPt (pt B, pt C, pt D, float s) {return T(C,s,proVec(B,C,D));};  
  pt proPt(pt A, pt B, pt C, pt D, pt E, float s) {pt PB = proPt(A,B,C,s); pt PC = proPt(B,C,D,s);  pt PD = proPt(C,D,E,s); return proPt(PB,PC,PD,-s);}
  pt proPt(pt A, pt B, pt C, pt D, pt E) {float s=sqrt(2f/3f); pt PB = proPt(A,B,C,s); pt PC = proPt(B,C,D,s);  pt PD = proPt(C,D,E,s); return proPt(PB,PC,PD,-s);}


  // CURVES
  void drawCubicBezier(pt A, pt B, pt C, pt D) {
    bezier(A.x, A.y, B.x, B.y, C.x, C.y, D.x, D.y);
  }

  //returns a tucked B towards its neighbors
  static pt B(pt A, pt B, pt C, float s) {
    return (L(L(B, s / 4f, A), 0.5f, L(B, s / 4f, C)));
  }

  //returns a bulged mid-edge point
  static pt F(pt A, pt B, pt C, pt D, float s) {
    return (L(L(A, 1f + (1f - s) / 8f, B), 0.5f, L(D, 1f + (1f - s) / 8f, C)));
  }

  static pt limit(pt A, pt B, pt C, pt D, pt E, float s, int r) {
    if (r == 0)
      return C.clone();
    else
      return limit(B(A, B, C, s), F(A, B, C, D, s), B(B, C, D, s), F(B, C, D,
          E, s), B(C, D, E, s), s, r - 1);
  }

  void scribe(String S) {
    fill(JarekUtils.black);
    text(S, 20, 20);
    noFill();
  }

  void scribe(String S, int i) {
    fill(JarekUtils.black);
    text(S, 20, 20 + i * 20);
    noFill();
  }

  void scribe(String S, int i, int j) {
    fill(JarekUtils.black);
    text(S, width - 10 * j, 20 + i * 20);
    noFill();
  }
  
  public void vertex(pt p) {
    vertex(p.x, p.y);
  }
  public void vertex(pt loc, pt uv) {
    vertex(loc.x, loc.y, uv.x, uv.y);
  }
  public void vertex(pt3 p) {
    vertex(p.x, p.y, p.z);
  }
  public void vertex(pt3 p, pt uv) {
    vertex(p.x, p.y, p.z, uv.x, uv.y);
  }
  public void vertex2(pt3 p) {
    vertex(p.x, p.y);
  }
  
  public void normal(vec3 p) {
    normal(p.x, p.y, p.z);
  }

  public static pt lerp(pt A, pt B, float t) {
    return new pt(lerp(A.x, B.x, t), lerp(A.y, B.y, t));
  }
  
  public static pt3 lerp(pt3 A, pt3 B, float t) {
    return new pt3(lerp(A.x, B.x, t), lerp(A.y, B.y, t), lerp(A.z, B.z, t));
  }
  
  public static float geometricLerp(float a, float b, float t) {
    return exp(lerp(log(a), log(b), t));
  }
  
  //from the processing core.jar
  public static float _bezierPoint(float a, float b, float c, float d, float t) {
    float t1 = 1.0f - t;
    return a*t1*t1*t1 + 3*b*t*t1*t1 + 3*c*t*t*t1 + d*t*t*t;
  }
  
  public static float _bezierTangent(float a, float b, float c, float d, float t) {
    return (3*t*t * (-a+3*b-3*c+d) +
            6*t * (a-2*b+c) +
            3 * (-a+b));
  }

    
  public static pt bezierPoint(pt a, pt b, pt c, pt d, float t) {
    return new pt(
        _bezierPoint(a.x, b.x, c.x, d.x, t),
        _bezierPoint(a.y, b.y, c.y, d.y, t)
    );
  }
  
  public static vec bezierTangent(pt a, pt b, pt c, pt d, float t) {
    return new vec(
        _bezierTangent(a.x, b.x, c.x, d.x, t),
        _bezierTangent(a.y, b.y, c.y, d.y, t)
    );
  }
  
  public void bezierVertex(pt a, pt b, pt c) {
    bezierVertex(a.x, a.y, b.x, b.y, c.x, c.y);
  }

  public void bezier(pt a, pt b, pt c, pt d) {
    bezier(a.x, a.y, b.x, b.y, c.x, c.y, d.x, d.y);
  }
  
  public void bezier(pt a, vec aDir, vec bDir, pt b) {
    bezier(a.x, a.y, a.x+aDir.x, a.y+aDir.y, b.x-bDir.x, b.y-bDir.y, b.x, b.y);
  }
  
  public void setToMouse(pt p) { p.x = mouseX; p.y = mouseY; }; 
  public void moveWithMouse(pt p) { p.x += mouseX-pmouseX; p.y += mouseY-pmouseY; }
  public void clipToWindow(pt p) {p.x=max(p.x,0); p.y=max(p.y,0); p.x=min(p.x,height); p.y=min(p.y,height); }
  public vec makeVecToCenter(pt p) {return(new vec(p.x-height/2f,p.y-height/2f)); }
  public vec makeVecToMouse(pt p) {return(new vec(mouseX-p.x,mouseY-p.y)); }

  void showLabel(pt p, String s, vec D) {text(s, p.x+D.x-5,p.y+D.y+4);  };  // show string displaced by vector D from point
  void showLabel(pt p, String s) {text(s, p.x+5, p.y+4);  };
  void showLabel(pt p, int i) {text(str(i), p.x+5, p.y+4);  };  // shows integer number next to point
  void showLabel(pt p, String s, float u, float v) {text(s, p.x+u, p.y+v);  }

  public void draw(pt[] points) {
    draw(points, OPEN);
  }

  public void draw(pt[] points, int closeFlag) {
    beginShape();
    for(pt p : points) {
      vertex(p);
    }
    endShape(closeFlag);
  }

  public void draw(List<? extends pt> points) {
    draw(points, false);
  }
  
  public void draw(List<? extends pt> points, boolean close) {
    if(close) {
      draw(points, CLOSE);
    } else {
      draw(points, OPEN);
    }
  }
  
  public void draw(List<? extends pt> points, int closeFlag) {
    beginShape();
    for(pt p : points) {
      vertex(p);
    }
    endShape(closeFlag);
  }
  
  public void draw(pt3[] points) {
    draw(points, OPEN);
  }

  public void draw(pt3[] points, int closeFlag) {
    beginShape();
    for(pt3 p : points) {
      vertex(p);
    }
    endShape(closeFlag);
  }
  
  public void draw(Delaunay d, pt[] points) {
    int[][] faces = d.getFaces();
    beginShape(PConstants.TRIANGLES);
    for(int i = 0; i < faces.length; i++) {
      int[] face = faces[i];
      vertex(points[face[0]]);
      vertex(points[face[1]]);
      vertex(points[face[2]]);        
    }
    endShape();
  }
  
  public void draw(Delaunay d, float[][] points) {
    int[][] faces = d.getFaces();
    beginShape(PConstants.TRIANGLES);
    float[] p;
    for(int i = 0; i < faces.length; i++) {
      int[] face = faces[i];
      if(face[0] < points.length && face[1] < points.length && face[2] < points.length) {
        p = points[face[0]]; vertex(p[0],p[1]);
        p = points[face[1]]; vertex(p[0],p[1]);
        p = points[face[2]]; vertex(p[0],p[1]);
      }
    }
    endShape();
  }

  public void jitter(pt p, float n) {
    p.x += random(-n, n);
    p.y += random(-n, n);      
  }

  /**
   * Convert an open curve in Euclidean coordinates into local Laplacian coordinates
   * 
   * @param globals
   * @return
   */
  public static List<vec> globalToLocal(List<pt> globals) {
    return globalToLocal(globals, 1);
  }
  
  public static List<vec> globalToLocal(List<pt> globals, int span) {
    List<vec> result = new ArrayList<vec>();
    int endex = globals.size()-span;
    for(int i = span; i < endex; i++) {
      vec ll = localLaplace(globals.get(i-span), globals.get(i), globals.get(i+span));
      result.add(ll);
    }
    return result;
  }
  
  /**
   * Convert an open curve in Euclidean coordinates into local Laplacian coordinates
   * 
   * @param globals
   * @return
   */
  public static List<vec> globalToLocal(pt[] globals) {
    List<vec> result = new ArrayList<vec>();
    int endex = globals.length-1;
    for(int i = 1; i < endex; i++) {
      vec ll = localLaplace(globals[i-1], globals[i], globals[i+1]);
      result.add(ll);
    }
    return result;
  }
  
  public static void smoothPolygon(List<? extends pt> pts, float t) {
    // TODO: make this a single pass, with a small buffer with relative indexes
    t /= 2;
    
    // first pass to
    List<vec> updated = new ArrayList<vec>();
    int i = pts.size()-1, j = 0, k = 1;
    do {
      pt
      A = pts.get(i),
      B = pts.get(j),
      C = pts.get(k);
      vec BA = V(B,A), BC = V(B,C);
      updated.add(S(t, S(BA, BC)));
      
      i = j; j = k; k = (k+1)%pts.size();
    } while(i < j);
    
    // second pass to add the offset vectors
    for(i = 0; i < pts.size(); i++) {
      pts.get(i).add(updated.get(i));
    }
  }

  public static void smoothPolygon(pt[] pts, float t) {
    smoothPolygon(Arrays.asList(pts), t);
  }

  public static void smoothPolyline(List<? extends pt> pts, double t) {
    smoothPolyline(pts, (float)t);
  }
  
  public static void smoothPolyline(List<? extends pt> pts, float t) {
    // this needs to be optimized in all kinds of ways
    t /= 2;
    int endex = pts.size()-1;
    List<vec> change = new ArrayList<vec>();
    change.add(new vec(0,0));
    for(int i = 1; i < endex; i++) {
      pt
      A = pts.get(i-1),
      B = pts.get(i),
      C = pts.get(i+1);
      vec BA = V(B,A), BC = V(B,C);
      change.add(S(t, S(BA, BC)));
    }
    change.add(new vec(0,0));
    
    for(int i = 0; i < pts.size(); i++) {
      pts.get(i).add(change.get(i));
    }
  }
  
  public static void smoothPolyline(pt[] pts, float t) {
    // this needs to be optimized in all kinds of ways
    if(pts.length < 3) return;
    t /= 2;
    int endex = pts.length-1;
    ArrayList<vec> change = new ArrayList<vec>();
    change.add(new vec(0,0));
    for(int i = 1; i < endex; i++) {
      pt
      A = pts[i-1],
      B = pts[i],
      C = pts[i+1];
      vec BA = V(B,A), BC = V(B,C);
      change.add(S(t, S(BA, BC)));
    }
    change.add(new vec(0,0));
    
    for(int i = 0; i < pts.length; i++) {
      pts[i].add(change.get(i));
    }
  }
  
  /**
   * smoothing subject to a data term (as in s.thrun's smoothing)
   */
  public static void smoothing(List<pt> pts, float dataWeight, int iterations) {
    List<pt> original = new ArrayList<pt>();
    for(pt p : pts) original.add(p.get());
    
    for(int k = 0; k < iterations; k++) {
      float t = 0.5f;
      int endex = pts.size()-1;
      List<vec> change = new ArrayList<vec>();
      change.add(new vec(0,0));
      for(int i = 1; i < endex; i++) {
        pt
        A = pts.get(i-1),
        B = pts.get(i),
        C = pts.get(i+1);
        vec BA = V(B,A), BC = V(B,C);
        change.add(S(t, S(BA, BC)));
      }
      change.add(new vec(0,0));
      
      for(int j = 0; j < pts.size(); j++) {
        pts.get(j).add(change.get(j));
        pts.get(j).translateTowardsByRatio(dataWeight, original.get(j));
      }
    }
  }

  /**
   * smoothing subject to a data term (as in s.thrun's smoothing)
   * pinned is a list of indices to which the data term should be applied, others are skipped
   */
  public static void smoothing(List<pt> pts, List<Integer> pinned, float dataWeight, int iterations) {
    List<pt> original = new ArrayList<pt>();
    for(pt p : pts) original.add(p.get());
    
    for(int k = 0; k < iterations; k++) {
      float t = 0.5f;
      int endex = pts.size()-1;
      List<vec> change = new ArrayList<vec>();
      change.add(new vec(0,0));
      for(int i = 1; i < endex; i++) {
        pt
        A = pts.get(i-1),
        B = pts.get(i),
        C = pts.get(i+1);
        vec BA = V(B,A), BC = V(B,C);
        change.add(S(t, S(BA, BC)));
      }
      change.add(new vec(0,0));
      
      for(int j = 0; j < pts.size(); j++) {
        pts.get(j).add(change.get(j));
      }
      for(int j : pinned) {
        pts.get(j).translateTowardsByRatio(dataWeight, original.get(j));
      }
    }
  }
  
  public static void smooth(float[] values, float t) {
    // we can get rid of the changes array and keep a single extra value around 
    if(values.length < 3) return;
    t /= 2;
    int endex = values.length-1;
    float[] change = new float[values.length];
    change[0] = 0;
    for(int i = 1; i < endex; i++) {
      float
      A = values[i-1],
      B = values[i],
      C = values[i+1];
      float BA = A-B, BC = C-B;
      change[i] = t * (BA + BC);
    }
    change[change.length-1] = 0;
    
    for(int i = 0; i < values.length; i++) {
      values[i] += change[i];
    }
  }
  
  
  public static void center(List<pt> pts, int width, int height) {
    float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
    for(pt p : pts) {
      if(p.x < minX) minX = p.x;
      if(p.y < minY) minY = p.y;
      if(p.x > maxX) maxX = p.x;
      if(p.y > maxY) maxY = p.y;
    }
    
    float xScale = width/(maxX-minX), yScale = height/(maxY-minY);
    float scale = min(xScale,yScale);
    for(pt p : pts) {
      p.x -= minX; p.x *= scale;
      p.y -= minY; p.y *= scale;
    }
  }
  
  public void centerOnScreen(List<pt> pts) {
    center(pts, width, height);
  }

  public static pt spiralPt(pt p, pt center, float scale, float angle) {
    return L(center, scale, R(p, angle, center));
  }

  public static pt spiralPt(pt p, pt center, float scale, float angle, float t) {
    if(center == null) {
      return new pt(p);
    } else {
      // here's the concise version
      if(true) {
        return L(center, pow(scale, t), R(p, t * angle, center));
      } else {
        // and here's the unrolled version
        float a = t * angle;
        float z = pow(scale, t);
        float cos = cos(a), sin = sin(a);
        
        return spiralPt(p, center, a, z, cos, sin);
      }
    }
  }
  
  // and here's the unrolled and constant-lifted version
  public static pt spiralPt(pt p, pt center, float a, float z, float cos, float sin) {
    float Ux = p.x-center.x;
    float Uy = p.y-center.y;
    float Rx = cos * Ux - sin * Uy;
    float Ry =  sin * Ux + cos * Uy;
    return new pt(
        center.x + z * Rx,
        center.y + z * Ry
    );
  }

  public static pt spiralCenter(pt A, pt B, pt C, pt D) { 
    // computes center of spiral that
    // takes A to C and B to D
    float a = spiralAngle(A, B, C, D);
    float z = spiralScale(A, B, C, D);
    return spiralCenter(a, z, A, C);
  }

  public static float spiralAngle(pt A, pt B, pt C, pt D) {
    return angle(V(A, B), V(C, D));
  }

  public static float spiralScale(pt A, pt B, pt C, pt D) {
    return d(C, D) / d(A, B);
  }

  public static pt spiralCenter(float a, float z, pt start, pt end) {
    float c = cos(a), s = sin(a);
    float D = sq(c * z - 1) + sq(s * z);
    float ex = c * z * start.x - end.x - s * z * start.y;
    float ey = c * z * start.y - end.y + s * z * start.x;
    float x = (ex * (c * z - 1) + ey * s * z) / D;
    float y = (ey * (c * z - 1) - ex * s * z) / D;
    if(Float.isNaN(x) || Float.isNaN(y)) {
      return null;
    } else {
      return new pt(x, y);
    }
  }
  
  /**
   * Given connectivity information, start and end points for verticies,
   * construct spirals that move the start points to the end points.
   * 
   * @param table
   * @param srcPoints
   * @param dstPoints
   * @return
   */
  public static Spiral[] makeSpirals(CornerTable table, pt[] srcPoints, pt[] dstPoints) {
    // I'm not sure if this is ok
    Spiral[] spirals = new Spiral[srcPoints.length];
    ArrayList<Spiral>[] perVertexSpirals = new ArrayList[srcPoints.length];
    for(int i = 0; i < perVertexSpirals.length; i++) {
      perVertexSpirals[i] = new ArrayList<Spiral>();
    }

    // for all the edges (not half-edges)
    for(int[] edge : table.getEdges()) {
      int v1 = edge[0], v2 = edge[1];
      
      pt start1 = srcPoints[v1], end1 = srcPoints[v2];
      pt start2 = dstPoints[v1], end2 = dstPoints[v2];
      Spiral s = new Spiral(start1, end1, start2, end2);
      
      // because we only see each edge once, we add this spiral to both the head and tail
      perVertexSpirals[v1].add(s);
      perVertexSpirals[v2].add(s);
    }
    spirals = new Spiral[srcPoints.length];
    for(int i = 0; i < perVertexSpirals.length; i++) {
      spirals[i] = Spiral.blend(srcPoints[i], dstPoints[i], perVertexSpirals[i]);
    }
    return spirals;
  }
  
  public static pt centerV(pt[] points) {
    pt center = P();
    for (int i = 0; i < points.length; i++) {
      center.addPt(points[i]);
    }
    return S(1f / points.length, center);
  }
  
  public static pt centerV(Collection<pt> points) {
    pt center = new pt();
    for(pt pt : points) {
      center.addPt(pt);
    }
    return S( 1f/points.size(), center);
  }

  public static pt convexCentMd(List<pt> P) {
    if(P.size() == 1) {
      return P.get(0).clone();
    } else if(P.size() == 2) {
      return A(P.get(0), P.get(1));
    }
    
    CornerTable del = CornerTable.delaunay(P);
    // instead of summing area weighted triangles, could find the border and compute trapezoidal area
    
    pt center = new pt();
    float totalArea = 0;
    for(int c = 0; c < del.V.length; c += 3) {
      pt
      A = P.get(del.V[c]),
      B = P.get(del.V[c+1]),
      C = P.get(del.V[c+2]); 
      pt triCenter = A(A,B,C);
      float area = triangleArea(A,B,C);
      totalArea += area;
      triCenter.scaleBy(area);
      center.addPt(triCenter);
    }
    return S(1f / totalArea, center);
  }

  
  public static pt centerE(pt[] P, boolean closed) {
    int n = P.length;
    pt G = P();
    float D = 0;
    for (int i = 0; i < n - 1; i++) {
      float d = d(P[i], P[i + 1]);
      D += d;
      G.addPt(S(d, A(P[i], P[i + 1])));
    }
    if (closed) {
      float d = d(P[n - 1], P[0]);
      D += d;
      G.addPt(S(d, A(P[n - 1], P[0])));
    }
    return S(1f / D, G);
  }

  public static void spiral(pt[] P, pt G, float s, float a, float t) {
    for (int i = 0; i < P.length; i++)
      P[i] = spiralPt(P[i], G, s, a, t);
  }
  
  /**
   * Move P by a fracion of t along a spiral from A to B
   * 
   * @param P
   * @param A
   * @param t
   * @param B
   */
  public static void spiral(Polygon P, Polygon A, float t, Polygon B) {
    float a = spiralAngle(A.points[0], A.points[A.points.length - 1],
        B.points[0], B.points[B.points.length - 1]);
    float s = spiralScale(A.points[0], A.points[A.points.length - 1],
        B.points[0], B.points[B.points.length - 1]);
    pt G = spiralCenter(a, s, A.points[0], B.points[0]);
    spiral(P.points, G, s, a, t);
  }

  public static pt[] resamplePolyline(List<pt> pts, int numPts) {
    if(pts.size() < 1) return new pt[0];
    
    float arcLen = 0;
    pt prev = pts.get(0);
    for(int i = 1; i < pts.size(); i++) {
      pt cur = pts.get(i);
      arcLen += cur.disTo(prev);
      prev = cur;
    }
    float segLen = arcLen/(numPts-1); // minus one because we preserve both endpoints
    
    pt[] samples = new pt[numPts];
    // preserve endpoints
    samples[0] = pts.get(0).clone();
    samples[numPts-1] = pts.get(pts.size()-1).clone();
    //
    float curSegLen = 0;
    prev = pts.get(0);
    int j = 1; // index for the new sampling
    for(int i = 1; i < pts.size() && j < numPts-1; i++) {
      pt cur = pts.get(i);
      
      while(true) {
        float dist = cur.disTo(prev);
        if(curSegLen+dist > segLen) {
          // update prev
          prev = lerp(prev, cur, (segLen-curSegLen)/dist);
          samples[j] = prev;
          j++;
          curSegLen = 0;
        } else {
          // we haven't completed this resampled segment, but we're at the end of this parent segment
          curSegLen += dist;
          break;
        }
      }
      prev = cur;
    }
    return samples;
  }
  
  // copied from above, with a different output format
  // maybe there's a better way
  public static List<pt> resamplePolylineToList(List<pt> pts, int numPts) {
    if(pts.size() < 1) return new ArrayList<pt>();
    // LAZZYVILLE
    pts.add(pts.get(0));
    
    // TODO: expose a version that ask for edgelength, since that may be easier to specify
    float segLen = arclengthOfLoop(pts)/(numPts-1); // minus one because we preserve both endpoints
    
    ArrayList<pt> result = new ArrayList<pt>(numPts);
    result.add(pts.get(0).clone());
    //
    float curSegLen = 0;
    pt prev = pts.get(0);
    int j = 1; // index for the new sampling
    for(int i = 1; i < pts.size() && j < numPts-1; i++) {
      pt cur = pts.get(i);
      
      while(true) {
        float dist = cur.disTo(prev);
        if(curSegLen+dist > segLen) {
          // update prev
          prev = lerp(prev, cur, (segLen-curSegLen)/dist);
          result.add(prev);
          j++;
          curSegLen = 0;
        } else {
          // we haven't completed this resampled segment, but we're at the end of this parent segment
          curSegLen += dist;
          break;
        }
      }
      prev = cur;
    }
    return result;
  }
  
  /**
   * TODO: make this more efficient, each sample takes log time,
   * whereas the noodly versions above take linear lime 
   * 
   * @param polyloop
   * @param maxEdgeLength
   * @return
   */
  public static List<pt> resampleLoopLazy(List<pt> polyloop, float minEdgeLength, float maxEdgeLength) {
    List<pt> resampled = new ArrayList<pt>();
    pt lastAdded = polyloop.get(polyloop.size()-1);
    pt lastSeen = lastAdded;
    float dist = 0;
    for(pt p : polyloop) {
      dist += p.disTo(lastSeen);
      if(dist > maxEdgeLength) {
        int numSteps = ceil(dist/maxEdgeLength);
        float stepSize = 1f/numSteps;
        for(int i = 0; i < numSteps; i++) {
          resampled.add(lerp(lastAdded, p, i*stepSize));
        }
      }
      if(dist > minEdgeLength) {
        resampled.add(p);
        lastAdded = p;
        dist = 0;
      }
      lastSeen = p;
    }
    return resampled;
  }

  public static List<pt> resample(List<pt> polyline, int numSamples) {
    List<pt> resampled = new ArrayList<pt>();
    SamplablePolyline p = new SamplablePolyline(polyline);
    float step = 1/(float)(numSamples-1);
    for(int i = 0; i < numSamples; i++) {
      resampled.add(p.sample(i*step));
    }
    return resampled;
  }
  
  public static List<pt> resampleToSpacing(List<pt> polyline, float spacing) {
    float length = arclength(polyline);
    return resample(polyline, (int) Math.ceil(length/spacing));
  }
  
  public static float arclengthOfLoop(List<pt> pts) {
    float arcLen = 0;
    pt prev = pts.get(pts.size()-1);
    for(int i = 0; i < pts.size(); i++) {
      pt cur = pts.get(i);
      arcLen += cur.disTo(prev);
      prev = cur;
    }
    return arcLen;
  }
  
  public static float arclength(List<pt> pts) {
    float arcLen = 0;
    pt prev = pts.get(0);
    for(int i = 1; i < pts.size(); i++) {
      pt cur = pts.get(i);
      arcLen += cur.disTo(prev);
      prev = cur;
    }
    return arcLen;
  }
  
  public static List<pt> localToGlobal(List<vec> locals, Map<Integer,pt> pinned) {

    int n = (locals.size()+2)*2;
    Matrix A = new Matrix(n, n);
    Matrix b = new Matrix(n, 1);
    vec lc;
    
    int len = locals.size(); // the first and last are special cases
    for(int i = 1; i <= locals.size(); i++) {
      int j = i*2;
      pt pin = pinned.get(i);
      
      lc = locals.get(i-1);
      
      A.set(j, j-2, lc.x-1);
      A.set(j, j-1, lc.y);
      A.set(j, j+2, -lc.x);
      A.set(j, j+3, -lc.y);
  
      A.set(j+1, j-2, -lc.y);
      A.set(j+1, j-1, lc.x-1);
      A.set(j+1, j+2, lc.y);
      A.set(j+1, j+3, -lc.x);

      if(pin != null) {
        b.set(j, 0, -pin.x);
        b.set(j+1, 0, -pin.y);
      } else {
        A.set(j, j, 1);
        A.set(j+1, j+1, 1);
      }
    }
    
    pt start = pinned.get(0);
    
    A.set(0, 0, 1);
    A.set(1, 1, 1);
    b.set(0, 0, start.x);
    b.set(1, 0, start.y);

    pt end = pinned.get(locals.size()+1);
    
    A.set(n-2, n-2, 1);
    A.set(n-1, n-1, 1);
    b.set(n-2, 0, end.x);    
    b.set(n-1, 0, end.y);
    
    
    Matrix pts = null;
    // both LU and QR decompose/solve work, LU is 2x to 10x faster
    try {
      pts = A.lu().solve(b);
    } catch(RuntimeException e) {
      e.printStackTrace();
      return new ArrayList<pt>(); // no solution, lame
    }
    
    if(pts.getRowDimension() != n) throw new IllegalArgumentException();
    
    List<pt> result = new ArrayList<pt>(n/2);
    for(int i = 0; i < n; i += 2) {
      result.add(new pt(pts.get(i, 0), pts.get(i+1, 0)));
    }
    return result;
  }
  
  public static List<pt> localToGlobal(Map<Integer,List<vec>> localSpans, Map<Integer,pt> pinned) {
    final int dim = 2;
    int numResultingPoints = -1;
    
    // keys are the span of points over which the local coord system was computed
    // values are the normalized coords in the system
    // as the span gets larger, there are more points at the end of the polyline for which
    // we can't construct a local coordinate system, so we factor that in when determining
    // how many points we can construct given some number of local coords
    for(Map.Entry<Integer,List<vec>> entry : localSpans.entrySet()) {
      numResultingPoints = entry.getValue().size() + entry.getKey()*2;
      break;
    }
    
    if(numResultingPoints < 1) throw new IllegalArgumentException();
    
    int n = numResultingPoints*dim;
    Matrix A = new Matrix(n, n);
    Matrix b = new Matrix(n, 1);
    vec lc;
        
    for(Map.Entry<Integer,List<vec>> entry : localSpans.entrySet()) {
      int span = entry.getKey();
      List<vec> locals = entry.getValue();
      
      int s2 = span*dim;
      int len = numResultingPoints-span; // the first and last are special cases
      for(int i = span; i < len; i++) { // index into resulting points
        int j = i*dim; // index into matrix
        pt pin = pinned.get(i);
    
        if(pin != null) {
          A.set(j, j, 1);
          A.set(j+1, j+1, 1);
          b.set(j, 0, pin.x);
          b.set(j+1, 0, pin.y);
          
        } else {
          float weight = 1f/sq(span);
          lc = locals.get(i-span);
          
          A.set(j, j-s2, (lc.x-1)*weight);
          A.set(j, j-s2+1, lc.y*weight);
          A.set(j, j+s2, -lc.x*weight);
          A.set(j, j+s2+1, -lc.y*weight);
      
          A.set(j+1, j-s2, -lc.y*weight);
          A.set(j+1, j-s2+1, (lc.x-1)*weight);
          A.set(j+1, j+s2, lc.y*weight);
          A.set(j+1, j+s2+1, -lc.x*weight);
          
          A.set(j, j, A.get(j,j)+weight);
          A.set(j+1, j+1, A.get(j+1,j+1)+weight);
        }
      }
    }
    
    pt start = pinned.get(0);
    
    A.set(0, 0, 1);
    A.set(1, 1, 1);
    b.set(0, 0, start.x);
    b.set(1, 0, start.y);

    pt end = pinned.get(numResultingPoints-1);
    
    A.set(n-2, n-2, 1);
    A.set(n-1, n-1, 1);
    b.set(n-2, 0, end.x);    
    b.set(n-1, 0, end.y);
    
    
    Matrix pts = null;
    // both LU and QR decompose/solve work, LU is 2x to 10x faster
    try {
      pts = A.lu().solve(b);
    } catch(RuntimeException e) {
      e.printStackTrace();
      return new ArrayList<pt>(); // no solution, lame
    }
    
    if(pts.getRowDimension() != n) throw new IllegalArgumentException();
    
    List<pt> result = new ArrayList<pt>(n/2);
    for(int i = 0; i < n; i += 2) {
      result.add(new pt(pts.get(i, 0), pts.get(i+1, 0)));
    }
    return result;
  }
  
  public static List<pt> localToGlobal(List<vec> locals, pt start, pt end) {
    Map<Integer,pt> pinned = new HashMap<Integer,pt>();
    pinned.put(0, start);
    pinned.put(locals.size()+1, end);
    return localToGlobal(locals, pinned);
  }
  
  // this doesn't interpolate the end points (though, neither does the curvature morph)
  // 
  public static Polygon laplaceBlend(Polygon a, float t, Polygon b) {
    List<vec> aLocals = globalToLocal(a.points);
    List<vec> bLocals = globalToLocal(b.points);
    
    for(int i = 0; i < aLocals.size(); i++) {
      vec al = aLocals.get(i), bl = bLocals.get(i);
      al.scaleBy(1-t);
      bl.scaleBy(t);
      al.add(bl);
    }
    
    pt start = new pt(0, 0), end = new pt(100, 0);
    List<pt> pts = localToGlobal(aLocals, start, end);
    Polygon p = new Polygon();
    p.points = new pt[pts.size()];
    for(int i = 0; i < pts.size(); i++) {
      p.points[i] = pts.get(i);
    }
    return p;
  }
  
  public static float[] blend(float[] a, float[] b, float t) {
    float[] result = new float[a.length];
    float t1 = 1-t;
    for(int i = 0; i < a.length; i++) {
      result[i] = a[i]*t1 + b[i]*t;
    }
    return result;
  }

  public static vec localLaplace(pt A, pt B, pt C) {
    vec
    AB = V(A,B),
    AC = V(A,C),
    AC_perp = new vec(AC.y, -AC.x);
    float norm = 1/n2(AC); 
    return new vec(
        dot(AB, AC)*norm,
        dot(AB, AC_perp)*norm
    );
  }

  static pt s(pt A, float s, pt B) {
    return (new pt(A.x + s * (B.x - A.x), A.y + s * (B.y - A.y)));
  }

  protected static pt b(pt A, pt B, pt C, float s) {
    return (s(s(B, s / 4f, A), 0.5f, s(B, s / 4f, C)));
  }

  protected static pt f(pt A, pt B, pt C,
      pt D, float s) {
        return (s(s(A, 1f + (1f - s) / 8f, B), 0.5f, s(D, 1f + (1f - s) / 8f, C)));
      }

  protected static <T> int nextI(T[] P, int i) {
    if (i == P.length-1) {
      return 0;
    } else {
      return i+1;
    }
  }

  protected static <T> T next(T[] P, int i) {
    return P[nextI(P, i)];
  }

  protected static <T> T prev(T[] P, int i) {
    if (i == 0) {
      return P[P.length-1];
    }  else {
      return P[i-1];
    }
  }

  public static double lerp(double a, double b, double t) {
    return (b-a)*t + a;
  }
  
  public static float elerp(float a, float b, float t) {
    return a * pow(b / a, t);
  }
  
  public static float clerp(float a, float b, float t) {
    return lerp(a,b,(1-cos(t*PI))/2);
  }
  
  public static pt clerp(pt a, pt b, float t) {
    return lerp(a,b,(1-cos(t*PI))/2);
  }
  
  public static double clerp(double a, double b, double t) {
    return lerp(a,b,(1-Math.cos(t*Math.PI))/2);
  }
  
  public static ComplexNumber lerp(ComplexNumber a, ComplexNumber b, float t) {
    return new ComplexNumber(lerp(a.real,b.real,t), lerp(a.imaginary, b.imaginary, t));
  }
  
  public static ComplexNumber clerp(ComplexNumber a, ComplexNumber b, float t) {
    return new ComplexNumber(clerp(a.real,b.real,t), clerp(a.imaginary, b.imaginary, t));
  }
  
  public static ComplexNumber elxerp(ComplexNumber a, ComplexNumber b, float t, float n) {
    return new ComplexNumber(elxerp(a.real, b.real, t, n), elxerp(a.imaginary, b.imaginary, t, n));
  }
  
  // a parameterized family of easing curves
  public static float elxerp(float a, float b, float t, float n) {
    return lerp(a, b, exp(log(t)*n));
  }
  
  public static double elxerp(double a, double b, double t, double n) {
    return lerp(a, b, Math.exp(Math.log(t)*n));
  }
  
  public static float pwlerp(float a, float b, float t, float n) {
    return (float)pwlerp(a,b,t,n);
  }
  
  public static double pwlerp(double a, double b, double t, double n) {
    n *= 2;
    if(n < 1) {
      if(t > n) return b;
      else return lerp(a,b,t/n);
    } else {
      n = n-1;
      if(t < n) return a;
      else return lerp(a,b, (t-n)/(1-n));
    }
  }
  
  public static ComplexNumber pwlerp(ComplexNumber a, ComplexNumber b, float t, float n) {
    return new ComplexNumber(pwlerp(a.real, b.real, t, n), pwlerp(a.imaginary, b.imaginary, t, n));
  }

  protected static Polygon curvatureMorph(Polygon start, float t, Polygon end) {
    Polygon P = new Polygon();
    P.copyFrom(end);
    int fail = 0;
    pt[] ps = P.points;
    ps[1] = T(ps[0], pow(d(start.points[0], start.points[1])
        / d(end.points[0], end.points[1]), t), V(end.points[0], end.points[1]));
    int n = min(end.points.length, start.points.length);
    
    for (int i = 2; i < n; i++) {
      /// get the angles and edge lengths of the inputs 
      float aLen = d(end.points[i - 1], end.points[i]);
      float bLen = d(start.points[i - 1], start.points[i]);
      
      float angleA = angle(
          V(end.points[i - 2], end.points[i - 1]),
          V(end.points[i - 1], end.points[i])
      );
      float angleB = angle(
          V(start.points[i - 2], start.points[i - 1]),
          V(start.points[i - 1], start.points[i])
      );
      
      // blend the angle and edge length
      float resultLength = elerp(aLen, bLen, t);
      float resultAngle = lerp(angleA, angleB, t);
      
      // get the direction of the previous edge,
      // our interpolated angle is relative to this
      vec diff = U(V(ps[i - 2], ps[i - 1]));
      if(diff == null) {
        // this is for dealing with coincident verticies i think
        // there's a better way to do this,
        // having to do with expanding the neighborhood
        ps[i] = L(ps[i - 1], 0.5f, ps[i - 2]); // lame but...
        fail++;
      } else {
        // move the previous point in the new angle, by the interpolated edge length
        ps[i] = T(ps[i - 1], S(resultLength, R(diff, resultAngle)));
      }
    }
    
    return new Polygon(ps);
  }

  public static Polygon linearMorph(Polygon A, float t, Polygon B) {
    int n = min(A.points.length, B.points.length);
    pt[] points = new pt[n];
    for (int i = 0; i < points.length; i++) {
      points[i] = L(A.points[i], t, B.points[i]);
    }
    return new Polygon(points);
  }
  
  public static List<pt> linearMorph(List<pt> A, float t, List<pt> B) {
    int n = min(A.size(), B.size());
    ArrayList<pt> pts = new ArrayList<pt>();
    for (int i = 0; i < n; i++) {
      pts.add(L(A.get(i), t, B.get(i)));
    }
    return pts;
  }
  
  
  class CurvatureMorph {
    float[] startLength, endLengths, startAngles, endAngles;
    public CurvatureMorph(Polygon start, Polygon end) {
      // TODO: precompute the lengths and angles
    }
  }
  
  public static Ray ray(pt A, pt B) {return new Ray(A,B); }
  public static Ray ray(pt Q, vec T) {return new Ray(Q,T); }
  public static Ray ray(pt Q, vec T, float d) {return new Ray(Q,T,d); }
  public static Ray leftTangentToCircle(pt P, pt C, float r) {return tangentToCircle(P,C,r,-1); }
  public static Ray rightTangentToCircle(pt P, pt C, float r) {return tangentToCircle(P,C,r,1); }
  public static Ray tangentToCircle(pt P, pt C, float r, float s) {
    float n=d(P,C); float w=sqrt(sq(n)-sq(r)); float h=r*w/n; float d=h*w/r; vec T = S(d,U(V(P,C)),s*h,R(U(V(P,C)))); return ray(P,T,w);}
  
  public static Ray3 ray(pt3 start, vec3 dir) { return new Ray3(start, dir); }
  
  /**
   * Returns the parameter on rays A and B between which this distance is minimized
   * Similar to line-line intersection. 
   */
  public static float[] rayPassingLocations(Ray3 A, Ray3 B) {
    pt3 p = A.start, q = B.start, w = P(p.x-q.x, p.y-q.y, p.z-q.z);
    vec3 u = A.dir, v = B.dir;
    float a = dot(u,u), b = dot(u,v), c = dot(v,v), d = dot(u,w), e = dot(v, w);
    return new float[] {
      (b*e - c*d)/(a*c - b*b),
      (a*e - b*d)/(a*c - b*b)
    };
  }
  
  

  public static float[][] toPackedArray(List<pt> points) {
    float[][] result = new float[points.size()][2];
    for(int i = 0; i < result.length; i++) {
      pt p = points.get(i);
      result[i][0] = p.x;
      result[i][1] = p.y;
    }
    return result;
  }
  
  public static float[][] toPackedArray(pt[] points) {
    float[][] result = new float[points.length][2];
    for(int i = 0; i < result.length; i++) {
      result[i][0] = points[i].x;
      result[i][1] = points[i].y;
    }
    return result;
  }
  
  public static float[][] toPacked2DArray(List<pt3> pts) {
    float[][] result = new float[pts.size()][2];
    for(int i = 0; i < result.length; i++) {
      pt3 p = pts.get(i);
      result[i][0] = p.x;
      result[i][1] = p.y;
    }
    return result;
  }

  
  public static class line3 {
    public pt3 start;
    public vec3 dir;
    public line3(pt3 start, vec3 dir) {
      this.start = start;
      this.dir = dir;
    }
  }
  
  public static line3 planeIntersection(pt3 onPlane1, vec3 normal1, pt3 onPlane2, vec3 normal2) {
    normal1 = normal1.normalized();
    normal2 = normal2.normalized();
    vec3 dir = normal1.cross(normal2); // get direction of intersection line
    vec3 toLineStart = dir.cross(normal1); // get direction from onPlane1 towards other plane
    toLineStart.normalize();
    pt3 start = linePlaneIntersection(onPlane1, toLineStart, onPlane2, normal2);
    return new line3(start, dir);
  }
  
  public static pt3 linePlaneIntersection(pt3 start, vec3 dir, pt3 planeCenter, vec3 normal) {
    normal = normal.normalized();
    float t = V(planeCenter,start).dot(normal) / dir.dot(normal);
    return T(start, -t, dir);
  }
  
  public static pt3 rayPlaneIntersection(Ray3 ray, Plane plane) {
    vec3 normal = plane.normal.normalized();
    float t = V(plane.center,ray.start).dot(normal) / ray.dir.dot(normal);
    if(t > 0) {
      return null;
    } else {
      return T(ray.start, -t, ray.dir);
    }
  }
  
  public static pt3[] intersectRaySphere(pt3 start, vec3 dir, pt3 center, float radius) {
    float a = dir.sqnorm();
    float b = 2*(dot(start,dir) - dot(center,dir));
    float c = start._sqnorm() - 2*dot(start,center) + center._sqnorm() - sq(radius);
    float[] ts = quadratic(a,b,c);
    pt3[] pts = new pt3[ts.length];
    for(int i = 0; i < pts.length; i++) {
      pts[i] = T(start, ts[i], dir);
    }
    return pts;
  }
  
  public float[] intersectRayCircle(pt p, vec dir, pt center, float rSq) {
    return quadratic(
      // a
      sq(dir.x) + sq(dir.y),
      // b
      2 * p.x * dir.x - 2 * center.x * dir.x
    + 2 * p.y * dir.y - 2 * center.y * dir.y,
      // c
      sq(p.x) - 2 * p.x * center.x + sq(center.x)
    + sq(p.y) - 2 * p.y * center.y + sq(center.y) - rSq
    );
  }

  public static final float[] quadratic(float a, float b, float c) {
    float discriminant = b*b - 4*a*c;
    
    if(discriminant < 0) {
      return new float[0];
  
    } else if(discriminant == 0) {
      return new float[] {-b/(2*a)};
    
    } else {
      float sqrtD = sqrt(discriminant);
      return new float[] {(-b+sqrtD)/(2*a), (-b-sqrtD)/(2*a)};
    }
  }
  
  public ArrayList<pt> loadPoints(String filename) {
    ArrayList<pt> pts = new ArrayList<pt>();
    String[] lines = loadStrings(filename);
    for(String line : lines) {
      String[] nums = line.split(" ");
      pts.add(new pt(parseFloat(nums[0]), parseFloat(nums[1])));
    }
    return pts;
  }
  
  public void savePoints(String name, List<pt> pts) {
    String[] lines = new String[pts.size()];
    for(int i = 0; i < lines.length; i++) {
      pt p = pts.get(i);
      lines[i] = p.x+" "+p.y;
    }
    saveStrings(name, lines);
  }
  
  public pt[][] loadPointSets(String filename) {
    String[] lines = loadStrings(filename);
    String[] lengths = lines[0].split(" ");
    pt[][] result = new pt[lengths.length][];
    for(int i = 0; i < result.length; i++) {
      result[i] = new pt[parseInt(lengths[i])];
    }
    int k = 1;
    for(int i = 0; i < result.length; i++) {
      for(int j = 0; j < result[i].length; j++) {
        String[] nums = lines[k].split(" ");
        result[i][j] = new pt(parseFloat(nums[0]), parseFloat(nums[1]));
        k++;
      }
    }
    return result;
  }
  
  public void savePointSets(String filename, pt[][] pts) {
    int numPoints = 0;
    for(pt[] ps : pts) numPoints += ps.length;
    String[] lines = new String[numPoints+1];
    lines[0] = lengthsToString(pts);
    int k = 1;
    for(pt[] ps : pts) {
      for(pt p : ps) {
        lines[k] = p.x+" "+p.y;
        k++;
      }
    }
    saveStrings(filename, lines);
  }
  
  private static String lengthsToString(Object[][] arrays) {
    StringBuilder sb = new StringBuilder();
    for(Object[] array : arrays) {
      sb.append(array.length);
      sb.append(' ');
    }
    if(sb.length() > 0) {
      sb.setLength(sb.length()-1);
    }
    return sb.toString();
  }
  

  public static float[] toFloats(String str) {
    String[] parts = str.split(" ");
    float[] vals = new float[parts.length];
    for(int i = 0; i < vals.length; i++) {
      vals[i] = parseFloat(parts[i]);
    }
    return vals;
  }
  
  public static String fromFloats(float... vals) {
    StringBuilder b = new StringBuilder();
    for(float f : vals) {
      b.append(f);
      b.append(' ');
    }
    return b.toString();
  }
  
  protected BoundingBox boundingBox(List<pt> pts) {
    return new BoundingBox(pts);
  }
  
  public static float sgn(float x) {
    if(x < 0) return -1;
    if(x > 0) return 1;
    return 0;
  }

  public pt orientedEllipsePoint(pt3 center, float maxRadius, vec3 normal, float theta) {
    vec d = new vec(normal.y, -normal.x);
    d.normalize();
    float ratio = normal.z;
    float dx =  d.x*maxRadius;
    float dy =  d.y*maxRadius;
    float dx2 = dx*ratio;
    float dy2 = dy*ratio;
    return orientedEllipsePoint(center.x, center.y, dx, dy, dx2, dy2, theta);
  }
  
  public pt orientedEllipsePoint(float cx, float cy, float dx, float dy, float dx2, float dy2, float theta) {
    float cost = cos(theta), sint = sin(theta);
    return new pt(
        cx + dx*cost - dy2*sint,
        cy + dy*cost + dx2*sint
    );
  }
  
  public void orientedEllipse(pt3 center, float maxRadius, vec3 normal, pt startPoint, pt endPoint) {
    vec d = new vec(normal.y, -normal.x);
    d.normalize();
    float
    ratio = normal.z,
    dx =  d.x*maxRadius,
    dy =  d.y*maxRadius,
    dx2 = dx*ratio,
    dy2 = dy*ratio; 
    
    pt center2d = center.as2D();
    
    float baseAngle = angle(V(center2d, orientedEllipsePoint(center.x, center.y, dx, dy, dx2, dy2, 0)));
    // this is wrong, we need some kind of iterative approach
    float startAngle = angle(V(center2d, startPoint))-baseAngle;
    float endAngle = angle(V(center2d, endPoint))-baseAngle;
    float fudge = 0.0f;
    
    if(startAngle > endAngle) {
      for(float theta = startAngle-fudge; theta > endAngle+fudge; theta -= 0.05) {
        pt p = orientedEllipsePoint(center.x, center.y, dx, dy, dx2, dy2, theta);
        float observedAngle = angle(V(center2d, p))-baseAngle;
        if(observedAngle > startAngle || observedAngle < endAngle) continue;
        vertex(p);
      }      
    } else {
      for(float theta = startAngle-fudge; theta < endAngle+fudge; theta += 0.05) {
        pt p = orientedEllipsePoint(center.x, center.y, dx, dy, dx2, dy2, theta);
        float observedAngle = angle(V(center2d, p))-baseAngle;
        if(observedAngle < startAngle || observedAngle > endAngle) continue;
        vertex(p);
      }
    }
  }
  
  public void orientedEllipse(pt3 center, float maxRadius, vec3 normal) {
    beginShape();
    orientedEllipseVerticies(center, maxRadius, normal);
    endShape();
  }

  public void orientedEllipseVerticies(pt3 center, float maxRadius, vec3 normal) {
    vec d = new vec(normal.y, -normal.x);
    d.normalize();
    float ratio = normal.z;
    float dx =  d.x*maxRadius;
    float dy =  d.y*maxRadius;

    _orientedEllipse(center, 0, TWO_PI, ratio, dx, dy);
  }

  // don't use this for general angles, it won't work
  // it will work if the angles are a multiple of PI/2
  public void orientedEllipse(pt3 center, float maxRadius, vec3 normal, float startAngle, float endAngle) {
    vec d = new vec(normal.y, -normal.x);
    d.normalize();
    float ratio = normal.z;
    float dx =  d.x*maxRadius;
    float dy =  d.y*maxRadius;

    _orientedEllipse(center, startAngle, endAngle, ratio, dx, dy);
  }
  
  public void halfDisk(pt center, float radius, vec normal) {
    float startAngle = normal.get().turnRight().angle();
    float stepSize = PI/12;
    beginShape();
    for(float angle = startAngle; angle < startAngle+PI; angle += stepSize) {
      vertex(center.x+cos(angle)*radius, center.y+sin(angle)*radius);
    }
    endShape();
  }

  private void _orientedEllipse(pt3 center, float startAngle, float endAngle, float ratio, float dx, float dy) {
    float step = 0.05f;
    for(float t = startAngle; t < endAngle; t += step) {
      float cost = cos(t), sint = sin(t);
      vertex(center.x + dx*cost - dy*sint*ratio,
             center.y + dy*cost + dx*sint*ratio
      );
    }
    float cost = cos(endAngle), sint = sin(endAngle);
    vertex(center.x + dx*cost - dy*sint*ratio,
           center.y + dy*cost + dx*sint*ratio
    );
  }
  public void drawQuadStrip(List<pt> left, List<pt> right) {
    int len = min(left.size(), right.size());
    beginShape(QUAD_STRIP);
    for(int i = 0; i < len; i++) {
      vertex(left.get(i));
      vertex(right.get(i));
    }
    endShape();
  }
  
  
  public static float hermiteLength(pt start, pt end, vec startDir, vec endDir) {
    float sum = 0;
    float stepSize = 0.1f;
    pt prev = start;
    for(float t = stepSize; t < 1; t += stepSize) {
      float sqt = sq(t), cbt = cubed(t);
      float sqt3 = sqt*3, cbt2 = cbt*2;
      pt cur = S(cbt2 - sqt3 + 1, start,  -cbt2 + sqt3, end,
                 cbt - sqt*2 + t, startDir, -cbt + sqt, endDir);
      sum += prev.disTo(cur);
      prev = cur;
    }
    sum += prev.disTo(end);
    return sum;
  }

  public static double sqrt(double v) {
    return Math.sqrt(v);
  }
  
  public static double sq(double v) {
    return v*v;
  }
  
  public static float cubed(float x) {
    return x*x*x;
  }
  
  public void hermite(pt start, pt end, vec startDir, vec endDir) {
    hermite(start,end,startDir,endDir,20);
  }
  
  // TODO: try to decode the curveVertexSegment method in PGraphics,
  // because it seems to accomplish catmull-rom splines with only addition in the inner loop
  public void hermite(pt start, pt end, vec startDir, vec endDir, int numSteps) {
    beginShape();
    float step = 1f/numSteps;
    for(float t = 0; t < 1; t += step) {
      float sqt = sq(t), cbt = cubed(t);
      float sqt3 = sqt*3, cbt2 = cbt*2;
      
      vertex(S(cbt2 - sqt3 + 1, start, -cbt2 + sqt3, end,
               cbt-sqt*2+t, startDir, -cbt+sqt, endDir));
    }
    vertex(end);
    endShape();
  }
  
  public pt3 screen(pt3 p) {
    return new pt3(
        screenX(p.x,p.y,p.z),
        screenY(p.x,p.y,p.z),
        screenZ(p.x,p.y,p.z)
    );
  }

  public boolean onScreen(pt3 p) {
    float x = screenX(p.x,p.y,p.z);
    float y = screenY(p.x,p.y,p.z);
    return x > 0 && x < width && y > 0 && y < width;
  }

  public pt screen2D(pt3 p) {
    return new pt(
        screenX(p.x,p.y,p.z),
        screenY(p.x,p.y,p.z)
    );
  }
  
  public void circle(pt center, double radius) {
    ellipseMode(RADIUS);
    ellipse(center.x, center.y, (float)radius, (float)radius);
  }
  
  public void polygon(pt... pts) {
    beginShape();
    for(pt p : pts) vertex(p);
    endShape(CLOSE);
  }
  
  public void polygon(pt3... pts) {
    beginShape();
    for(pt3 p : pts) vertex(p);
    endShape(CLOSE);
  }
  
  public void polygon(List<pt> pts) {
    beginShape();
    for(pt p : pts) vertex(p);
    endShape(CLOSE);
  }
  
  public void polyline(pt... pts) {
    beginShape();
    for(pt p : pts) vertex(p);
    endShape();
  }
  
  public void polyline(pt3... pts) {
    beginShape();
    for(pt3 p : pts) vertex(p);
    endShape();
  }
  
  public void polyline(List<pt> pts) {
    beginShape();
    for(pt p : pts) vertex(p);
    endShape();
  }
  
  public void polyline3(List<pt3> pts) {
    beginShape();
    for(pt3 p : pts) vertex(p);
    endShape();
  }
  
  public static int closestPointIndex(pt[] pts, float x, float y) {
    float closestDist = Float.MAX_VALUE;
    int closestIndex = -1;
    for(int i = 0; i < pts.length; i++) {
      float d = pts[i].sqDisTo(x,y);
      if(d < closestDist) {
        closestDist = d;
        closestIndex = i;
      }
    }

    return closestIndex;
  }
  
  public static int closestPointIndex(List<? extends pt> pts, pt q) {
    return closestPointIndex(pts, q.x, q.y);
  }
  public static int closestPointIndex(List<? extends pt> pts, float x, float y) {
    float closestDist = Float.MAX_VALUE;
    int closestIndex = -1;
    for(int i = 0; i < pts.size(); i++) {
      float d = pts.get(i).sqDisTo(x,y);
      if(d < closestDist) {
        closestDist = d;
        closestIndex = i;
      }
    }

    return closestIndex;
  }

  public static int closestPointIndex(List<pt3> pts, pt3 p) {
    return closestPointIndex(pts, p.x, p.y, p.z);
  }
  public static int closestPointIndex(List<pt3> pts, float x, float y, float z) {
    float closestDist = Float.MAX_VALUE;
    int closestIndex = -1;
    for(int i = 0; i < pts.size(); i++) {
      float d = pts.get(i).sqDisTo(x,y,z);
      if(d < closestDist) {
        closestDist = d;
        closestIndex = i;
      }
    }

    return closestIndex;
  }

  public static int closestPointIndex(List<pt3> pts, Ray3 r) {
    float closestDist = Float.MAX_VALUE;
    int closestIndex = -1;
    for(int i = 0; i < pts.size(); i++) {
      float d = r.sqdist(pts.get(i));
      if(d < closestDist) {
        closestDist = d;
        closestIndex = i;
      }
    }
    return closestIndex;
  }

  public static pt closestPoint(pt[] pts, pt p) {
    if(pts.length > 0) {
      return pts[closestPointIndex(pts, p.x, p.y)];
    } else {
      return null;
    }
  }
  
  public static pt closestPoint(pt[] pts, float x, float y) {
    if(pts.length > 0) {
      return pts[closestPointIndex(pts, x, y)];
    } else {
      return null;
    }
  }
  
  public static pt closestPoint(List<? extends pt> pts, float x, float y) {
    int i = closestPointIndex(pts, x, y); // if pts are NaN or something, may return -1 even with contents
    if(i >= 0) {
      return pts.get(i);
    } else {
      return null;
    }
  }
  
  public static pt3 closestPoint(List<pt3> pts, pt3 p) {
    return closestPoint(pts, p.x, p.y, p.z);
  }
  
  public static pt3 closestPoint(List<pt3> pts, float x, float y, float z) {
    if(pts.size() > 0) {
      return pts.get(closestPointIndex(pts, x, y, z));
    } else {
      return null;
    }
  }

  public static int[] closestPointIndex(float x, float y, pt[]... arrays) {
    float closestDist = Float.MAX_VALUE;
    int closestIndex = -1;
    int closestArray = -1;
    for(int i = 0; i < arrays.length; i++) {
      pt[] pts = arrays[i];
      for(int j = 0; j < pts.length; j++) {
        float d = pts[j].sqDisTo(x,y);
        if(d < closestDist) {
          closestDist = d;
          closestIndex = j;
          closestArray = i;
        }
      }
    }
    return new int[]{closestArray,closestIndex};
  }
  
  public static pt closestPoint(float x, float y, pt[]... arrays) {
    if(arrays.length > 0) {
      int[] ind = closestPointIndex(x,y, arrays);
      return arrays[ind[0]][ind[1]];
    } else {
      return null;
    }
  }
  
  public static float triangleArea(pt A, pt B, pt C) {
    vec a = V(A,B), b = V(A,C);
    return abs(a.x*b.y - b.x*a.y)*0.5f;
  }
  
  public static float triangleArea(pt3 A, pt3 B, pt3 C) {
    vec3 a = V(A,B), b = V(A,C);
    return abs(a.x*b.y - b.x*a.y)*0.5f;
  }
  
  public static float trapezoidArea(pt A, pt B) {
    return (A.x+B.x)*(A.y-B.y)*0.5f;
  }
  
  public static float doubleTrapezoidArea(pt A, pt B) {
    return (A.x+B.x)*(A.y-B.y);
  }
  
  public static float signedArea(pt a, pt b, pt c, pt d) {
    return signedArea(Arrays.asList(new pt[]{a,b,c,d}));
  }
  
  public static float signedArea(List<pt> pts) {
    float area = 0;
    for(int pi = pts.size()-1, i = 0; i < pts.size(); pi = i, i++) {
      area += doubleTrapezoidArea(pts.get(pi), pts.get(i));
    }
    return area/2;
  }
  
  public static float area(List<pt> pts) {
    return abs(signedArea(pts));
  }
  
  public static class AreaInfo {
    pt centroid;
    float area;
  }
  
  public static AreaInfo areaInfo(List<pt> pts) {
    pt centroid = new pt(0,0);
    float area = 0;
    for(int pi = pts.size()-1, i = 0; i < pts.size(); pi = i, i++) {
      pt a = pts.get(pi), b = pts.get(i);
      float trapArea = a.x*b.y - b.x*a.y;
      centroid.x += (a.x+b.x)*trapArea;
      centroid.y += (a.y+b.y)*trapArea;
      area += trapArea;
    }
    centroid.scaleBy(1/(3*area));
    AreaInfo areaInfo = new AreaInfo();
    areaInfo.area = area;
    areaInfo.centroid = centroid;
    return areaInfo;
  }
  
  public static pt centroid(pt[] pts) {
    return centroid(Arrays.asList(pts));
  }

  public static pt3 centroid(pt3[] pts) {
    return centroid3(Arrays.asList(pts));
  }
  
  public static pt centroid(List<pt> pts) {
    if(pts.size() < 3) return centerV(pts); // for a line or point 
    pt result = new pt(0,0);
    float area = 0;
    for(int pi = pts.size()-1, i = 0; i < pts.size(); pi = i, i++) {
      pt a = pts.get(pi), b = pts.get(i);
      float trapArea = a.x*b.y - b.x*a.y;
      result.x += (a.x+b.x)*trapArea;
      result.y += (a.y+b.y)*trapArea;
      area += trapArea;
    }
    result.scaleBy(1/(3*area));
    return result;
  }
  
  // HUH???
  public static pt3 centroid3(List<pt3> pts) {
    pt3 result = new pt3(0,0,0);
    float area = 0;
    for(int pi = pts.size()-1, i = 0; i < pts.size(); pi = i, i++) {
      pt3 a = pts.get(pi), b = pts.get(i);
      float trapArea = a.x*b.y - b.x*a.y;
      result.x += (a.x+b.x)*trapArea;
      result.y += (a.y+b.y)*trapArea;
      area += trapArea;
    }
    result.scaleBy(1/(3*area));
    return result;
  }
  
  public static pt convexCentroid(List<pt> pts) {
    return centroid(ConvexHull.fromPoints(pts).boundary);
  }
  
  public static float moment(pt[] boundary) {
    return moment(Arrays.asList(boundary));
  }
  
  public static float moment(List<pt> boundary) {
    return moment(centroid(boundary),boundary);
  }
  
  // moment of inertia
  public static float moment(pt center, List<pt> boundary) {
    float sum = 0;
    for (int i = 0, pi = boundary.size()-1; i < boundary.size(); pi = i, i++) {
       vec GA = V(center,boundary.get(pi));
       vec GB = V(center,boundary.get(i));
       sum += dot(GA.get().turnLeft(),GB)*(dot(GA,GA)+dot(GA,GB)+dot(GB,GB));
    }
    return abs(sum / 12f);   
  }

  protected static void main(String[] args, Class<? extends PApplet> klass) {    
    try {
      
      JFrame frame = new JFrame();
      final PApplet sketch = klass.newInstance();
  
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          sketch.exit();
        }
      });
      
      frame.add(sketch);
      sketch.frame = frame;
      sketch.init();
      try {
        while(sketch.defaultSize) {
            Thread.sleep(20);
        }
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
      
      frame.pack();
      frame.setResizable(false); // would need to have some logic to resize the additional buffer
      frame.setVisible(true);
      sketch.requestFocus();
      
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public static String lpad(int value, char pad, int width) {
    return lpad(Integer.toString(value), pad, width);
  }
  
  public static String lpad(String value, char pad, int width) {
    if(value.length() >= width) return value;
    else return lpad(pad+value, pad, width-1);
  }

  // i get a few obfuscation points for this, i hope
  public static <T> Iterable<? extends T> reversed(final List<? extends T> items) {
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          int i;
          List<? extends T> _items;
          { // this block is like a constructor, but the parameter is "passed" as a closure variable
            if(items instanceof RandomAccess) {
              _items = items;
            } else {
              // store to a buffer
              _items = new ArrayList<T>(items);
            }
            i = _items.size()-1;
          }
          
          public boolean hasNext() {
            return i >= 0;
          }
  
          public T next() {
            T result = _items.get(i);
            i--;
            return result;
          }
  
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public static String packPoints(pt[] pts) {
    StringBuilder b = new StringBuilder();
    for(pt p : pts) {
      b.append(p.x);
      b.append(' ');
      b.append(p.y);
      b.append(' ');
    }
    return b.toString();
  }
  
  public static pt[] unpackPoints(String packed) {
    String[] values = packed.split(" ");
    pt[] pts = new pt[values.length/2];
    for(int i = 0; i < pts.length; i++) {
      pts[i] = new pt(
          Float.parseFloat(values[i*2]),
          Float.parseFloat(values[i*2+1])
      );
    }
    return pts;
  }
  
  public static pt[] unpackPoints(String[] values) {
    pt[] pts = new pt[values.length];
    for(int i = 0; i < pts.length; i++) {
      String[] vals = values[i].split(" ",2);
      pts[i] = new pt(
          Float.parseFloat(vals[0]),
          Float.parseFloat(vals[1])
      );
    }
    return pts;
  }
  
  public pt randomPoint() {
    return new pt(random(0,width),random(0,height));
  }
  
  public vec randomVector() {
    return randomVector(1);
  }
  
  public vec randomVector(float maxLength) {
    float mlsq = maxLength*maxLength;
    vec v = new vec(0,0); 
    do {
      v.setTo(random(-maxLength, maxLength), random(-maxLength, maxLength));
    } while(v.sqnorm() > mlsq);
    return v;
  }
  
  public vec randomUnitVector() {
    vec v = randomVector();
    v.normalize();
    return v;
  }
  
  public float smallestGreaterThanZero(float a, float b) {
    if(a > 0 && a < b) return a;
    else if(b > 0 && b < a) return b;
    else return max(a, b);
  }
  public void line(vec3 a, vec3 b) {
    line(a.x, a.y, a.z, b.x, b.y, b.z);
  }
  // it would be nice if java's type system smiled upon generalization
  // but as it is, we'll have to settle for some code duplication
  public static pt[] clonePoints(pt[] items) {
    pt[] cloned = items.clone();
    for(int i = 0; i < items.length; i++) {
      cloned[i] = items[i].clone();
    }
    return cloned;
  }
  
  /**
   * Inplace clone, for when the original array was created in a unique way, but the points are aliased
   * @param items
   */
  public static void _clonePoints(pt[] items) {
    for(int i = 0; i < items.length; i++) {
      items[i] = items[i].clone();
    }
  }
  
  public static List<pt> clonePoints(List<pt> items) {
    try {
      List<pt> cloned;
      try {
        cloned = items.getClass().newInstance();
      } catch (InstantiationException e) {
        cloned = new ArrayList<pt>();
      }
      for(pt p : items) {
        cloned.add(p.clone());
      }
    return cloned;
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }
  
  public static <T> T firstCommonElement(List<T> A, List<T> B) {
    Set<T> BH = new TreeSet<T>(B); // obvious
    for(T a : A) {
      if(BH.contains(a)) {
        return a;
      }
    }
    return null;
  }
  
  public static <T> Pair<Integer,Integer> firstCommonElementIndicies(List<T> A, List<T> B) {
    Set<T> BH = new TreeSet<T>(B); // obvious
    int aIndex = 0;
    int bIndex = -1; 
    for(T a : A) {
      if(BH.contains(a)) {
        bIndex = B.indexOf(a);
        break;
      }
      aIndex++;
    }
    
    if(bIndex == -1) return null;
    
    return Pair.make(aIndex,bIndex);
  }

  public static vec3 reflect(vec3 incoming, vec3 normal) {
    float s = -2*normal.dot(incoming);
    return new vec3(incoming.x + s*normal.x, incoming.y + s*normal.y, incoming.z + s*normal.z);
  }

  public static vec3 rotate(vec3 v, vec3 axis, float radians) {
    return new Rotation(axis, radians).transform(v);
  }
  
  // FIXME: these could certainly be more efficient
  
  public static Rotation getRotation(vec3 a, vec3 b) {
    vec3 srcU = U(a), dstU = U(b); // FIXME: do we need to normalize the axis?
    return Rotation.axisAngle(U(srcU.cross(dstU)), acos(srcU.dot(dstU)));
  }
  
  public static Rotation getRotationAround(vec3 a, vec3 b, vec3 axis) {
    return getRotation(a.orthogonal(axis), b.orthogonal(axis));
  }
  
  public static float getRotationAngleAround(vec3 a, vec3 b, vec3 axis) {
    /*
    a = U(a.orthogonal(axis));
    b = U(b.orthogonal(axis));
    return acos(a.dot(b));
    */
    Rotation ro = getRotationAround(a,b,axis);
    if(axis.dot(ro.getAxis()) > 0) {
      print("t ");
      return ro.getAngle();
    } else {
      print("f ");
      return -ro.getAngle();
    }
  }
  
  /** Get the rotation between two isometric triangles*/
  public static Rotation getRotation(pt3[] src, pt3[] dst) {
    if(src.length != 3 || dst.length != 3) throw new IllegalArgumentException("only triangles supported");
    pt3 srcCenter = A(src), dstCenter = A(dst);
    vec3[] srcE = new vec3[3];
    vec3[] dstE = new vec3[3];
    for(int i = 0; i < 3; i++) {
      srcE[i] = V(srcCenter, src[i]);
      dstE[i] = V(dstCenter, dst[i]);
    }
    // get rotation aligning first vertex/edge
    Rotation ro1 = getRotation(srcE[0], dstE[0]);
    // get rotation for the residual,
    // around the axis we want to avoid changing (the one aligned by the first rotation) 
    Rotation ro2 = getRotationAround(ro1.transform(srcE[1]), dstE[1], dstE[0]);
    ro2.combine(ro1);
    return ro2;
  }
  
  /**
   * FIXME: this will not always provide the optimal result,
   * in fact, it may return angles that don't result in src mapping to dst,
   * even when a solution exists,
   * this just uses a geometric hueristic,
   * and should be replaced by something correct
   */
  public static float[] getRotationAround(vec3 src, vec3 dst, vec3 V, vec3 U) {
    src = U(src); dst = U(dst); 
    // rotation, fitting U first
    Rotation fru = getRotationAround(src, dst, U);
    vec3 ruSrc = fru.transform(src);
    Rotation frv = getRotationAround(ruSrc, dst, V);
    Rotation fwd = frv.get();
    fwd.combine(fru);
    
    // rotation fitting V first
    Rotation brv = getRotationAround(dst, src, V);
    vec3 rvDst = brv.transform(dst);
    brv.minus();
    Rotation bru = getRotationAround(src, rvDst, U);
    ruSrc = bru.transform(src);
    Rotation bwd = brv.get();
    bwd.combine(bru);

    // debug
    float f = U(fwd.transform(src)).dot(dst);
    float b = U(bwd.transform(src)).dot(dst);
    float max = dst.dot(dst);
    float slen = src.mag(), dlen = dst.mag();
    // take whichever to closer to the target
    if(fwd.transform(src).dot(dst) > bwd.transform(src).dot(dst)) {
        return new float[] {frv.getAngle(), fru.getAngle()};
    } else {
      return new float[] {brv.getAngle(), bru.getAngle()};
    }
  }
  
  public void drawRotation(Rotation ro, pt3 center, pt3 start) {
    ro.toAxisAngle();
    drawRotation(V(ro.x, ro.y, ro.z), ro.angle, center, start);
  }
  
  public void drawRotation(vec3 axis, double angle, pt3 center, pt3 start) {
    vec3 U = V(center,start);
    center = T(center, U.parallel(axis));
    
    U = V(center,start);
    vec3 V = axis.cross(U);
    float farDist = U.mag();
    float nearDist = farDist*.75f;
    U.normalize(); V.normalize();
    int numSteps = (int)(32*Math.abs(angle)/PI);
    if(numSteps < 5) numSteps = 5;
    double step = angle/numSteps;
    beginShape(QUAD_STRIP);
    for(int i = 0; i < numSteps+1; i++) {
      double th = i*step;
      vec3 dir = S(Math.sin(th), V, Math.cos(th), U);
      vertex(T(center,nearDist,dir));
      vertex(T(center,farDist,dir));
    }
    endShape();
  }
  
  
  // TODO
  public static int black,white,red,green,blue,cyan,magenta,yellow,orange;
  {
    black = color(0);
    white = color(255);
    
    red = color(255,0,0);
    green = color(0,255,0);
    blue = color(0,0,255);
    
    cyan = color(0,255,255);
    magenta = color(255,0,255);
    yellow = color(255,255,0);
    
    orange = color(255,200,0);
  }
  
  public void draw(Trajectory traj) {
    float step = 0.05f;
    beginShape();
    for(float t = 0; t < 1+step; t += step) {
      vertex(traj.at(t));
    }
    endShape();
  }
  
  public void draw(Trajectory traj, float step) {
    beginShape();
    for(float t = 0; t < 1+step; t += step) {
      vertex(traj.at(t));
    }
    endShape();
  }
  
  public String _selectOutput(final String prompt) {
    return _selectFileImpl(prompt, FileDialog.SAVE);
  }
  
  public String _selectInput(final String prompt) {
    return _selectFileImpl(prompt, FileDialog.LOAD);
  }
  
  public String _selectFileImpl(final String prompt, final int mode) {
    checkParentFrame();

    FileDialog fileDialog =
      new FileDialog(parentFrame, prompt, mode);
    fileDialog.setVisible(true);
    String directory = fileDialog.getDirectory();
    String filename = fileDialog.getFile();
    selectedFile = (filename == null) ? null : new File(directory, filename);
    return (selectedFile == null) ? null : selectedFile.getAbsolutePath();
  }
  
  public String prompt(String prompt) {
    return prompt(prompt, "");
  }
  
  private String promptResult;
  public String prompt(final String prompt, final String defaultValue) {
    promptResult = "";
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          promptResult = (String) JOptionPane.showInputDialog(
            parentFrame,
            prompt,
            "",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            defaultValue
          );
        }
      });
      if(promptResult == null) {
        promptResult = "";
      }
      return promptResult;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public String alert(final String message) {
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(
            parentFrame,
            message
          );
        }
      });
      if(promptResult == null) {
        promptResult = "";
      }
      return promptResult;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  void checkInited() {
    if(g == null) throw new IllegalStateException("applet must be initialized before calling this method");
  }
  
  // one common menuListener that all of the menus created with the following methods will call
  // set
  protected ActionListener menuListener;
  protected ActionListener menuListenerDelayed;
  
  public void menuItem(Menu m, String text) {
    menuItem(m, text, true);
  }
  
  public void menuItem(Menu m, String text, boolean delay) {
    if(menuListener == null) throw new IllegalStateException("Must set menuListener before constructing menus");
    
    MenuItem mi = new MenuItem(text);
    mi.setName(text);
    if(delay) {
      if(menuListenerDelayed == null) {
        menuListenerDelayed = delayToPost(menuListener);
      }
      mi.addActionListener(menuListenerDelayed);
    } else { 
      mi.addActionListener(menuListener);
    }
    m.add(mi);
  }
  
  public Menu makeMenu(String name, String[] items) {
    Menu menu = new Menu(name);
    for(String item : items) {
      if(item != null) {
        menuItem(menu, item);
      } else {
        menu.addSeparator();
      }
    }
    return menu;
  }
  
  public Menu menu(String name, String[] items, MenuBar menuBar) {
    Menu menu = makeMenu(name, items);
    menuBar.add(menu);
    return menu;
  }
    
  public ActionListener delayToPre(final ActionListener listener) {
    checkInited();
    Delayer delayer = new Delayer(listener);
    registerPre(delayer);
    return delayer;
  }
  
  public ActionListener delayToDraw(final ActionListener listener) {
    checkInited();
    Delayer delayer = new Delayer(listener);
    registerDraw(delayer);
    return delayer;
  }
  
  public ActionListener delayToPost(final ActionListener listener) {
    checkInited();
    Delayer delayer = new Delayer(listener);
    registerPost(delayer);
    return delayer;
  }
  
  public ChangeListener delayToPost(final ChangeListener listener) {
    checkInited();
    Delayer delayer = new Delayer(listener);
    registerPost(delayer);
    return delayer;
  }
  
  public ActionListener delayToDispose(final ActionListener listener) {
    checkInited();
    Delayer delayer = new Delayer(listener);
    registerPost(delayer);
    return delayer;
  }
  
  /**
   * A class to delay the handling of action events until specific times in the Processing lifecycle.
   * Useful when you have AWT menus which receive their actions on the AWT event thread,
   * and which want to modify data being used concurrently by a Processing .draw method. For example. 
   *
   */
  public static class Delayer implements java.awt.event.ActionListener, javax.swing.event.ChangeListener {
    
    ActionListener wrappedListener;
    ChangeListener wrappedChangeListener;
    List<ActionEvent> delayedEvents;
    List<ChangeEvent> delayedChangeEvents;
    
    public Delayer(ActionListener wrappedListener) {
      this.wrappedListener = wrappedListener;
      delayedEvents = new ArrayList<ActionEvent>();
      delayedChangeEvents = new ArrayList<ChangeEvent>();
    }
    
    public Delayer(ChangeListener wrappedListener) {
      this.wrappedChangeListener = wrappedListener;
      delayedEvents = new ArrayList<ActionEvent>();
      delayedChangeEvents = new ArrayList<ChangeEvent>();
    }
    
    public void actionPerformed(ActionEvent e) {
      delayedEvents.add(e);
    }
    
    public void stateChanged(ChangeEvent e) {
      delayedChangeEvents.add(e);
    }
    
    public void pre() { handle(); }
    public void draw() { handle(); }
    public void post() { handle(); }
    public void dispose() { handle(); }
    
    public void handle() {
      for(ActionEvent e : delayedEvents) {
        try {
          wrappedListener.actionPerformed(e);
        } catch(Exception ex) {
          ex.printStackTrace();
        }
      }
      delayedEvents.clear();
      
      for(ChangeEvent e : delayedChangeEvents) {
        try {
          wrappedChangeListener.stateChanged(e);
        } catch(Exception ex) {
          ex.printStackTrace();
        }
      }
      delayedChangeEvents.clear();
    }  
  }
  

  public static Matrix columnMatrix(vec[] columns) {
    Matrix m = new Matrix(2, columns.length);
    for(int i = 0; i < columns.length; i++) {
      m.set(0, i, columns[i].x);
      m.set(1, i, columns[i].y);
    }
    return m;
  }
  
  public static Matrix rowMatrix(vec[] rows) {
    Matrix m = new Matrix(rows.length, 2);
    for(int i = 0; i < rows.length; i++) {
      m.set(i, 0, rows[i].x);
      m.set(i, 1, rows[i].y);
    }
    return m;
  }
  
  public static Matrix diagonalMatrix(float[] values) {
    Matrix m = new Matrix(values.length, values.length);
    for(int i = 0; i < values.length; i++) {
      m.set(i,i,values[i]);
    }
    return m;
  }
  
  public static vec[] relativeTo(pt origin, pt[] pts) {
    vec[] relative = new vec[pts.length];
    for(int i = 0; i < pts.length; i++) {
      relative[i] = new vec(origin, pts[i]);
    }
    return relative;
  }
  
  public static vec[] relativeTo(pt origin, List<pt> pts) {
    vec[] relative = new vec[pts.size()];
    for(int i = 0; i < relative.length; i++) {
      relative[i] = new vec(origin, pts.get(i));
    }
    return relative;
  }
  
  public static vec3[] relativeTo(pt3 origin, pt3[] pts) {
    vec3[] relative = new vec3[pts.length];
    for(int i = 0; i < pts.length; i++) {
      relative[i] = new vec3(origin, pts[i]);
    }
    return relative;
  }

  public static vec3[] relativeTo(pt3 origin, List<pt3> pts) {
    vec3[] relative = new vec3[pts.size()];
    for(int i = 0; i < relative.length; i++) {
      relative[i] = new vec3(origin, pts.get(i));
    }
    return relative;
  }

  public static boolean collinear(pt a, pt b, pt c) {
    vec AB = V(a,b), AC = V(a,c);
    return AB.x*AC.y == AC.x*AB.y;
  }
  
  public static boolean colocated(pt a, pt b) {
    return a.x==b.x && a.y==b.y;
  }
  
  public static String degeneracyReport(List<pt> pts) {
    StringBuilder report = new StringBuilder();
    for(int i = 0; i < pts.size(); i++) {
      for(int j = i+1; j < pts.size(); j++) {
        if(colocated(pts.get(i), pts.get(j))) {
          report.append("colocated: "+i+" "+j+" at "+pts.get(i).toString()+"\n");
          continue; // if they're colocated, we don't need a collinear report
        }
        for(int k = j+1; k < pts.size(); k++) {
          if(collinear(pts.get(i),pts.get(j),pts.get(k))) {
            report.append("collinear: "+i+" "+j+" "+k+"\n");
          }
        }
      }
    }
    return report.toString();
  }
  
  public static int p(int c) { int mod = c%3; if(mod == 0) return c+2; else return c-1; }
  public static int n(int c) { int mod = c%3; if(mod == 2) return c-2; else return c+1; }
  
  /**
   * Cotangent of the angle between OI and OJ
   * Can be used to compute weights for a laplacian matrix, or for curvature estimation.
   * 
   * @param O
   * @param I 
   * @param J
   * @return
   */
  public static float cotan(pt O, pt I, pt J) {
    vec A = V(O,I);
    vec B = V(O,J);
    float v = A.dot(B)/(A.norm()*B.norm());
    return (float)(v/sqrt(1-v*v));
  }
  
  public static float cotan(pt3 O, pt3 I, pt3 J) {
    vec3 A = V(O,I);
    vec3 B = V(O,J);
    float v = A.dot(B)/(A.norm()*B.norm());
    return (float)(v/sqrt(1-v*v));
  }
  
  public static float[] normalize(float[] values) {
    float[] result = new float[values.length];
    float sum = sum(values);
    for(int i = 0; i < values.length; i++) {
      result[i] = values[i]/sum;
    }
    return result;
  }
  
  public static float sum(float[] values) {
    float sum = 0;
    for(int i = 0; i < values.length; i++) {
      sum += values[i];
    }
    return sum;
  }
  
  public static int minInt(Collection<Integer> vals) {
    int smallest = Integer.MAX_VALUE;
    for(int v : vals) {
      if(v < smallest) smallest = v;
    }
    return smallest;
  }
  
  public static float minFloat(Collection<Float> vals) {
    float smallest = Float.MAX_VALUE;
    for(float v : vals) {
      if(v < smallest) smallest = v;
    }
    return smallest;
  }
  
  // given a triangle with edges lengths a,b,c
  // what is the length of the edge drawn from the midpoint of edge c to the opposite vertex?
  // used by the flower sim to compute the rest length of springs 
  // when the actual rest position is unknown/unembeddable
  public static float splitEdgeLengthC(float a, float b, float c) {
    return sqrt(sq(sqrt(a*a*a*a - 2*a*a*b*b + b*b*b*b - 
                        2*a*a*c*c - 2*b*b*c*c + c*c*c*c)/a)/16f + 
              + sq(-a - (a*a + b*b - c*c)/(2f*a))/4f);
  }
  
  public static float splitEdgeLengthAB(float a, float b, float c) {
    return sqrt(
          (a*a*a*a - 2*a*a*b*b + b*b*b*b - 2*a*a*c*c - 2*b*b*c*c + c*c*c*c)/(16f*a*a)
        + sq(-a/2f + (a*a + b*b - c*c)/(4f*a))
    );
  }
  
  
  public static float zoomFactor(BoundingBox src, BoundingBox dst) {
    return min(dst.width()/src.width(), dst.height()/src.height());
  }
  
  
  public static <T> Iterable<Pair<T,T>> circularPairs(Iterator<T> wrapped) {
    return new CircularPairIterator<T>(wrapped);
  }
  public static <T> Iterable<Pair<T,T>> circularPairs(Iterable<T> wrapped) {
    return new CircularPairIterator<T>(wrapped);
  }
  public static <T> Iterable<Pair<T,T>> circularPairs(T[] wrapped) {
    return new CircularPairIterator<T>(iterator(wrapped));
  }
  
  public static <T> Iterable<Pair<T,T>> pairs(Iterator<T> wrapped) {
    return new PairIterator<T>(wrapped);
  }
  public static <T> Iterable<Pair<T,T>> pairs(Iterable<T> wrapped) {
    return new PairIterator<T>(wrapped);
  }
  public static <T> Iterable<Pair<T,T>> pairs(T[] wrapped) {
    return new PairIterator<T>(iterator(wrapped));
  }
  
  public static <T> Iterator<T> iterator(T[] items) {
    return Arrays.asList(items).iterator();
  }
  
  public static <T> Iterable<T> iterable(T[] items) {
    return Arrays.asList(items);
  }

  public static vec fromRadial(float radius, float theta) {
    return new vec(radius*cos(theta), radius*sin(theta));
  }
  
  public static boolean contains(List<pt> points, pt query) {
    InteriorTest it = new InteriorTest(toPackedArray(points));
    return it.contains(query.x, query.y);
  }
  

  public static pt spiralCenter(Edge A, Edge B) {
    return spiralCenter(A.a,A.b,B.a,B.b); 
  }
  
  public static Edge bezier(Edge A, Edge B, Edge C, Edge D, float t) {
    return  R( R( R(A,t,B) ,t, R(B,t,C) ) ,t, R( R(B,t,C) ,t, R(C,t,D) ) ); 
  }
  public static Edge catmull(Edge A, Edge B, Edge C, Edge D, float t) {
    return bezier(B,R(A,1f/6,C,B),R(D,1f/6,B,C),C,t); 
  }

  // local cords of M in {U,V}
  public static vec local(vec M, vec U, vec V) {
    float d = U.x*V.y-U.y*V.x;
    float x = (M.x*V.y-M.y*V.x)/d;
    float y = (M.y*U.x-M.x*U.y)/d; return V(x,y);
  }
  
  //local cords of M in {U,V}
  public static pt local(pt M, pt O, vec U, vec V) {
    float d = U.x*V.y-U.y*V.x;
    float x = ((M.x-O.x)*V.y-(M.y-O.y)*V.x)/d;
    float y = ((M.y-O.y)*U.x-(M.x-O.x)*U.y)/d;
    return P(x,y);
  }
  
  public static Edge E(pt A, pt B) {
    return new Edge(A,B); 
  }
    
  // cotangent(angle(BA,BC)) 
  public static float cotAlpha(pt3 A, pt3 B, pt3 C) {
    vec3 a = V(B,A);
    vec3 b = V(B,C);
    float v = a.dot(b)/(a.norm()*b.norm());
    return (float)(v/sqrt(1-v*v));
  }
  
  public static float cotAlpha(pt A, pt B, pt C) {
    vec a = V(B,A);
    vec b = V(B,C);
    float v = a.dot(b)/(a.norm()*b.norm());
    return (float)(v/sqrt(1-v*v));
  }
  
  static float cotAlphaSlower(pt3 A, pt3 B, pt3 C) {
    vec3 a = V(B,A);
    vec3 b = V(B,C);
    float n = a.dot(b);
    float m = a.mag()*b.mag();
    return (float)(1/tan(acos(n/m)));
  }

  public static Quaternion mul(Quaternion q1, Quaternion q2) {
    Quaternion res = new Quaternion();
    res.w = q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z;
    res.x = q1.w * q2.x + q1.x * q2.w + q1.y * q2.z - q1.z * q2.y;
    res.y = q1.w * q2.y + q1.y * q2.w + q1.z * q2.x - q1.x * q2.z;
    res.z = q1.w * q2.z + q1.z * q2.w + q1.x * q2.y - q1.y * q2.x;
    return res;
  }
 
  /**
   * Given an object that fits in the src box, translate and scale so that it fits into the dst box.
   * 
   * Scales uniformly so that the object is not stretched.
   * 
   * @param src
   * @param dst
   */
  public float boundingBoxZoom(BoundingBox src, BoundingBox dst, boolean dontEnlarge) {
    /*
    if(dontEnlarge) {
      float srcMinSize = dst.width();
      float minD = min(src.width(), src.height());
      if(srcMinSize < 10) src.pad(srcMinSize-minD);
    }
    */
    
    pt srcCenter = src.center();
    pt dstCenter = dst.center();
    float scale = zoomFactor(src, dst);
    if(dontEnlarge) scale = min(scale, 1);
    
    translate(dstCenter.x, dstCenter.y);
    scale(scale,scale);
    translate(-srcCenter.x, -srcCenter.y);
    return scale;
  }
  
  public void centerAndZoom(pt src, pt dst, float scale) {
    translate(dst.x, dst.y);
    scale(scale,scale);
    translate(-src.x, -src.y);
  }
  
  public pt3 pick() {
    return pick(mouseX, mouseY);
  }
  
  public pt3 pick(int x, int y) {
    GL gl = ((PGraphicsOpenGL)g).beginGL();
    GLU glu = ((PGraphicsOpenGL)g).glu;
    int viewport[] = new int[4]; 
    double[] proj = new double[16]; 
    double[] model = new double[16]; 
    gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0); 
    gl.glGetDoublev(GL.GL_PROJECTION_MATRIX,proj,0); 
    gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX,model,0); 
    FloatBuffer fb=ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer(); 
    gl.glReadPixels(x, height-y, 1, 1, GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, fb); 
    fb.rewind(); 
    double[] mousePosArr=new double[4]; 
    glu.gluUnProject((double)x,height-(double)y,(double)fb.get(0), model,0,proj,0,viewport,0,mousePosArr,0); 
    ((PGraphicsOpenGL)g).endGL(); 
    return P(mousePosArr[0],mousePosArr[1],mousePosArr[2]);
  }
  
  public Ray3 getRay(float x, float y, ArcBall arcball) {
    float fov = PI/3;
    if(g instanceof PGraphicsOpenGL) {
      fov = ((PGraphicsOpenGL)g).cameraFOV;
    }
    return getRay(x, y, arcball, fov);
  }
  
  public Ray3 getRay(float x, float y, CameraInterface camera, float fov) {
    return getRay(x,y, camera.getEye(), camera.getFocus(), camera.getUp(), fov);
  }
  
  public Ray3 getRay(float x, float y, pt3 eye, pt3 focus, vec3 up, float fov) {
    vec3 X = V(eye,focus).cross(up).normalized();
    vec3 Y = X.cross(V(eye,focus)).normalized();
    vec3 Z = U(eye, focus);
    
    float cx = x-this.width()/2, cy = y-this.height()/2;
    // normalize screen so that far left = -1, far right = 1
    float nrm = 1f/this.height();
    // triangle formed from eye, along camera vector to center of screen, along screen to one side
    // angle at eye = fov/2, length of half screen = 1/2, zed = distance from eye to screen
    float zed = 0.5f/tan(fov/2); 
    vec3 dir = S(zed, Z, cx*nrm, X, cy*nrm, Y);
    dir.normalize();
    return new Ray3(eye, dir);
  }
  
  // for viewport changes
  public int width() { return width; }
  public int height() { return height; }

  /* http://code.google.com/p/processing/issues/detail?id=1005 */
  public void sphere(float r) {
    pushMatrix();
    scale(r);
    super.sphere(1);
    popMatrix();
  }
  
  public void sphere(vec3 loc, float r) {
    sphere(P(loc), r);
  }
  
  public void sphere(pt3 loc, double r) { sphere(loc, (float)r); }
  public void sphere(pt3 loc, float r) {
    pushMatrix();
    translate(loc);
    sphere(r);
    popMatrix();
  }
  
  
  public void assertTrue(boolean expr) {
    if(!expr) throw new AssertionError();
  }
  
  public void bar(pt3 a, pt3 b, float r) {
    bar(a, b, r, 5);
  }
  
  public void bar(pt3 a, pt3 b, float r, int numFacets) {
    if(a.sqdist(b) < 0.0001) {
      sphere(a,r); // not really a bar
    } else {
      vec3
      along = V(a,b),
      up = V(1,1,1),
      U = along.cross(up),
      V = along.cross(U);
      U.normalize(); U.scaleBy(r);
      V.normalize(); V.scaleBy(r);
      beginShape(QUAD_STRIP);
      for(int thi = 0; thi < numFacets+1; thi++) {
        float th = thi*TWO_PI/numFacets;
        vertex(T(a, cos(th), U, sin(th), V));
        vertex(T(b, cos(th), U, sin(th), V));
      }
      endShape();
    }
  }
  
  public void box(pt3 a, pt3 b, vec3 up, float r, int[] colors) {
    box(a,b,up,r,r,colors);
  }
  
  public void box(pt3 a, pt3 b, vec3 up, float r1, float r2, int[] colors) {
    int numFacets = 4;
    vec3
    along = V(a,b),
    U = along.cross(up),
    V = along.cross(U);
    U.normalize(); U.scaleBy(r1);
    V.normalize(); V.scaleBy(r2);
    beginShape(QUADS);
    float step = TWO_PI/numFacets;
    for(int thi = 0; thi < numFacets+1; thi++) {
      fill(colors[thi%colors.length]);
      float th0 = (0+thi)*step + PI/4;
      float th1 = (1+thi)*step + PI/4;
      vertex(T(a, cos(th0), U, sin(th0), V));
      vertex(T(b, cos(th0), U, sin(th0), V));
      vertex(T(b, cos(th1), U, sin(th1), V));
      vertex(T(a, cos(th1), U, sin(th1), V));
    }
    endShape();
  }
  
  public List<pt> project(List<pt3> pts) {
    List<pt> results = new ArrayList<pt>();
    for(pt3 p : pts) {
      results.add(new pt(screenX(p.x, p.y, p.z), screenY(p.x, p.y, p.z)));
    }
    return results;
  }
  
  public void drawGradient() {
    // TODO: make the more processing-y?
    GL gl = ((PGraphicsOpenGL)g).beginGL();
    gl.glClearDepth(1);
    gl.glMatrixMode(gl.GL_PROJECTION);
    gl.glPushMatrix();
    gl.glLoadIdentity();
    gl.glOrtho(-1,1,-1,1,-1,1);
    gl.glMatrixMode(gl.GL_MODELVIEW);
    gl.glPushMatrix();
    gl.glLoadIdentity();
    gl.glPushAttrib(gl.GL_ENABLE_BIT);
    gl.glDisable(gl.GL_DEPTH_TEST);
    gl.glDisable(gl.GL_LIGHTING);
    gl.glDisable(gl.GL_TEXTURE_2D);

    gl.glBegin(gl.GL_TRIANGLE_STRIP);
    gl.glColor3d(.75f,.7f,.9f); gl.glVertex2f(-1, 1);
    gl.glColor3d(.9,.9,1); gl.glVertex2f(-1,-1);
    gl.glColor3d(.75f,.7f,.9f); gl.glVertex2f( 1, 1);
    gl.glColor3d(.9,.9,1); gl.glVertex2f( 1,-1);
    gl.glEnd();

    gl.glPopAttrib();
    gl.glPopMatrix(); // restore modelview
    gl.glMatrixMode(gl.GL_PROJECTION);
    gl.glPopMatrix();
    gl.glMatrixMode(gl.GL_MODELVIEW);
    ((PGraphicsOpenGL)g).endGL();
  }
  

  /**
   * Get an orthonormal basis containing this vector as the first of three
   * TODO: make this non-random, look at arrow()
   * make static when random is removed
   */
  public vec3[] basis(vec3 U) {
    U = U.normalized();
    vec3 V = V(1,1,1);
    V.normalize();
    vec3 W = U.cross(V);
    V = W.cross(U);
    return new vec3[] {U,V,W};
  }
  
  public void circle(pt3 center, float radius, vec3 up) {
    vec3[] UVW = basis(up);
    vec3 V = UVW[1], W = UVW[2];
    V.toLength(radius); W.toLength(radius);
    int steps = 16;
    float step = TWO_PI/steps;
    beginShape();
    for(int i = 0; i < steps; i++) {
      float th = step*i;
      vertex(T(center, cos(th), V, sin(th), W));
    }
    endShape(CLOSE);
  }
  
  public void caplet(Circle A, Circle B) {
    vec3 Y = V(0,-1,0);
    
    float height = 0.0f;
    pt3 a = P(A.center.x, height, A.center.y);
    pt3 b = P(B.center.x, height, B.center.y);
    circle(a, A.radius, Y);
    circle(b, B.radius, Y);
    vec3 ab = V(a,b);
    
    float angle = asin((B.radius-A.radius)/ab.norm());
    ab.normalize();
    
    vec3 off = R(ab, PI/2+angle, Y);
    line(T(a, A.radius, off),T(b, B.radius, off));
    off = R(ab, -PI/2-angle, Y);
    line(T(a, A.radius, off),T(b, B.radius, off));
  }
  
  public static Edge[] capletEdges(Circle a, Circle b) {
    vec ab = V(a.center, b.center);
    
    float angle = asin((b.radius-a.radius)/ab.norm());
    ab.normalize();
    
    vec off1 = R(ab, PI/2+angle);
    vec off2 = R(ab, -PI/2-angle);
    return new Edge[] { // the direction of these edges are required by BendMaquette's convex hull approximation
      new Edge(T(a.center, a.radius, off1), T(b.center, b.radius, off1)),
      new Edge(T(b.center, b.radius, off2), T(a.center, a.radius, off2))
    };
  }

  public static Matrix getRotation(vec3 axis, float angle) {
    // TODO: expand this out to make it more efficient
    //[R] = [I] + s*[~axis] + t*[~axis]^2
    Matrix cr = axis.cross();
    Matrix cr2 = cr.times(cr);
    return Matrix.identity(4, 4)
      .plusEquals(cr.times(sin(angle)))
      .plusEquals(cr2.times(1-cos(angle)));
  }

  public static vec3 column(Matrix m, int i) {
    return V(m.get(0,i),m.get(1,i),m.get(2,i));
  }
  
  public static PMatrix3D PMatrix3D(Matrix m) {
    double[][] x = m.getArray();
    return new PMatrix3D(
      (float)x[0][0], (float)x[0][1], (float)x[0][2], (float)x[0][3],
      (float)x[1][0], (float)x[1][1], (float)x[1][2], (float)x[1][3],
      (float)x[2][0], (float)x[2][1], (float)x[2][2], (float)x[2][3],
      (float)x[3][0], (float)x[3][1], (float)x[3][2], (float)x[3][3]
    );
  }
  
  public boolean isShiftDown() {
    return keyEvent != null && keyEvent.isShiftDown();
  }

  public boolean isCmdDown() {
    return keyEvent != null && keyEvent.isMetaDown();
  }
  
  public static <T extends Enum<T>> T nextEnum(T[] values, T e) {
    return values[(e.ordinal()+1)%values.length];
  }
  
  public static float circular_lerp(float a, float b, float t) {
    if(abs(b-a) > PI) {
      if(b-a > 0) {
        return PApplet.lerp(a+TWO_PI,b,t)%TWO_PI;
      } else {
        return PApplet.lerp(a,b+TWO_PI,t)%TWO_PI;
      }
    } else {
      return PApplet.lerp(a,b,t);
    }
  }
  
  public static vec3 scaleByAlong(vec3 v, float scale, vec3 axis) {
    vec3 par = v.parallel(axis);
    vec3 ort = v.sub(par);
    return S(scale, par, 1, ort);
  }
  /**
   * (TODO: write a test to confirm this)
   * Return the shortest angular offset from a to b
   * 
   * b == (a+angle_diff(a,b))%TWO_PI
   */
  public static float angle_diff(float a, float b) {
    if(abs(b-a) > PI) {
      if(b-a > 0) {
        return b-a-TWO_PI;
      } else {
        return b+TWO_PI-a;
      }
    } else {
      return b-a;
    }
  }

  public static float angle_lerp(float a, float b, float t) {
    float vec = angle_diff(a,b);
    return a+vec*t;
  }

  public static float round(float val, float q) {
    return round(val/q)*q;
  }

  public static void save(File f, Object obj) {
      try {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
        out.writeObject(obj);
        out.close();
      } catch(IOException e) {
        e.printStackTrace();
      }
    }
      
  public static Object load(File f) {
    try {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
      Object result = in.readObject();
      in.close();
      return result;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }
}