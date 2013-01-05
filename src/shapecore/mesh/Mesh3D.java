package shapecore.mesh;

import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import shapecore.BoundingBox3;
import shapecore.Oplet;
import shapecore.pt3;
import shapecore.vec3;
import shapecore.mesh.CornerTable.OneRingIterator;
import shapecore.mesh.CornerTable.StarLinkEdge;
import shapecore.mesh.CornerTable.VertexIterator;



public class Mesh3D {
  
  // these shouldn't be public, there are too many moving pieces
  // but... we'll deal with it
  public CornerTable corner;
  
  /** Mapping from the vertex indicies onto a corner incident at that vertex */
  public int[] C;
  public pt3[] G;
  
  // extra info, usually null
  public vec3[] faceNormals;
  public vec3[] vertexNormals;
  public vec3[] curvatures; // at vertices
  
  public Mesh3D(pt3[] geom, int[][] triangles) {
    this.G = geom;
    this.corner = new CornerTable(triangles);
  }
  
  public Mesh3D(List<pt3> geom, List<int[]> triangles) {
    this.G = geom.toArray(new pt3[0]);
    this.corner = new CornerTable(triangles);
  }
  
  public Mesh3D(pt3[] geom, CornerTable corner) {
    this.G = geom;
    this.corner = corner;
    corner.numVerts = geom.length; // very trusting, we could scan the V table to make sure
  }

  public static Mesh3D load(String name) {
    return load(new File(name));
  }
  
  public static Mesh3D load(File file) {
    if(!file.exists()) throw new IllegalArgumentException();
    try {
      String name = file.getName();
      InputStream is = new FileInputStream(file);
      if(name.endsWith(".gz")) {
        name = name.substring(0, name.length()-3);
        is = new GZIPInputStream(is);
      }
      
      BufferedReader reader = new BufferedReader(new InputStreamReader(is)); 
      
      if(name.endsWith(".obj")) {
        return loadObj(reader);
      } else if(name.endsWith(".ply")) {
        return loadPly(reader);
      } else {
        throw new IllegalArgumentException("unknown format");
      }
      
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  protected static Mesh3D loadObj(BufferedReader reader) throws IOException {
    List<pt3> points = new ArrayList<pt3>();
    List<int[]> faceIndices = new ArrayList<int[]>();
    boolean seenO = false;
    for(String line = reader.readLine(); line != null; line = reader.readLine()) {
      if(line.length() < 1) continue;
      char c = line.charAt(0);
      if(c == 'v') {
        points.add(readObjVertex(line));
      } else if(c == 'f') {
        faceIndices.add(readObjFace(line));
      } else if(c == 'o') {
        if(seenO) throw new IllegalArgumentException("too many objects in this file");
        seenO = true;
      }
    }
    return new Mesh3D(points, faceIndices);
  }
  
  private static pt3 readObjVertex(String line) {
    String[] parts = line.split(" ");
    if(parts.length != 4) throw new IllegalStateException("too much stuff on v line");
    return new pt3(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
  }
  
  private static int[] readObjFace(String line) {
    String[] parts = line.split(" ");
    if(parts.length != 4) throw new IllegalStateException("we only support triangle meshes currently");
    return new int[]{
        Integer.parseInt(parts[1])-1,
        Integer.parseInt(parts[2])-1,
        Integer.parseInt(parts[3])-1};
  }
  
  public void saveVst(String fname) {
    saveVst(new File(fname));
  }
  
  class DumbOutputStream {
    BufferedOutputStream out;
    DumbOutputStream(OutputStream os) {
      if(os instanceof BufferedOutputStream) {
        this.out = (BufferedOutputStream) os;
      } else {
        this.out = new BufferedOutputStream(os);
      }
    }
    public void writeInt(int v) throws IOException {
      out.write((v >>>  0) & 0xFF);
      out.write((v >>>  8) & 0xFF);
      out.write((v >>> 16) & 0xFF);
      out.write((v >>> 24) & 0xFF);
    }
    public void writeFloat(float v) throws IOException {
      writeInt(Float.floatToIntBits(v));
    }
    public void close() throws IOException {
      out.close();
    }
  }
  
  public boolean saveVst(File file) {
    try {
      DumbOutputStream os = new DumbOutputStream(new FileOutputStream(file));
      os.writeInt(0x00747376); // magic
      os.writeInt(1);
      os.writeInt(numPoints());
      os.writeInt(numFaces());
      int[] cv = corner.inverseV();
      for(int i = 0; i < G.length; i++) {
        os.writeInt(cv[i]<<1); // one bit for boundary in index
        os.writeFloat(G[i].x);
        os.writeFloat(G[i].y);
        os.writeFloat(G[i].z);
      }
      int nt = numFaces();
      for(int t = 0; t < nt; t++) {
        int c = t*3;
        os.writeInt(corner.V[c+0]<<1);
        os.writeInt(corner.V[c+1]<<1);
        os.writeInt(corner.V[c+2]<<1);
        os.writeInt(corner.s(c+0)<<1);
        os.writeInt(corner.s(c+1)<<1);
        os.writeInt(corner.s(c+2)<<1);
      }
      os.close();
      
      return true;
    } catch(IOException ioe) {
      ioe.printStackTrace();
      return false;
    }
  }
  
  protected static Mesh3D loadPly(BufferedReader is) {
    return null;
  }
  
  public void draw(Oplet p) {
    corner.draw(p, G);
  }
  
  public void draw(Oplet p, int[] colors) {
    corner.draw(p, G, colors);
  }
  
  public void draw(Oplet p, int[] colors, boolean[] mask) {
    corner.draw(p, G, colors, mask);
  }
  
  public void drawSmooth(Oplet p) {
    computeVertexNormals();
    corner.drawWithNormals(p, G, vertexNormals);
  }
  
  public void drawFaceNormals(Oplet p, float len) {
    if(faceNormals == null || faceNormals.length != numFaces()) computeFaceNormals();
    for(int t = 0; t < numFaces(); t++) {
      p.arrow(faceCenter(t), len, faceNormals[t]);
    }
  }
  
  public void drawVertexNormals(Oplet p, float len) {
    if(vertexNormals == null || vertexNormals.length != G.length) computeVertexNormals();
    for(int v = 0; v < G.length; v++) {
      p.arrow(G[v], len, vertexNormals[v]);
    }
  }
  
  public void drawCurvatures(Oplet p, float len) {
    if(curvatures == null || curvatures.length != G.length) computeCurvatures();
    for(int v = 0; v < G.length; v++) {
      if(isBorderVertex(v)) {
        p.stroke(255,0,0);
        //p.arrow(G[v], len, curvatures[v]);
      } else {
        p.stroke(0);
        p.arrow(G[v], len, curvatures[v]);
      }
    }
  }
  
  public void subdivide() {
    G = corner.subdivide(G);
    C = null;
    faceNormals = null;
  }
  
  public void computeFaceNormals() {
    if(faceNormals == null || faceNormals.length != corner.numFaces()) {
      faceNormals = new vec3[corner.numFaces()];
    }
    for(int t = 0; t < faceNormals.length; t++) {
      faceNormals[t] = faceNormal(t);
    }
  }
  
  public void computeVertexNormals() {
    if(vertexNormals == null || vertexNormals.length != G.length) {
      vertexNormals = new vec3[G.length];
    }
    for(int v = 0; v < vertexNormals.length; v++) {
      vertexNormals[v] = vertexNormal(v);
      vertexNormals[v].normalize();
    }
  }
  
  public void computeCurvatures() {
    if(curvatures == null || curvatures.length != G.length) {
      curvatures = new vec3[G.length];
    }
    for(int v = 0; v < curvatures.length; v++) {
      curvatures[v] = curvature(v);
    }
  }
  
  
  public int valence(int c) {
    int i = 0;
    for(Object _ : corner.vertexCorners(c)) i++;
    return i;
  }
  
  // unnormalized, perhaps ironically
  public vec3 vertexNormal(int v) {
    vec3 normal = new vec3();
    pt3 A = G[v];
    // TODO: change the naming scheme, aroundVerts, aroundVertsAtCorner, aroundCornersAtVert
    for(int crnr : corner.vertexCorners(cornerForVertex(v))) {
      int bv = corner.V[corner.n(crnr)];
      pt3 B = G[bv];
      int cv = corner.V[corner.p(crnr)];
      pt3 C = G[cv];
      
      normal.add(V(A,B).cross(V(A,C)));
    }
    return normal;
  }
  
  public vec3 cornerNormal(int c) {
    return faceNormal(c/3);
  }
  
  public vec3 faceNormal(int t) {
  return triNormal(
      G[corner.V[t*3+0]],
      G[corner.V[t*3+1]],
      G[corner.V[t*3+2]]).normalize();
  }
  
  public pt3 faceCenter(int t) {
    return A(
      G[corner.V[t*3+0]],
      G[corner.V[t*3+1]],
      G[corner.V[t*3+2]]);
  }
 
  public static vec3 triNormal(pt3 a, pt3 b, pt3 c) {
    return V(a,b).cross(V(a,c));
  }
  

  public int cornerForVertex(int v) {
    if(C == null) {
      C = corner.inverseV();
    }
    return C[v];
  }
  
  public OneRingIterator oneRing(int vertex) {
    return corner.oneRing(cornerForVertex(vertex));
  }
  
  public Iterable<StarLinkEdge> starLink(int vertex) {
    return corner.starLink(cornerForVertex(vertex));
  }
  
  public VertexIterator vertexCorners(int vertex) {
    return corner.vertexCorners(cornerForVertex(vertex));
  }
  
  public float[] curvatures() {
    float[] result = new float[G.length];
    for(int i = 0; i < G.length; i++) {
      result[i] = curvature(i).mag();
    }
    return result;
  }
  
  public vec3 curvature(int v) {
    return _curvature(v); // this looks very wrong, maybe the iterator is broken?
    //return _curvatureUniform(v); // a naive version with uniform weights and a simpler itera
  }
  
  // based on Desbrun et al 99
  public vec3 _curvature(int v) {
    vec3 sum = new vec3();
    
    float normalizer = 0;
    float contrib;
    pt3 C = G[v];
    for(int c : vertexCorners(v)) {
      pt3 A = G[corner.V[p(c)]], B = G[corner.V[n(c)]];
      // cotan angle at A, vector to B
      contrib = cotAlpha(C,A,B); normalizer += contrib;
      sum.add(contrib, V(C,B));
      // cotan angle at B, vector to A
      contrib = cotAlpha(C,B,A); normalizer += contrib;
      sum.add(contrib, V(C,A));
    }
    
    sum.mul(1f/normalizer);
    return sum;
  }
  
  public vec3 _curvatureUniform(int v) {
    vec3 sum = new vec3();
    
    float num = 0;
    for(int j : oneRing(v)) {
      sum.add(V(G[v],G[j]));
      num++;
    }
    
    sum.mul(1f/num);
    return sum;
  }
  
  public float cotan(int c) {
    return cotAlpha(G[corner.V[p(c)]], G[corner.V[c]], G[corner.V[n(c)]]);
  }
  
  // the cotangent weight for the edge associated with this corner
  public float cotanSum(int c) {
    if(corner.O[c] < 0) {
      return cotan(c); // x2 ??
    } else {
      return cotan(c)+cotan(corner.O[c]);
    }
  }
    
  public List<Path> silhouettes(vec3 camera) {
    return new Silhouettes().find(camera);
  }
  
  private class Silhouettes {
    boolean[] forward;
    boolean[] visited;
    
    List<Path> find(vec3 camera) {
      forward = new boolean[corner.numFaces()];
      for(int i = 0; i < forward.length; i++) {
        forward[i] = camera.dot(faceNormal(i)) > 0;
      }
      List<Path> paths = new ArrayList<Path>();
      // one for each edge
      visited = new boolean[corner.V.length];
      for(int c = 0; c < corner.V.length; c++) {
        if(!visited[c] && silhouetteEdge(c)) {
          Path p = new Path();
          setVisited(c);
          tracePath(p, corner.n(c));
          p.reverse();
          tracePath(p, corner.p(c));
          paths.add(p);
        }
      }
      
      return paths;
    }
    
    void setVisited(int c) {
      visited[c] = true;
      visited[corner.o(c)] = true;
    }
    
    void tracePath(Path p, int c) {
      while(!visited[c]) {
        setVisited(c);
        for(Integer otherC : corner.vertexCorners(c)) {
          int nc = corner.n(otherC);
          if(!visited[nc] && silhouetteEdge(nc)) {
            setVisited(nc);
            int pc = corner.p(otherC);
            p.add(corner.v(pc));
            c = pc;
            break;
          }
        }
      }
    }
    
    boolean silhouetteEdge(int c) {
      // if this corner and it's opposite are on faces facing different directions relative to the camera 
      return forward[corner.t(c)] ^ forward[corner.t(corner.o(c))];
    }
  }
  
  public class Path {
    CornerTable.Path path = corner.new Path();

    public void add(int vert) {
      path.add(vert);
    }
    
    public void reverse() {
      path.reverse();
    }
    
    public List<pt3> points() {
      List<pt3> pts = new ArrayList<pt3>();
      for(int i : path.verts) {
        pts.add(G[i]);
      }
      return pts;
    }
    
    public int size() {
      return path.size();
    }
  }

  // edge opposite te given corner
  public float edgeLengthSq(int c) {
    return G[corner.V[n(c)]].sqdist(G[corner.V[p(c)]]);
  }
  
  public float edgeLength(int c) {
    return sqrt(edgeLengthSq(c));
  }
  
  public int numPoints() {
    return G.length;
  }
  
  public int numFaces() {
    return corner.numFaces();
  } 
  
  public int numCorners() {
    return numFaces()*3;
  }
  
  public void smooth(float t) {
    computeCurvatures();
    
    for(int v = 0; v < curvatures.length; v++) {
      if(!isBorderVertex(v)) {
        G[v].add(t, curvatures[v]);
      }
    }
  }
  
  public boolean isBorderVertex(int vertex) {
    return corner.isVertexOnBorder(cornerForVertex(vertex));
  }

  public BoundingBox3 boundingBox() {
    return new BoundingBox3(G);
  }
}
