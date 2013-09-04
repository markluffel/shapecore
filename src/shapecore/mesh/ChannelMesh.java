package shapecore.mesh;

import static processing.core.PApplet.*;
import static shapecore.Geometry.*;
import static shapecore.Oplet.*;
import shapecore.pt3;

/**
 * A class for storing quantities on the edges and vertices of a mesh.
 * 
 */
public class ChannelMesh extends Mesh3D {
  
  // a light coating of type safety
  public interface EdgeEnum { int ordinal(); }
  public interface VertexEnum { int ordinal(); }
  
  public interface EdgeCriterion {
    float diff(pt3[] G, float[][] VQ, float[][] EQ, int vi, int vj);
  }
  
  float[][] VQ; // vertex channel quantities
  float[][] EQ; // edge channel quantities
  
  public EdgeEnum restLengthEnum;
  
          
  public ChannelMesh(pt3[] verts, float[][] vertexChannels, CornerTable corner) {
    this(verts, vertexChannels, new float[0][0], corner);
  }

  public ChannelMesh(pt3[] verts, float[][] vertexChannels, int[][] tris) {
    this(verts, vertexChannels, new float[0][0], tris);
  }

  public ChannelMesh(pt3[] verts, float[][] vertexChannels, float[][] edgeChannels, CornerTable corner) {
    super(verts, corner);
    this.VQ = vertexChannels;
    this.EQ = edgeChannels;
    simpleSanityCheck();
  }

  public ChannelMesh(pt3[] verts, float[][] vertexChannels, float[][] edgeChannels, int[][] tris) {
    super(verts, tris);
    this.VQ = vertexChannels;
    this.EQ = edgeChannels;
    simpleSanityCheck();
  }
  
  void simpleSanityCheck() {
    for(int h = 0; h < VQ.length; h++) {
      if(VQ[h].length != G.length) {
        throw new IllegalArgumentException("(vertex) scalar field array and vertex array lengths don't match");
      }
    }
    for(int h = 0; h < EQ.length; h++) {
      if(EQ[h].length != corner.V.length) {
        throw new IllegalArgumentException("(edge) scalar field array and corner array lengths don't match");
      }
    }
  }
  
  public float vertexArea(int v) {
    float sum = 0;
    for(int c : vertexCorners(v)) {
      // TODO: have mathematica create a more efficient version of this
      pt3
      pa = G[corner.V[c]],
      pb = G[corner.V[n(c)]],
      pc = G[corner.V[p(c)]],
      pd = average(pa,pb,pc),
      pe = average(pa,pb),
      pf = average(pa,pc);
      sum += triangleArea(pa,pe,pd)+triangleArea(pa,pd,pf);
    }
    return sum;
  }
  
  public float maxEdgeLength() {
    float maxSq = 0;
    int[] O = corner.O;
    for(int c = 0; c < O.length; c++) {
      if(c > O[c]) {
        maxSq = max(maxSq, this.edgeLengthSq(c));
      }
    }
    return sqrt(maxSq);
  }
  
  public int valence(int vertex) {
    int count = 0;
    for(int _ : oneRing(vertex)) count++;
    return count;
  }
  
  // simple interface, there may be no good reason to have anything fancier
  public ChannelMesh splitEdges(float maxEdgeLength) {
    return splitEdges(new EdgeCriterion() {
      public float diff(pt3[] G, float[][] VQ, float[][] EQ, int vi, int vj) {
        return dist(G[vi],G[vj]);
      }
    }, maxEdgeLength);
  }
  
  // the heart of the whole operation
  public ChannelMesh splitEdges(EdgeCriterion ec, float threshold) {
    int[] O = corner.O, V = corner.V;
    int nV = numPoints();
    int nT = numFaces();
    int nC = nT*3;
    
    // use criterion to find which edges to split
    // record # of created triangles and vertices 
    boolean[] splits = new boolean[nC];
    for(int c = 0; c < nC; c++) {
      if(c > O[c]) {
        if(ec.diff(G,VQ,EQ, V[n(c)], V[p(c)]) > threshold) {
          splits[c] = true;
          nT++;
          nV++;
          if(O[c] >= 0) {
            splits[O[c]] = true;
            nT++;
          }
        }
      }
    }
    
    // allocate the new stuff
    CornerTable ct = new CornerTable(nT);
    pt3[] newG = new pt3[nV];
    float[][] newVQ = new float[VQ.length][nV];
    float[][] newEQ = new float[EQ.length][nT*3];
    // copy old stuff into new stuff
    System.arraycopy(G, 0, newG, 0, G.length); // geometry
    System.arraycopy(V, 0, ct.V, 0, V.length); // connectivity
    for(int h = 0; h < newVQ.length; h++) {
      System.arraycopy(VQ[h], 0, newVQ[h], 0, G.length); // per vertex channels
    }
    for(int h = 0; h < newEQ.length; h++) {
      System.arraycopy(EQ[h], 0, newEQ[h], 0, V.length); // per corner/half-edge channels
    }
    // collect it all together
    ChannelMesh cm = new ChannelMesh(newG, newVQ, newEQ, ct);
    
    // interpolate old values to new locations 
    nV = G.length;
    int[] cToV = new int[nC]; // old nC
    for(int c = 0; c < nC; c++) {
      if(splits[c]) {
        if(c < O[c] || O[c] < 0) {
          // end points
          int v1 = V[n(c)];
          int v2 = V[p(c)];
          // interpolate vertex location
          newG[nV] = lerp(G[v1], G[v2], 0.5f);
          // interpolate scalar fields at vertex
          for(int i = 0; i < VQ.length; i++) {
            newVQ[i][nV] = lerp(VQ[i][v1], VQ[i][v2], 0.5f);
          }
          // bookkeeping info for later
          cToV[c] = nV;
          if(O[c] >= 0) cToV[O[c]] = nV;
          nV++;
        }
      }
    }
    
    int newC = numFaces()*3;
    for(int c = 0; c < nC; c += 3) {
      boolean split0 = splits[c+0], split1 = splits[c+1], split2 = splits[c+2]; 
      int numSplit = (split0?1:0) + (split1?1:0) + (split2?1:0);
      
      float stretch = 1;
      if(numSplit > 0 && restLengthEnum != null) {
        stretch = triangleStretch(c);
      }
      
      if(numSplit == 1) {
        int cor = -1; // which corner to split
        if(split0) cor = c+0;
        else if(split1) cor = c+1;
        else if(split2) cor = c+2;
        int svi = cToV[cor]; // get the index of the vertex created by splitting this corner
        // compute new edge quantities for split and created edges
        //float[] halfEQ = halfEQ(cor);
        //float[] interpEQ = interpolateEQ1(cor);
        // replace this triangle with a new one
        ct.V[c+0] = V[p(cor)];
        ct.V[c+1] = V[cor];
        ct.V[c+2] = svi;
        // set the edge quantities for this triangle
        //setEQ(newEQ, c+0, interpEQ);
        //setEQ(newEQ, c+1, halfEQ);
        //setEQ(newEQ, c+2, n(cor));
        setTriangleStretch(cm, c, stretch);
        
        // add a new triangle
        ct.V[newC+0] = svi;
        ct.V[newC+1] = V[cor];
        ct.V[newC+2] = V[n(cor)];
        // set the edge quantities
        //setEQ(newEQ, newC+0, p(cor));
        //setEQ(newEQ, newC+1, halfEQ);
        //setEQ(newEQ, newC+2, interpEQ);
        setTriangleStretch(cm, newC, stretch);
        
        newC += 3; // where to continue adding more triangles
        
      } else if(numSplit == 2) {
        int cor = -1; // which corner not to split
        if(!split0) cor = c+0;
        else if(!split1) cor = c+1;
        else if(!split2) cor = c+2;
        int nsvi = cToV[n(cor)]; // vertex index for splitting next corner  
        int psvi = cToV[p(cor)]; // vertex index created by splitting previous corner
        
        float nextEdgeLen = ec.diff(newG, newVQ, newEQ, V[n(c)], nsvi);
        float prevEdgeLen = ec.diff(newG, newVQ, newEQ, V[p(c)], psvi);
        
        // clear rest lengths, or whatev
        for(int i = 0; i < 3; i++) clearEQ(newEQ, c+i);
        for(int i = 0; i < 6; i++) clearEQ(newEQ, newC+i);
        
        // replace this triangle with a new one
        ct.V[c+0] = V[cor];
        ct.V[c+1] = psvi;
        ct.V[c+2] = nsvi;
        setTriangleStretch(cm, c, stretch);
        
        if(nextEdgeLen < prevEdgeLen) {
          ct.V[newC+0] = psvi;
          ct.V[newC+1] = V[n(cor)];
          ct.V[newC+2] = nsvi;
          setTriangleStretch(cm, newC, stretch);
          newC += 3;
          ct.V[newC+0] = nsvi;
          ct.V[newC+1] = V[n(cor)];
          ct.V[newC+2] = V[p(cor)];
          setTriangleStretch(cm, newC, stretch);
          newC += 3;
        } else {
          ct.V[newC+0] = nsvi;
          ct.V[newC+1] = psvi;
          ct.V[newC+2] = V[p(cor)];
          setTriangleStretch(cm, newC, stretch);
          newC += 3;
          ct.V[newC+0] = V[p(cor)];
          ct.V[newC+1] = psvi;
          ct.V[newC+2] = V[n(cor)];
          setTriangleStretch(cm, newC, stretch);
          newC += 3;
        }
        
      } else if(numSplit == 3) {
        int cvi = cToV[c+0], nvi = cToV[c+1], pvi = cToV[c+2];
        
        // clear rest lengths, etc
        for(int i = 0; i < 3; i++) clearEQ(newEQ, c+i);
        for(int i = 0; i < 9; i++) clearEQ(newEQ, newC+i);
        
        // update existing
        ct.V[c+0] = nvi;
        ct.V[c+1] = pvi;
        ct.V[c+2] = cvi;
        setTriangleStretch(cm, c, stretch);
        
        ct.V[newC+0] = V[c+0];
        ct.V[newC+1] = pvi;
        ct.V[newC+2] = nvi;
        setTriangleStretch(cm, newC, stretch);
        newC += 3;
        
        ct.V[newC+0] = V[c+1];
        ct.V[newC+1] = cvi;
        ct.V[newC+2] = pvi;
        setTriangleStretch(cm, newC, stretch);
        newC += 3;
        
        ct.V[newC+0] = V[c+2];
        ct.V[newC+1] = nvi;
        ct.V[newC+2] = cvi;
        setTriangleStretch(cm, newC, stretch);
        newC += 3;
      }
    }
    ct.numVerts = nV;
    ct.recomputeO();
    
    cm.restLengthEnum = restLengthEnum; // FIXME
    return cm;
  }
  
  float triangleStretch(int c) {
    float[] restLength = EQ(restLengthEnum);
    return geometricAverage(
        edgeLength(c+0)/restLength[c+0],
        edgeLength(c+1)/restLength[c+1],
        edgeLength(c+2)/restLength[c+2]
        );
  }
  
  float geometricAverage(float... values) {
    float logSum = 0;
    for(float val : values) {
      logSum += log(val);
    }
    return exp(logSum);
  }
  
  void setTriangleStretch(ChannelMesh cm, int c, float stretch) {
    if(restLengthEnum == null) return;
    float[] restLength = cm.EQ(restLengthEnum);
    restLength[c+0] = cm.edgeLength(c+0)*stretch;
    restLength[c+1] = cm.edgeLength(c+1)*stretch;
    restLength[c+2] = cm.edgeLength(c+2)*stretch;
  }

  // interpolate values onto the new edge between the vertex at this corner and the opposite edge
  private float[] interpolateEQ1(int cor) {
    float[] result = new float[EQ.length];
    for(int h = 0; h < EQ.length; h++) {
      result[h] = splitEdgeLengthC(EQ[h][n(cor)], EQ[h][p(cor)], EQ[h][cor]);
    }
    return result;
  }

  // interpolate values onto the new edge between the midpoints
  // of the edges meeting at this corner
  private float[] interpolateEQ2(int cor) {
    float[] result = new float[EQ.length];
    for(int h = 0; h < EQ.length; h++) {
      result[h] = splitEdgeLengthAB(EQ[h][n(cor)], EQ[h][p(cor)], EQ[h][cor]);
    }
    return result;
  }

  // get the edges values opposite the given corner 
  private float[] halfEQ(int cor) {
    float[] result = new float[EQ.length];
    for(int h = 0; h < EQ.length; h++) {
      result[h] = EQ[h][cor]/2;
    }
    return result;
  }

  // set the edge values at the dst edge to the given array
  private void setEQ(float[][] newEQ, int dst, float[] values) {
    for(int h = 0; h < EQ.length; h++) {
      newEQ[h][dst] = values[h];
    }
  }

  // set the edges values at the dst edge to be the same as those from the existing array at the src location
  private void setEQ(float[][] newEQ, int dst, int src) {
    for(int h = 0; h < EQ.length; h++) {
      newEQ[h][dst] = EQ[h][src];
    }
  }

  public void sharedBorderWeights(float[] weights) {
    int[] O = corner.O;
    int[] V = corner.V;
    for(int c = 0; c < O.length; c++) {
      if(c > O[c]) {
        pt3 pa = G[V[p(c)]], pb = G[V[c]], pc = G[V[n(c)]];
        float value = S(1/6f,pa, 1/6f, pc, -1/3f, pb)._norm();
        if(O[c] >= 0) {
          pt3 D = G[V[O[c]]];
          value += S(1/6f,pa, 1/6f, pc, -1/3f, D)._norm();
          weights[O[c]] = value;
        }
        weights[c] = value;
      }
    }
  }
  
  public void computeCurvatures(VertexEnum curvatureChan) {
    float[] curv = VQ(curvatureChan);
    for(int v = 0; v < numPoints(); v++) {
      curv[v] = curvature(v).norm();
    }
  }

  // get vertex channel given enum
  public float[] VQ(VertexEnum u) {
    return VQ[u.ordinal()];
  }
  
  // get edge channel given enum
  public float[] EQ(EdgeEnum u) {
    return EQ[u.ordinal()];
  }

  // allocate memory for channels
  public void allocateChannels(Class<? extends VertexEnum> vEnum, Class<?extends EdgeEnum> eEnum) {
    VQ = new float[vEnum.getEnumConstants().length][G.length];
    EQ = new float[eEnum.getEnumConstants().length][corner.V.length];
  }

  static void clearEQ(float[][] _EQ, int c) {
    for(int h = 0; h < _EQ.length; h++) {
      _EQ[h][c] = 0;
    }
  }
  
  // set all the vertex channels
  public void setVQ(float[][] _VQ) {
    for(int h = 0; h < _VQ.length; h++) {
      if(_VQ[h].length != numPoints()) {
        throw new IllegalArgumentException("vertex channel does not have the correct length");
      }
    }
    VQ = _VQ;
  }

  // set all the edge channels
  public void setEQ(float[][] _EQ) {
    for(int h = 0; h < _EQ.length; h++) {
      if(_EQ[h].length != numCorners()) {
        throw new IllegalArgumentException("edge channel does not have the correct length");
      }
    }
    EQ = _EQ;
  }

  // swap to vertex channels
  public void swapVQ(VertexEnum a, VertexEnum b) {
    int ai = a.ordinal(), bi = b.ordinal();
    float[] temp = VQ[ai];
    VQ[ai] = VQ[bi];
    VQ[bi] = temp;
  }

}
