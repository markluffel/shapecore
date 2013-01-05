package megamu.mesh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import quickhull3d.QuickHull3D;

public class MedialAxisTransform {

  float[][] edges;
  // temp
  public LinkedArray faceNet;
  public IntArray[] pointBuckets;
  public float[][] dualPoints;
  public float[] radii;
  
  Brancher brancher;
  int artifact;

  public MedialAxisTransform(float[][] points) {

    if( points.length < 1 ){
      edges = new float[0][];
      return;
    }

    // build points array for qhull
    double[] qPoints = new double[ points.length*3 + 9 ];
    for(int i=0; i<points.length; i++){
      qPoints[i*3] = points[i][0];
      qPoints[i*3+1] = points[i][1];
      qPoints[i*3+2] = -(points[i][0]*points[i][0] + points[i][1]*points[i][1]); // standard half-squared eucledian distance
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

    // prepare quickhull
    QuickHull3D quickHull = new QuickHull3D(qPoints);
    int[][] faces = quickHull.getFaces(QuickHull3D.POINT_RELATIVE + QuickHull3D.CLOCKWISE);
    artifact = 0;
    
    InteriorTest poly = new InteriorTest(points);

    // compute dual points
    dualPoints = new float[faces.length][2];
    radii = new float[faces.length];
    for(int i = 0; i < faces.length; i++){

      // test if it's the artifact
      if( faces[i][0] >= points.length && faces[i][1] >= points.length && faces[i][2] >= points.length )
        artifact = i;

      double x0 = qPoints[faces[i][0]*3+0];
      double y0 = qPoints[faces[i][0]*3+1];
      double x1 = qPoints[faces[i][1]*3+0];
      double y1 = qPoints[faces[i][1]*3+1];
      double x2 = qPoints[faces[i][2]*3+0];
      double y2 = qPoints[faces[i][2]*3+1];

      double v1x = 2 * (x1-x0);
      double v1y = 2 * (y1-y0);
      double v1z = x0*x0 - x1*x1 + y0*y0 - y1*y1;

      double v2x = 2 * (x2-x0);
      double v2y = 2 * (y2-y0);
      double v2z = x0*x0 - x2*x2 + y0*y0 - y2*y2;

      double tmpx = v1y * v2z - v1z * v2y;
      double tmpy = v1z * v2x - v1x * v2z;
      double tmpz = v1x * v2y - v1y * v2x;

      dualPoints[i][0] = (float)(tmpx/tmpz);
      dualPoints[i][1] = (float)(tmpy/tmpz);
      
      if(faces[i][0] >= points.length || faces[i][1] >= points.length || faces[i][2] >= points.length ||
          !poly.contains((float)dualPoints[i][0], (float)dualPoints[i][1])) {
        // this triangle is outside the polygon, flag it to ignore
        radii[i] = -1;
      } else {
        double
        x01 = x0-x1, x12 = x1-x2, x20 = x2-x0,
        y01 = y0-y1, y12 = y1-y2, y20 = y2-y0,
        a = Math.sqrt(x01*x01 + y01*y01),
        b = Math.sqrt(x12*x12 + y12*y12),
        c = Math.sqrt(x20*x20 + y20*y20);
        radii[i] = (float)((a*b*c)/Math.sqrt((a+b+c)*(b+c-a)*(c+a-b)*(a+b-c))); // circumradius-o-clock
      }
    }

    // create edge/point/face network
    edges = new float[1][6];
    int edgeCount = 0;
    faceNet = new LinkedArray(faces.length);
    pointBuckets = new IntArray[points.length];
    for(int i = 0; i < points.length; i++) {
      pointBuckets[i] = new IntArray();
    }

    // discover edges
    for(int i = 0; i < faces.length; i++){

      // bin faces to the points they belong with
      for(int f=0; f<faces[i].length; f++) {
        if(faces[i][f] < points.length) {
          pointBuckets[ faces[i][f] ].add(i);
        }
      }

      // this looks like the slow part, quadratic?
      for(int j = 0; j < i; j++) {
        // && radii[i] > 0 && radii[j] > 0
        if( i != artifact && j != artifact && isEdgeShared(faces[i], faces[j])  && radii[i] > 0 && radii[j] > 0) {

          faceNet.link(i, j);

          if( edges.length <= edgeCount ){
            float[][] tmpedges = new float[edges.length*2][6];
            System.arraycopy(edges, 0, tmpedges, 0, edges.length);
            edges = tmpedges;
          }

          edges[edgeCount][0] = (float) dualPoints[i][0];
          edges[edgeCount][1] = (float) dualPoints[i][1];
          edges[edgeCount][2] = (float) radii[i];
          edges[edgeCount][3] = (float) dualPoints[j][0];
          edges[edgeCount][4] = (float) dualPoints[j][1];
          edges[edgeCount][5] = (float) radii[j];
          edgeCount++;

        }
      }
    }

    // trim edges down
    float[][] tmpedges = new float[edgeCount][4];
    System.arraycopy(edges, 0, tmpedges, 0, tmpedges.length);
    edges = tmpedges;
  }    

  public float[][] getEdges() {
    return edges;
  }
  
  float[][] spheres;
  public float[][] getSpheres() {
    if(spheres == null) {
      ArrayList<float[]> _spheres = new ArrayList<float[]>();
      for(float[] edge : edges) {
        float stepSize = 0.1f;
        for(float t = stepSize; t < 1; t += stepSize) {
          _spheres.add(lerpSphere(edge,t));          
        }
      }
      for(int i = 0; i < radii.length; i++) {
        if( i != artifact && radii[i] > 0) {
          _spheres.add(new float[]{dualPoints[i][0],dualPoints[i][1],radii[i]});
        }
      }
      spheres = new float[0][];
      spheres = _spheres.toArray(spheres);
    }
    return spheres;
  }
  
  private float[] lerpSphere(float[] edge, float t) {
    return new float[] {
        lerp(edge[0],edge[3],t),
        lerp(edge[1],edge[4],t),
        lerp(edge[2],edge[5],t)
    };
  }
  
  private float lerp(float a, float b, float t) {
    return a*(1-t) + b*t;
  }

  protected boolean isEdgeShared(int face1[], int face2[]){
    for(int i = 0; i < face1.length; i++){
      int cur = face1[i];
      int next = face1[(i + 1) % face1.length];
      for(int j = 0; j < face2.length; j++){
        int from = face2[j];
        int to = face2[(j + 1) % face2.length];
        if(cur == from && next == to || cur == to && next == from)
          return true;
      }
    }
    return false;
  }
  
  public ArrayList<ArrayList<float[]>> getBranches() {
    if(brancher == null) brancher = new Brancher();
    return brancher.branches;
  }
  
  public ArrayList<Junction> getJunctions() {
    if(brancher == null) brancher = new Brancher();
    return brancher.junctions;
  }
  
  public static class Junction {
    public float x,y,radius;
    /** array of x,y,r values of connected branches */
    public float[][] outgoing;
    Junction(float x, float y, float radius, float[][] outgoing) {
      this.x = x;
      this.y = y;
      this.radius = radius;
      this.outgoing = outgoing;
    }
  }

  private class Brancher {
    Set<Integer> junctionIndicies;
    Set<Integer> traversedJunctions;
    ArrayList<Junction> junctions;
    ArrayList<ArrayList<float[]>> branches;
    
    Brancher() {
      // TODO: should be able to remove this precompute step
      junctionIndicies = new HashSet<Integer>();
      junctions = new ArrayList<Junction>();
      for(int i = 0; i < faceNet.size(); i++) {
        LinkedIndex index = faceNet.get(i);
        int arity = index.getLinks().length;
        if(arity != 2 && radii[i] > 0) {
          junctionIndicies.add(i);
          int[] links = index.getLinks();
          float[][] outgoing = new float[links.length][3];
          for(int j = 0; j < outgoing.length; j++) {
            outgoing[j][0] = dualPoints[links[j]][0];
            outgoing[j][1] = dualPoints[links[j]][1];
            outgoing[j][2] = radii[links[j]];
          }
          junctions.add(new Junction(dualPoints[i][0], dualPoints[i][1], radii[i], outgoing));
        }
      }
      branches = new ArrayList<ArrayList<float[]>>();
      traversedJunctions = new HashSet<Integer>();
      
      // we need to try each of the junctionIndicies
      // later ones will probably be traversed already
      for(Integer i : junctionIndicies) {
        traverse(i, -1);
      }
    }
    
    void traverse(int junctionIndex, int ignoreDirection) {
      if(traversedJunctions.contains(junctionIndex)) return;
      traversedJunctions.add(junctionIndex);
      
      int[] links = faceNet.get(junctionIndex).getLinks();
      for(int i = 0; i < links.length; i++) {
        int j = links[i];
        int pj = junctionIndex;
        if(j == ignoreDirection) continue; // we arrived from one of the branches, which we should avoid retraversing
        if(j == junctionIndex) continue; // self loop :(
        if(j == 0) continue; // artifact?
        
        // make a new branch outgoing from this direction
        ArrayList<float[]> branch = new ArrayList<float[]>();
        branch.add(ball(junctionIndex));
        
        // continue until you arrive at one of the other junctionIndicies
        while(!junctionIndicies.contains(j)) {
          branch.add(ball(j));
          int[] moreLinks = faceNet.get(j).getLinks();
          if(moreLinks.length == 2) {
            // pick the correct edge, in the same direction
            if(moreLinks[0] == pj) {
              pj = j;
              j = moreLinks[1];
            } else if(moreLinks[1] == pj) {
              pj = j;
              j = moreLinks[0];
            } else {
              break;
            }
          } else {
            break; // too many
          }
        }
        branch.add(ball(j));
        branches.add(branch);
        
        traverse(j, pj); // traverse the new branch
      }
    }
    
    private float[] ball(int i) {
      return new float[] {dualPoints[i][0], dualPoints[i][1], radii[i]};
    }
  }
}
