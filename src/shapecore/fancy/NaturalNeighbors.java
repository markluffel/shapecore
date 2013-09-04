package shapecore.fancy;


import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import processing.core.PConstants;
import shapecore.ConvexHull;
import shapecore.Oplet;
import shapecore.pt;
import shapecore.vec;
import shapecore.mesh.CornerTable;


public class NaturalNeighbors {

  /** Locations to interpolate */
  public pt[] pts;

  /** Delaunay triangulation of input points */
  public CornerTable table;

  /** Circumcenters of delaunay triangles, verticies on voronoi diagram */
  public pt[] dual;
  
  public ConvexHull hull;
  
  /** Mapping from vertex to a corner on that vertex */
  public int[] C;
  
  /** A limitation to prevent infinite loops,
   * if this value is exceeded when traversing to find neighbors, an exception will be thrown.
   * This indicates that either 1) the mesh is malformed or 2) there are simply a large number of neighbors
   * */
  int maxNeighborhood;
  
  /** A reasonable limit. Unless the input points include a densely sampled circle this should be ok. */
  static int DEFAULT_MAX_NEIGHBORHOOD = 60;

  public static NaturalNeighbors build(pt[] pts) {
    if(pts != null && pts.length > 2) {
      return new NaturalNeighbors(pts);
    } else {
      return null;
    }
  }
  
  private NaturalNeighbors(pt[] pts) {
    this.pts = pts;
    this.maxNeighborhood = DEFAULT_MAX_NEIGHBORHOOD;
    calc();
  }
  
  public void calc() {
    table = CornerTable.delaunay(pts);
    hull = ConvexHull.fromDelaunay(table, pts);
    
    // compute an inverse for the V table
    // to accelerate lookup
    C = table.inverseV();
    if(pts.length < table.numVerts) {
      throw new IllegalArgumentException();
    }
    
    dual = new pt[table.O.length/3];
    for(int f = 0; f < table.V.length; f += 3) {
      pt center = circumcenter(pts[table.V[f]], pts[table.V[f+1]], pts[table.V[f+2]]);
      dual[f/3] = center;
    }    
  }
  
  ArrayList<pt> neighborhoodBoundary(float x, float y) {
    return neighborhoodBoundary(new pt(x,y));
  }
  
  public ArrayList<pt> neighborhoodBoundary(pt query) {
    ArrayList<pt> mine = new ArrayList<pt>();
    
    if(!insideHull(query)) return new ArrayList<pt>();
    
    int firstCorner = findFirstCorner(query);
    int c = firstCorner;

    pt invadee = pts[table.V[c]];
    int prevC;
    int i = 0;
    do {
      prevC = c;
      c = table.n(c);
      if(table.b(c)) { // on a boundary we inter
        // find corner of my neighborhood
        pt intr = intersection(invadee, query, dual[prevC/3], table.edgeNormal(c, pts));

        // next invadee
        c = table.n(c);
        invadee = pts[table.V[c]];

        // remember intersection
        mine.add(intr);
        
      } else {
        c = table.n(table.o(c));
        if(tooFarToInvade(c, invadee, query)) {
          // find corner of my neighborhood
          pt intr = intersection(invadee, query, dual[prevC/3], dual[c/3]);
  
          // next invadee
          c = table.p(prevC);
          invadee = pts[table.V[c]];
  
          // remember intersection
          mine.add(intr);
        }
      }
      i++;
    } while(c != firstCorner && i < maxNeighborhood);
    
    if(i >= maxNeighborhood) {
      throw new IllegalStateException("Exceeded neighborhood traversal limit, mesh is malformed or max neighborhood set too low.");
    }
    
    return mine;
  }
  
  public List<pt> overlapBoundary(pt query, int neighbor, int numExpected) {
    List<pt> corner = new LinkedList<pt>();
    List<Integer> belongsTo = new LinkedList<Integer>();
    
    if(!insideHull(query)) return new ArrayList<pt>();
    
    int firstCorner = findFirstCorner(query);
    int c = firstCorner;

    pt invadee = pts[table.V[c]];
    int prevC;
    int i = 0; // a loop counter
    int j = 0; // a neighbor counter
    do {
      corner.add(dual[c/3]);
      belongsTo.add(j%numExpected);
      
      prevC = c;
      c = table.n(c);
      if(table.b(c)) { // on a boundary we inter
        // find corner of my neighborhood
        pt intr = intersection(invadee, query, dual[prevC/3], table.edgeNormal(c, pts));

        // remember intersection
        corner.add(intr);
        belongsTo.add(j%numExpected);
        corner.add(intr);
        belongsTo.add((j+1)%numExpected);
        
        // next invadee
        c = table.n(c);
        invadee = pts[table.V[c]];
        j++;
        
      } else {
        c = table.n(table.o(c));
        if(tooFarToInvade(c, invadee, query)) {
          // find corner of my neighborhood
          pt intr = intersection(invadee, query, dual[prevC/3], dual[c/3]);
          
          // remember intersection
          corner.add(intr);
          belongsTo.add(j%numExpected);
          corner.add(intr);
          belongsTo.add((j+1)%numExpected);
          
          // next invadee
          c = table.p(prevC);
          invadee = pts[table.V[c]];
          j++;
        }
      }
      i++;
    } while(c != firstCorner && i < maxNeighborhood);
    
    if(i >= maxNeighborhood) {
      throw new IllegalStateException("Exceeded neighborhood traversal limit, mesh is malformed or max neighborhood set too low.");
    }
    
    Iterator<Integer> ii = belongsTo.iterator();
    Iterator<pt> pti = corner.iterator();
    while(pti.hasNext()) {
      pti.next();
      if(ii.next() != neighbor) {
        pti.remove();
      }
    }
    
    return corner;
  }

  public Neighborhood getNeighborhood(float x, float y) {
    return getNeighborhood(new pt(x,y));
  }

  public Neighborhood getNeighborhood(pt query) {
    return getNeighborhood(query, false);
  }
  
  public Neighborhood getNeighborhood(pt query, boolean returnNullIfDegenerate) {
    List<Integer> neighbors = new ArrayList<Integer>();
    List<Double> areaOverlaps = new ArrayList<Double>();
    
    
    if(!insideHull(query)) return null;
    
    int firstCorner = findFirstCorner(query);
    int c = firstCorner;
    
    neighbors.add(table.V[c]);
    pt invadee = pts[table.V[c]];
    pt prevIntersection = null;
    pt firstIntersection = null;
    double area = 0;
    int prevC;
    int i = 0;//MAX_NEIGHBORHOOD-3;
    do {
      pt intersection = null;
      prevC = c;
      c = table.n(c);
      if(table.b(c)) {
        // find corner of my neighborhood
        intersection = intersection(invadee, query, dual[prevC/3], table.edgeNormal(c, pts));
        c = table.p(prevC);

      } else {
        c = table.n(table.o(c));
        if(tooFarToInvade(c, invadee, query)) {
        
          // find corner of my neighborhood
          intersection = intersection(invadee, query, dual[prevC/3], dual[c/3]);
          c = table.p(prevC);
          
        } else {
          // add part of the neighborhood's intersection with this invadee's neighborhood
          area += doubleTrapezoidArea(dual[prevC/3], dual[c/3]);
          trace(invadee, dual[prevC/3], dual[c/3]);
        }
      }
      
      if(intersection != null) {
        // because our goal is a ratio, not the actual area, we can factor out a constant
        // in this case, we are computing twice the actual area
        double areaPiece = doubleTrapezoidArea(dual[prevC/3], intersection);
        area += areaPiece;
        
        if(prevIntersection == null) {
          firstIntersection = intersection;
        } else {
          area += doubleTrapezoidArea(intersection, prevIntersection);
        }
        
        // record the overlapping area (up to this point)
        // and record the vertex/neighbor index
        areaOverlaps.add(area);
        
        // next invadee
        invadee = pts[table.V[c]];
        neighbors.add(table.V[c]);
        prevIntersection = intersection;
        area = -areaPiece; // flipped relative to the last neighbor's overlap
      }
      
      i++;
    } while(c != firstCorner && i < maxNeighborhood);
    
    if(i >= maxNeighborhood) {
      if(returnNullIfDegenerate) {
        return null;
      } else {
        throw new IllegalStateException("Exceeded neighborhood traversal limit, mesh is malformed or max neighborhood set too low.");
      }
    }
    
    if(firstIntersection == null) {
      return null;
    }
    
    trace(invadee, firstIntersection, prevIntersection);
    area += doubleTrapezoidArea(firstIntersection, prevIntersection);
    area += areaOverlaps.get(0);
    areaOverlaps.set(0, area);
    
    // sum up the stolen area
    double totalArea = 0;
    for(double a : areaOverlaps) {
      totalArea += a;
    }
    totalArea = Math.abs(totalArea);
    
    float[] ratios = new float[areaOverlaps.size()];
    vec[] offsets = new vec[ratios.length];
    int[] naybors = new int[ratios.length]; // goofy name to prevent shadowing
    for(int j = 0; j < ratios.length; j++) {
      naybors[j] = neighbors.get(j); 
      ratios[j] = (float) (Math.abs(areaOverlaps.get(j).doubleValue()) / totalArea);
      offsets[j] = V(query, pts[naybors[j]]);
    }
    
    return new Neighborhood(ratios, offsets, naybors);
  }
  
  private boolean insideHull(pt query) {
    return hull.contains(query);
  }

  // subclass hook?
  protected void trace(pt invadee, pt L, pt r) {
  }
  
  public void drawVoronoi(Oplet p) {
    for(int i = 0; i < table.O.length; i++) {
      if(i < table.O[i]) {
        int j = table.O[i];
        p.line(
          dual[i/3],
          dual[j/3]
        );
      } else if(table.O[i] < 0) {
        vec direction = table.edgeNormal(i, pts);
        p.line(dual[i/3], T(dual[i/3], 100, direction));
      }
    }
  }
  

  /** check if the given point is within the wedge centered at the given corner,
   * with the edges of the wedge being given by the two sides of the triangle
   * that intersect at this corner
   */
  boolean inWedge(int corner, pt p) {
    if(corner > table.V.length || corner < 0) return false;
    pt v = pts[table.V[corner]],
    nv = pts[table.V[table.n(corner)]],
    pv = pts[table.V[table.p(corner)]];
    return isRightTurn(v, nv, p) && !isRightTurn(v, pv, p);
  }
  
  /**
   * firstCorner is a corner associated with the closest vertex to the query,
   * and is associated with the triangle containing the query
   * 
   */
  public int findFirstCorner(pt query) {
    // does a linear scan over the points to find the closest
    // TODO: accelerate this with a hash/tree
    int hoverIndex = closestPointIndex(pts, query.x, query.y);
    
    boolean hitBoundary = false;
    int c = C[hoverIndex];
    int iterations = 0;
    while(!inWedge(c, query) && iterations < 2000) {
      c = table.n(c);
      if(table.b(c)) { // hit a boundary before finding the right edge 
        hitBoundary = true;
        break;
      }
      c = table.n(table.o(c));
      iterations++;
    }
    
    if(hitBoundary) { // try in the opposite direction instead
      hitBoundary = false;
      c = C[hoverIndex];
      iterations = 0;
      while(!inWedge(c, query) && iterations < 2000) {
        c = table.p(c);
        if(table.b(c)) {
          hitBoundary = false;
          break;
        }
        c = table.p(table.o(c));
        iterations++;
      }  
    }
    
    if(hitBoundary) {
      throw new IllegalStateException("couldn't find the right wedge, maybe somebody was mutating some delaunay or dual data that they oughtn't have");
    }
    
    return c;
  }
  
  public int findTrueCorner(pt query) {
    int numFaces = table.numFaces();
    for(int t = 0; t < numFaces; t++) {
      if(table.inTriangle(pts, t, query)) {
        float d1 = pts[table.V[t*3+0]].sqdist(query);
        float d2 = pts[table.V[t*3+1]].sqdist(query);
        float d3 = pts[table.V[t*3+2]].sqdist(query);
        if(d1 < d2) {
          if(d2 < d3) {
            return t*3+0;
          } else {
            return t*3+2;
          }
        } else {
          if(d2 < d3) {
            return t*3+1; 
          } else {
            return t*3+2;
          }
        }
      }
    }
    return -1;
  }
  

  static pt intersection(pt invadee, pt invader, pt voronoi1, pt voronoi2) {
    return lineIntersection(
        average(invader,invadee), R(invader,invadee),
        voronoi1, V(voronoi1,voronoi2));
  }
  
  private pt intersection(pt invadee, pt invader, pt voronoi1, vec edgeNormal) {
    return lineIntersection(
        average(invader,invadee), R(invader,invadee),
        voronoi1, edgeNormal);
  }

  
  /** checks if the voronoi region vertex associated with this corner is closer to the invadee than the invader (the query)
   * if so we know that between this voronoi region vert and the previous one there will be an intersection
   */
  boolean tooFarToInvade(int corner, pt invadee, pt query) {
    pt voronoiRegionVertex = dual[corner/3];
    return invadee.sqdist(voronoiRegionVertex) < query.sqdist(voronoiRegionVertex); 
  }
  
  public static class Neighborhood {

    public int[] neighbors;
    public float[] ratios;
    public vec[] offsets;

    public Neighborhood(float[] ratios, vec[] offsets, int[] neighbors) {
      this.ratios = ratios;
      this.offsets = offsets;
      this.neighbors = neighbors;
    }
    
    /**
     * Union all the neighbors, average their ratios
     * 
     */
    public static Neighborhood blend(Neighborhood... ns) {
      Map<Integer,Accum> neighborUnion = new HashMap<Integer,Accum>();
      for(Neighborhood n : ns) {
        for(int i = 0; i < n.neighbors.length; i++) {
          Accum a = neighborUnion.get(n.neighbors[i]);
          if(a == null) {
            a = new Accum();
            neighborUnion.put(n.neighbors[i], a);
          }
          a.ratio += n.ratios[i];
          a.offset.add(n.offsets[i]);
          a.count++;
        }
      }
      Neighborhood result = new Neighborhood(neighborUnion.size());
      int i = 0;
      for(Map.Entry<Integer,Accum> e : neighborUnion.entrySet()) {
        result.neighbors[i] = e.getKey();
        Accum a = e.getValue();
        result.ratios[i] = a.ratio/a.count;
        a.offset.scaleBy(1/a.count);
        result.offsets[i] = a.offset;
        i++;
      }
      return result;
    }
    
    private static class Accum {
      float ratio;
      vec offset; // probably ignored
      int count;
      Accum() {
        offset = new vec();
      }
    }

    private Neighborhood(int size) {
      this.ratios = new float[size];
      this.neighbors = new int[size];
      this.offsets = new vec[size];
    }
    
    /**
     * arithmetic mean
     * this assumes that the ratios sum to 1
     */
    public float interpolate(float[] values) {
      float result = 0;
      for(int i = 0; i < neighbors.length; i++) {
        result += values[neighbors[i]]*ratios[i];
      }
      return result;
    }

    /**
     * arithmetic mean
     * this also assumes that the ratios sum to 1
     */
    public float geometricInterpolate(float[] values) {
      float logSum = 0;
      for(int i = 0; i < neighbors.length; i++) {
        logSum += Math.log(values[neighbors[i]])*ratios[i];
      }
      return (float) Math.exp(logSum);
    }
    
    /**
     * Interpolate values using both natural neighbors and gradients.
     * 
     * @param values
     * @param gradients
     * @return a C1 smooth interpolated value
     */
    public float interpolate(float[] values, vec[] gradients) {
      float result = 0;
      float normalizer = 0;
      for(int i = 0; i < neighbors.length; i++) {
        float weight = ratios[i]/offsets[i].norm(); // h_i(x)*d(x,x_i)^-1
        normalizer += weight;
        result += (values[neighbors[i]] + gradients[neighbors[i]].dot(offsets[i]))*weight; // z_i + \nabla 
      }
      result /= normalizer;
      return result;
    }
    
    public pt interpolate(pt[] values) {
      float x = 0;
      float y = 0;
      for(int i = 0; i < neighbors.length; i++) {
        x += values[neighbors[i]].x*ratios[i];
        y += values[neighbors[i]].y*ratios[i];
      }
      return new pt(x,y);
    }
    
    public vec interpolate(vec[] values) {
      float x = 0;
      float y = 0;
      for(int i = 0; i < neighbors.length; i++) {
        x += values[neighbors[i]].x*ratios[i];
        y += values[neighbors[i]].y*ratios[i];
      }
      return new vec(x,y);
    }
    
    public pt interpolateNullable(pt[] values) {
      float x = 0;
      float y = 0;
      float normalizer = 0;
      for(int i = 0; i < neighbors.length; i++) {
        pt val = values[neighbors[i]];
        if(val != null) {
          x += val.x*ratios[i];
          y += val.y*ratios[i];
          normalizer += ratios[i];
        }
      }
      x /= normalizer;
      y /= normalizer;
      return new pt(x,y);
    }
    
    // UHHH: Is this gradients tuple array just a Jacobian?
    public pt interpolate(pt[] values, vec[][] gradients) {
      float x = 0, y = 0;
      float normalizerX = 0;
      float normalizerY = 0;
      for(int i = 0; i < neighbors.length; i++) {
        float weightX = ratios[i]/offsets[i].norm();
        float weightY = ratios[i]/offsets[i].norm();
        normalizerX += weightX;
        normalizerY += weightY;
        x += (values[neighbors[i]].x + gradients[neighbors[i]][0].dot(offsets[i]))*weightX;
        y += (values[neighbors[i]].y + gradients[neighbors[i]][1].dot(offsets[i]))*weightY;
      }
      x /= normalizerX;
      y /= normalizerY;
      return new pt(x,y);
    }
  }

  public void drawVoronoiCell(Oplet p, int corner) {
    int firstCorner = corner;
    p.beginShape();
    boolean hitBoundary = false;
    do {
      p.vertex(dual[corner/3]);
      corner = table.n(corner);
      if(table.b(corner)) {
        hitBoundary = true;
        break; // TODO: traverse 
      }
      corner = table.n(table.o(corner));
    } while(corner != firstCorner);
    
    if(hitBoundary) {
      vec toInfinity = table.edgeNormal(corner, pts);
      p.vertex(T(dual[corner/3], 100, toInfinity));
      corner = firstCorner;
      while(!table.b(table.p(corner))) {
        corner = table.pop(corner);
      }
      corner = table.p(corner);
      toInfinity = table.edgeNormal(corner, pts);
      p.vertex(T(dual[corner/3], 100, toInfinity));
      corner = table.n(corner);
      while(corner != firstCorner) {
        p.vertex(dual[corner/3]);
        corner = table.non(corner);
      }
    }
    p.endShape(PConstants.CLOSE);
  }
}
