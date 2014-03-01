package megamu.mesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import processing.core.PApplet;

import quickhull3d.QuickHull3D;
import shapecore.Circle;
import shapecore.pt;
import shapecore.interfaces.PointSet;

public class MedialAxisTransform {

  // connectivity (undirected graph)
  public LinkedArray faceNet;
  // geometry
  public float[][] dualPoints;
  public float[] radii;

  // cached data
  float[][] _edges;
  List<Circle> _spheres;

  // higher level knowledge
  Brancher brancher;
  int artifact;

  public MedialAxisTransform(float[][] points) {

    // TODO: catch other special cases? two points?
    if(points.length < 1) {
      return;
    }

    // build points array for qhull
    double[] qPoints = new double[ points.length*3 + 9 ];
    for(int i = 0; i < points.length; i++){
      qPoints[i*3] = points[i][0];
      qPoints[i*3+1] = points[i][1];
      // z component = squared distance from 0,0
      // thus convex hull will contain faces of delaunay triangulation 
      qPoints[i*3+2] = -(points[i][0]*points[i][0] + points[i][1]*points[i][1]);
    }
    // 1
    qPoints[ qPoints.length-9 ] = -8000D;
    qPoints[ qPoints.length-8 ] = 0D;
    qPoints[ qPoints.length-7 ] = -64000000D;
    // 2
    qPoints[ qPoints.length-6 ] = 8000D;
    qPoints[ qPoints.length-5 ] = 8000D;
    qPoints[ qPoints.length-4 ] = -128000000D;
    // 3
    qPoints[ qPoints.length-3 ] = 8000D;
    qPoints[ qPoints.length-2 ] = -8000D;
    qPoints[ qPoints.length-1 ] = -128000000D;

    // run quickhull
    QuickHull3D quickHull = new QuickHull3D(qPoints);
    int[][] faces = quickHull.getFaces(QuickHull3D.POINT_RELATIVE + QuickHull3D.CLOCKWISE);
    artifact = 0;
    
    // we'll discard any points outside the shape
    InteriorTest poly = new InteriorTest(points);
    
    // compute dual points
    dualPoints = new float[faces.length][2];
    radii = new float[faces.length];
    for(int i = 0; i < faces.length; i++){

      // test if it's the artifact
      if(faces[i][0] >= points.length && faces[i][1] >= points.length && faces[i][2] >= points.length) {
        artifact = i;
      }
      
      // vertices of triangle
      double
      x0 = qPoints[faces[i][0]*3+0],
      y0 = qPoints[faces[i][0]*3+1],
      x1 = qPoints[faces[i][1]*3+0],
      y1 = qPoints[faces[i][1]*3+1],
      x2 = qPoints[faces[i][2]*3+0],
      y2 = qPoints[faces[i][2]*3+1];

      // circumcenter calculation
      double
      v1x = 2 * (x1-x0),
      v1y = 2 * (y1-y0),
      v1z = x0*x0 - x1*x1 + y0*y0 - y1*y1,

      v2x = 2 * (x2-x0),
      v2y = 2 * (y2-y0),
      v2z = x0*x0 - x2*x2 + y0*y0 - y2*y2,

      denom = v1x * v2y - v1y * v2x;

      dualPoints[i][0] = (float)((v1y * v2z - v1z * v2y) / denom);
      dualPoints[i][1] = (float)((v1z * v2x - v1x * v2z) / denom);
      
      if(faces[i][0] >= points.length || faces[i][1] >= points.length || faces[i][2] >= points.length ||
          !poly.contains(dualPoints[i][0], dualPoints[i][1])) {
        // this triangle is outside the polygon, flag it to ignore
        radii[i] = -1;
      } else {
        double
        x01 = x0-x1, x12 = x1-x2, x20 = x2-x0,
        y01 = y0-y1, y12 = y1-y2, y20 = y2-y0,
        a = Math.sqrt(x01*x01 + y01*y01),
        b = Math.sqrt(x12*x12 + y12*y12),
        c = Math.sqrt(x20*x20 + y20*y20);
        // circumradius
        radii[i] = (float)((a*b*c)/Math.sqrt((a+b+c)*(b+c-a)*(c+a-b)*(a+b-c)));
      }
    }
    
    faceNet = new LinkedArray(faces.length);

    // discover edges
    for(int i = 0; i < faces.length; i++){
      
      // discard these
      if(i == artifact || radii[i] <= 0) continue;
      
      // this looks like the slow part, quadratic time?
      for(int j = 0; j < i; j++) {
        if(j != artifact && radii[j] > 0 && Voronoi.isEdgeShared(faces[i], faces[j])) {
          faceNet.link(i, j);
        }
      }
    }

  }

  public float[][] getEdges() {
    if(_edges == null) {
      int edgeCount = faceNet.numEdges();
      _edges = new float[edgeCount][6];
      int k = 0;
      // build all edges from connectivity data in faceNet
      // and geometry in dualPoints and radii
      for(int i = 0; i < faceNet.size(); i++) {
        for(int j : faceNet.get(i).getLinks()) {
          if(i < j) { // ignore one of the directed edges
            _edges[k][0] = dualPoints[i][0];
            _edges[k][1] = dualPoints[i][1];
            _edges[k][2] = radii[i];
            _edges[k][3] = dualPoints[j][0];
            _edges[k][4] = dualPoints[j][1];
            _edges[k][5] = radii[j];
            k++;
          }
        }
      }
    }

    return _edges;
  }
  
  public List<Circle> getInterpolatedCircles() {
    if(_spheres == null) {
      _spheres = new ArrayList<Circle>();
      for(float[] edge : getEdges()) {
        // pick how far apart the interpolated circles should be
        float r1 = edge[2], r2 = edge[5];
        float d = PApplet.dist(edge[0],edge[1], edge[3],edge[4]);
        float stepSize = 0.25f*Math.min(r1,r2)/d;
        // interpolate between samples
        // may result in no sphere if the originals are close, with a large radius 
        for(float t = stepSize; t < 1; t += stepSize) {
          _spheres.add(lerpCircle(edge,t));          
        }
      }
      for(int i = 0; i < radii.length; i++) {
        if(i != artifact && radii[i] > 0) {
          _spheres.add(ball(i));
        }
      }
    }
    return _spheres;
  }
  
  private Circle lerpCircle(float[] edge, float t) {
    return new Circle(
        lerp(edge[0],edge[3],t), // x
        lerp(edge[1],edge[4],t), // 
        lerp(edge[2],edge[5],t) // radius
    );
  }
  
  private float lerp(float a, float b, float t) {
    return a*(1-t) + b*t;
  }

  
  public List<BallChain> getBranches() {
    if(brancher == null) brancher = new Brancher();
    return brancher.branches;
  }
  
  public List<Junction> getJunctions() {
    if(brancher == null) brancher = new Brancher();
    return brancher.junctions;
  }
  
  /**
   * Where multiple branches of the medial axis meet.
   */
  public static class Junction {
    public float x,y,radius;
    
    /** array of x,y,r values of connected branches */
    public float[][] outgoing;
    List<BallChain> branches = new ArrayList<BallChain>();
    
    Junction(float x, float y, float radius, float[][] outgoing) {
      this.x = x;
      this.y = y;
      this.radius = radius;
      this.outgoing = outgoing;
    }

    public void addBranch(BallChain branch) {
      branches.add(branch);
    }

    public List<BallChain> getBranches() {
      return branches;
    }
  }
  
  public static class BallChain implements Iterable<Circle>,PointSet {
    List<Circle> balls = new ArrayList<Circle>();
    List<Junction> junctions = new ArrayList<Junction>();

    public void add(Circle ball) {
      balls.add(ball);
    }

    public int size() {
      return balls.size();
    }

    public Circle get(int i) {
      return balls.get(i);
    }

    public Iterator<Circle> iterator() {
      return balls.iterator();
    }

    public List<pt> getPoints() {
      List<pt> result = new ArrayList<pt>();
      for(Circle c : balls) {
        result.add(c.center);
      }
      return result;
    }

    public List<Junction> getJunctions() {
      return junctions;
    }
    
    public void addJunction(Junction j) {
      junctions.add(j);
      j.addBranch(this);
    }
  }
  
  /**
   * Construct the connectivity of the medial axis.
   * Find all the junctions (where multiple branches meet) and branches (sequence of edges without intervening junctions).
   *
   */
  private class Brancher {
    // mapping from medial axis vertices to any junction object at that vertex
    Map<Integer,Junction> junctionIndicies;
    Set<Integer> traversedJunctions;
    List<Junction> junctions;
    List<BallChain> branches;
    
    Brancher() {
      // first find all junctions and construct objects for them 
      junctionIndicies = new HashMap<Integer,Junction>();
      junctions = new ArrayList<Junction>();
      for(int i = 0; i < faceNet.size(); i++) {
        LinkedIndex index = faceNet.get(i);
        int arity = index.getLinks().size();
        if(arity != 2 && radii[i] > 0) {
          List<Integer> links = index.getLinks();
          float[][] outgoing = new float[links.size()][3];
          for(int j = 0; j < outgoing.length; j++) {
            outgoing[j][0] = dualPoints[links.get(j)][0];
            outgoing[j][1] = dualPoints[links.get(j)][1];
            outgoing[j][2] = radii[links.get(j)];
          }
          Junction j = new Junction(dualPoints[i][0], dualPoints[i][1], radii[i], outgoing);
          junctions.add(j);
          junctionIndicies.put(i, j);
        }
      }
      // now find all branches and link to their junctions
      branches = new ArrayList<BallChain>();
      traversedJunctions = new HashSet<Integer>();
      
      // we need to try each of the junctionIndicies
      // later ones will probably be traversed already
      for(Integer i : junctionIndicies.keySet()) {
        traverse(i, -1); // don't ignore any direction
      }
    }
    
    /**
     * Find and add all the branches at this junction.
     */
    void traverse(int junctionIndex, int ignoreDirection) {
      if(traversedJunctions.contains(junctionIndex)) return;
      traversedJunctions.add(junctionIndex);
      
      // for all the edges at this junction
      List<Integer> links = faceNet.get(junctionIndex).getLinks();
      for(int i = 0; i < links.size(); i++) {
        int j = links.get(i);
        int pj = junctionIndex;
        // we arrived from one of the branches, which we should avoid retraversing
        if(j == ignoreDirection) continue;
        if(j == junctionIndex) continue; // self loop, I don't know if this is possible
        if(j == 0) continue; // artifact of the large containing triangle
        
        // make a new branch outgoing from this junction for the given link
        BallChain branch = new BallChain();
        branch.add(ball(junctionIndex));
        branch.addJunction(junctionIndicies.get(junctionIndex));
        
        // continue until you arrive at one of the other junctionIndicies
        while(!junctionIndicies.containsKey(j)) {
          branch.add(ball(j));
          List<Integer> moreLinks = faceNet.get(j).getLinks();
          if(moreLinks.size() == 2) {
            // pick the correct edge, in the same direction
            if(moreLinks.get(0) == pj) {
              pj = j;
              j = moreLinks.get(1);
            } else if(moreLinks.get(1) == pj) {
              pj = j;
              j = moreLinks.get(0);
            } else {
              break;
            }
          } else {
            break; // arrived at a junction, stop creating this branch
          }
        }
        
        // add the junction point that we found (perhaps without any iterations above)
        branch.add(ball(j));
        branch.addJunction(junctionIndicies.get(j));
        branches.add(branch);
        
        traverse(j, pj); // traverse the new branch
      }
    }
  }
  
  Circle ball(int i) {
    return new Circle(dualPoints[i][0], dualPoints[i][1], radii[i]);
  }
}
