package shapecore.mesh.compression;

import shapecore.Oplet;
import shapecore.pt3;
import shapecore.vec3;
import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

//CORNER TABLE FOR TRIANGLE MESHES by Jarek Rosignac
//Last edited Feb 17, 2008
// modified in spring 2010 by mark luffel

public class EBCornerTable {
  
  // ==================================== INIT, CREATE, COPY
  // ====================================
  EBCornerTable() {}
  int maxnv = 30000;                         // max number of vertices
  int maxnt = maxnv*2;                       // max number of triangles

  int[] V = new int[3*maxnt];               // V table (triangle/vertex indices)
  int[] O = new int[3*maxnt];               // O table (opposite corner indices)
  
  //int[] W = new int [3*maxnt];               // mid-edge vertex indices for subdivision (associated with corner opposite to edge)
  
  pt3[] G = new pt3[maxnv];                   // geometry table (vertices)
  int[] Mv = new int[maxnv];                  // vertex markers
  int[] Valence = new int[maxnv];             // vertex valence (count of incident triangles)
  boolean[] Border = new boolean [maxnv];     // vertex is border
  vec3[] Nv = new vec3[maxnv];                // vertex normals or laplace vectors

  vec3[] Nt = new vec3[maxnt];                // triangles normals
  boolean[] visible = new boolean[maxnt];     // set if triangle visible
  int[] Mt = new int[maxnt];                  // triangle markers for distance and other things

  int nt = 0;                                 // current number of triangles
  int nc = 0;                                 // current number of corners (3 per triangle)

  void declare() {
    for (int i=0; i<maxnv; i++) {
      G[i]=new pt3(0,0,0); Nv[i]=new vec3(0,0,0);
    }   // init vertices and normals
    for (int i=0; i<maxnt; i++) {
      Nt[i]=new vec3(0,0,0); visible[i]=true;
    }
  }       // init triangle normals and skeleton labels
  
  void init() {
    nv=0;
    nt=0;
    nc=0;
    for (int i=0; i<maxnt; i++) visible[i]=true;
  }
  
  void makeGrid (int w) { // make a 2D grid of vertices
    for (int i=0; i<w; i++) {
      for (int j=0; j<w; j++) { 
        //G[w*i+j].setTo(height*.8*j/(w-1)+height/10,height*.8*i/(w-1)+height/10,0);
      }
    }    
    for (int i=0; i<w-1; i++) {
      for (int j=0; j<w-1; j++) {                  // define the triangles for the grid
        V[(i*(w-1)+j)*6]=i*w+j;       V[(i*(w-1)+j)*6+2]=(i+1)*w+j;       V[(i*(w-1)+j)*6+1]=(i+1)*w+j+1;
        V[(i*(w-1)+j)*6+3]=i*w+j;     V[(i*(w-1)+j)*6+5]=(i+1)*w+j+1;     V[(i*(w-1)+j)*6+4]=i*w+j+1;
      }
    }
    nv = w*w;
    nt = 2*(w-1)*(w-1); 
    nc = 3*nt;
  }
  
  void update() {
    computeO();
    normals();
  }
  
  // ============================================= CORNER OPERATORS
  // =======================================

  // operations on a corner
  final int n (int c) {return 3*t(c)+(c+1)%3;}   // next corner in the same t(c)
  final int p (int c) {return n(n(c));}  // previous corner in the same t(c)
  final int o (int c) {if (b(c)) return c; else return O[c];} // opposite (or self if it has no opposite)
  final int l (int c) {return o(n(c));} // left neighbor (or next if n(c) has no opposite)
  final int r (int c) {return o(p(c));} // right neighbor (or previous if p(c) has no opposite)
  final int s (int c) {return n(l(c));} // swings around v(c) or around a border loop

  final int v (int c) {return V[c];}   // id of the vertex of c
  final int t (int c) {return (int)(c/3);}          // triangle of corner
  final pt3 g (int c) {return G[v(c)];}  // shortcut to get the point of the vertex v(c) of corner c
  final boolean b (int c) {return O[c]==-1;}       // if faces a border (has no opposite)
  vec3 Nv (int c) {return(Nv[V[c]]);}             // shortcut to get the normal of v(c)
  vec3 Nt (int c) {return(Nt[t(c)]);}            // shortcut to get the normal of t(c)
  boolean vis(int c) {return visible[t(c)]; };   // true if tiangle of c is visible
  
  // void writeCorner (int c) {println("c="+c+", n="+n(c)+", p="+p(c)+",
  // o="+o(c)+", v="+v(c)+", t="+t(c)+", EB symbol="+triangleSymbol[t(c)]+"."+",
  // nt="+nt+", nv="+nv ); };
  // void writeCorner () {writeCorner (c);}
  // void writeCorners () {for (int c=0; c<nc; c++) {println("T["+c+"]="+t(c)+",
  // visible="+visible[t(c)]+", v="+v(c)+", o="+o(c));};}

  pt3 cg(int c) {pt3 cPt = average(g(c),average(g(c),triCenter(t(c))));  return(cPt); };   // computes point at corner
  pt3 corner(int c) {return average(g(c),triCenter(t(c)));   };   // returns corner point
  //void showCorner(int c, int r) {pt3 cPt = A(g(c),A(g(c),corner(c)));  cPt.show(r); };   // renders corner c as small ball

  // ============================================= O TABLE CONSTRUCTION
  // =========================================
  void computeOnaive() {                         // sets the O table from the V table, assumes consistent orientation of triangles
    for (int i=0; i < 3*nt; i++) O[i] = -1;  // init O table to -1: has no opposite (i.e. is a border corner)
    for (int i=0; i < nc; i++) {
      for (int j=i+1; j < nc; j++) {       // for each corner i, for each other corner j
        if( v(n(i)) == v(p(j)) && v(p(i)) == v(n(j)) ) {
          // make i and j opposite if they match
          O[i]=j; O[j]=i;
        }
      }
    }
  }

  void computeO() { 
    int val[] = new int [nv]; for (int v=0; v<nv; v++) val[v]=0;  for (int c=0; c<nc; c++) val[v(c)]++;   // valences
    int fic[] = new int [nv]; int rfic=0; for (int v=0; v<nv; v++) {fic[v]=rfic; rfic+=val[v];};  // head of list of incident corners
    for (int v=0; v<nv; v++) val[v]=0;   // valences wil be reused to track how many incident corners were encountered for each vertex
    int [] C = new int [nc]; for (int c=0; c<nc; c++) C[fic[v(c)]+val[v(c)]++]=c;  // vor each vertex: the list of val[v] incident corners starts at C[fic[v]]
    for (int c=0; c<nc; c++) O[c]=-1;    // init O table to -1 meaning that a corner has no opposite (i.e. faces a border)
    for (int v=0; v<nv; v++)             // for each vertex...
      for (int a=fic[v]; a<fic[v]+val[v]-1; a++) for (int b=a+1; b<fic[v]+val[v]; b++)  { // for each pair (C[a],C[b[]) of its incident corners
        if (v(n(C[a]))==v(p(C[b]))) {O[p(C[a])]=n(C[b]); O[n(C[b])]=p(C[a]); }; // if C[a] follows C[b] around v, then p(C[a]) and n(C[b]) are opposite
        if (v(n(C[b]))==v(p(C[a]))) {O[p(C[b])]=n(C[a]); O[n(C[a])]=p(C[b]); };        };                }

  // ============================================= DISPLAY
  // =======================================
  pt3 Cbox = new pt3();                   // mini-max box center
  float Rbox = 0;                                        // Radius of enclosing
  // ball
  boolean showLabels=false; 
  void computeBox() {
    pt3 Lbox =  G[0].get();  pt3 Hbox = G[0].get();
    for (int i=1; i<nv; i++) { 
      Lbox.x=min(Lbox.x,G[i].x); Lbox.y=min(Lbox.y,G[i].y); Lbox.z=min(Lbox.z,G[i].z);
      Hbox.x=max(Hbox.x,G[i].x); Hbox.y=max(Hbox.y,G[i].y); Hbox.z=max(Hbox.z,G[i].z); 
    };
    Cbox.setTo(average(Lbox,Hbox));
    Rbox = Cbox.dist(Hbox);
  }
  
  /*
  void show(JPApplet p) {
    int col=60;
    p.noSmooth(); p.noStroke();
    if(showDistance) showDistance(); else if(showEB) showEB();  else if(showTriangles) showTriangles();  
    if (showEdges) {stroke(dblue); for(int i=0; i<nc; i++) if(visible[t(i)]) drawEdge(i); };  
    if (showSelectedTriangle) {noStroke(); fill(green); shade(t(c)); noFill(); }; 
    stroke(red); showBorder();
    if (showVertices) {noStroke(); noSmooth();fill(white); for (int v=0; v<nv; v++)  G[v].show(r); noFill();};
    if (showNormals) {stroke(blue); showTriNormals(); stroke(magenta); showVertexNormals(); };                // show triangle normals
    if (showLabels) { fill(black); 
    for (int i=0; i<nv; i++) {G[i].label("v"+str(i),labelD); }; 
    for (int i=0; i<nc; i++) {corner(i).label("c"+str(i),labelD); }; 
    for (int i=0; i<nt; i++) {triCenter(i).label("t"+str(i),labelD); }; noFill();};
    noStroke(); fill(dred); showCorner(prevc,r); fill(dgreen); mark.show(iint(1.5*r)); fill(dblue); showCorner(c,iint(2*r));  
  }
  */
  // ========================================================== EDGES
  // ===========================================
  //boolean showEdges=false;
  //void findShortestEdge() {c=cornerOfShortestEdge();  } 
  int cornerOfShortestEdge() {  // assumes manifold
    float md=dist(g(p(0)),g(n(0))); int ma=0;
    for (int a=1; a<nc; a++) if (vis(a)&&(dist(g(p(a)),g(n(a)))<md)) {ma=a; md=dist(g(p(a)),g(n(a)));}; 
    return ma;
  } 
  //void drawEdge(int c) {showLine(g(p(c)),g(n(c))); };  // draws edge of t(c) opposite to corner c
  //void showBorder() {for (int i=0; i<nc; i++) {if (visible[t(i)]&&b(i)) {drawEdge(i);}; }; };         // draws all border edges

  // ========================================================== TRIANGLES
  // ===========================================
  //boolean showTriangles=true;
  //boolean showSelectedTriangle=false;
  
  void addTriangle(int i, int j, int k) {V[nc++]=i; V[nc++]=j; V[nc++]=k; visible[nt++]=true;}
  
  pt3 triCenter(int i) {return average( G[V[3*i]], G[V[3*i+1]], G[V[3*i+2]]); } // computes center of triangle t(i)
  //pt3 triCenter() {return triCenter(t());}
  void writeTri (int i) {println("T"+i+": V = ("+V[3*i]+":"+v(o(3*i))+","+V[3*i+1]+":"+v(o(3*i+1))+","+V[3*i+2]+":"+v(o(3*i+2))+")"); };
  /*
  void hitTriangle() {
    prevc=c;       // save c for geodesic and other applications
    float smallestDepth=10000000;
    boolean hit=false;
    for (int t=0; t<nt; t++) {
      if (rayHitTri(eye,mark,g(3*t),g(3*t+1),g(3*t+2))) {hit=true;
      float depth = rayDistTriPlane(eye,mark,g(3*t),g(3*t+1),g(3*t+2));
      if ((depth>0)&&(depth<smallestDepth)) {smallestDepth=depth;  c=3*t;};
      }; 
    };
    if (hit) {        // sets c to be the closest corner in t(c) to the picked point
      pt3 X = eye.make(); X.addScaledVec(smallestDepth,eye.vecTo(mark));
      mark.setTo(X);
      float distance=X.disTo(g(c));
      int b=c;
      if (X.disTo(g(n(c)))<distance) {b=n(c); distance=X.disTo(g(b)); };
      if (X.disTo(g(p(c)))<distance) {b=p(c);};
      c=b;
      println("c="+c+", pc="+prevc+", t(pc)="+t(prevc));
    };
  }
  */
  void shade(Oplet p, int t) {
    if(visible[t]) {
      p.beginShape(TRIANGLES);
      p.vertex(g(3*t));
      p.vertex(g(3*t+1));
      p.vertex(g(3*t+2));
      p.endShape();
    }
  }
  //void showTriangles() {fill(cyan); for(int t=0; t<nt; t++)  shade(t); noFill();}; 
  // ========================================================== VERTICES
  // ===========================================
  //boolean showVertices=false;
  int nv = 0;                              // current number of vertices
  int r=5;                                // radius of spheres for displaying vertices
  int addVertex(pt3 P) { G[nv].setTo(P); nv++; return nv-1;};
  int addVertex(float x, float y, float z) { G[nv].x=x; G[nv].y=y; G[nv].z=z; nv++; return nv-1;};
  /*
  void move(int c) {g(c).addScaledVec(pmouseY-mouseY,Nv(c));}
  void move(int c, float d) {g(c).addScaledVec(d,Nv(c));}
  void move() {move(c); normals();}
  void moveROI() {
    pt3 Q = new pt3(0,0,0);
    for (int i=0; i<nv; i++) Mv[i]=0;  // resets the valences to 0
    computeDistance(5);
    for (int i=0; i<nv; i++) VisitedV[i]=false;  // resets the valences to 0
    computeTriNormals(); computeVertexNormals();
    for (int i=0; i<nc; i++) if(!VisitedV[v(i)]&&(Mv[v(i)]!=0)) move(i,1.*(pmouseY-mouseY+mouseX-pmouseX)*(rings-Mv[v(i)])/rings/10);  // moves ROI
    computeDistance(7);
    Q.setTo(g());
    smoothROI();
    g().setTo(Q);
  }
  */

  // ========================================================== NORMALS
  // ===========================================
  //boolean showNormals=false;
  void normals() {
    computeValenceAndResetNormals();
    computeTriNormals();
    computeVertexNormals();
  }
  void computeValenceAndResetNormals() {
    // caches valence of each vertex
    for (int i=0; i<nv; i++) {Nv[i].setTo(0,0,0); Valence[i]=0;};  // resets the valences to 0
    for (int i=0; i<nc; i++) {Valence[v(i)]++; };
  }
  vec3 triNormal(int t) { return V(g(3*t),g(3*t+1)).cross(V(g(3*t),g(3*t+2))); }  
  
  void computeTriNormals() {
    // caches normals of all triangles
    for (int i=0; i<nt; i++) Nt[i].setTo(triNormal(i));
  }             
  void computeVertexNormals() {
    // computes the vertex normals as sums of the normal vectors of incident tirangles scaled by area/2
    for (int i=0; i<nv; i++) Nv[i].setTo(0,0,0);  // resets the valences to 0
    for (int i=0; i<nc; i++) Nv[v(i)].add(Nt[t(i)]);
    for (int i=0; i<nv; i++) Nv[i].normalize();
  }
  
  //void showCornerNormal(int c) {S(20*r,Nt[t(c)]).show(M(g(c),g(c),triCenter(t(c))));};   // renders corner normal
  //void showVertexNormals() {for (int i=0; i<nv; i++) S(10*r,Nv[i]).show(G[i]);  };
  //void showTriNormals() {for (int i=0; i<nt; i++) S(10*r,U(Nt[i])).show(triCenter(i));  };

  // ============================================================= SMOOTHING
  // ============================================================
  void computeLaplaceVectors() {  // computes the vertex normals as sums of the normal vectors of incident tirangles scaled by area/2
    computeValenceAndResetNormals();
    for (int i=0; i<3*nt; i++) {Nv[v(p(i))].add(g(p(i)).to(g(n(i))));}
    for (int i=0; i<nv; i++) {
      Nv[i].div(Valence[i]);
    }
  }
  void tuck(float s) {for (int i=0; i<nv; i++) {G[i].add(s,Nv[i]);} }  // displaces each vertex by a fraction s of its normal
  void smoothen() {normals(); computeLaplaceVectors(); tuck(0.6f); computeLaplaceVectors(); tuck(-0.6f);};
  void tuckROI(float s) {for (int i=0; i<nv; i++) if (Mv[i]!=0) G[i].add(s,Nv[i]); }; //displaces each vertex by a fraction s of its normal
  void smoothROI() {computeLaplaceVectors(); tuckROI(0.5f); computeLaplaceVectors(); tuckROI(-0.5f);};

  // ============================================================= SUBDIVISION
  // ============================================================
  
  public void butterfly() {
    int[] W = new int[3*nv];
    splitEdges(W);
    bulge(W);
    splitTriangles(W);
    computeO();
    computeValenceAndResetNormals(); 
    computeTriNormals(); 
    computeVertexNormals();
  }
  
  void splitEdges(int[] W) {
    // creates a new vertex for each edge and stores its ID in the W of the corner (and of its opposite if any)
    for (int i=0; i<3*nt; i++) {  // for each corner i
      if(b(i)) {
        G[nv]=average(g(n(i)),g(p(i)));
        W[i]=nv++;
      } else {
        if(i<o(i)) {
          G[nv]=average(g(n(i)),g(p(i)));
          W[o(i)]=nv;
          W[i]=nv++;
        }
      }
    }
 // if this corner is the first to  see the edge
  }

  void bulge(int[] W) {
    // tweaks the new mid-edge vertices according to the Butterfly mask
    for(int i = 0; i < 3*nt; i++) {
      // i < o(i) means: only tweak one of the half edges, arbitrarily the lower ordered one
      if(!b(i) && i < o(i)) {    // no tweak for mid-vertices of border edges
        if (!b(p(i))&&!b(n(i))&&!b(p(o(i)))&&!b(n(o(i)))) { // check that
          G[W[i]].add(0.25f,average(average(g(l(i)),g(r(i))),average(g(l(o(i))),g(r(o(i))))).to(average(g(i),g(o(i)))));
        }
      }
    }
  }

  void splitTriangles(int[] W) {    // splits each triangle into 4
    for (int i = 0; i < 3*nt; i += 3) {
      V[3*nt+i]=v(i); V[n(3*nt+i)]=W[p(i)]; V[p(3*nt+i)]=W[n(i)];
      V[6*nt+i]=v(n(i)); V[n(6*nt+i)]=W[i]; V[p(6*nt+i)]=W[p(i)];
      V[9*nt+i]=v(p(i)); V[n(9*nt+i)]=W[n(i)]; V[p(9*nt+i)]=W[i];
      V[i]=W[i]; V[n(i)]=W[n(i)]; V[p(i)]=W[p(i)];
    }
    nt=4*nt; nc=3*nt;
  }

  void refine() {
    int[] W = new int[3*nt];
    update();
    splitEdges(W);
    bulge(W);
    splitTriangles(W);
    update();
  }

  // ========================================================== FILL HOLES
  // ===========================================
  void fanHoles() {for (int cc=0; cc<nc; cc++) if (visible[t(cc)]&&b(cc)) fanThisHole(cc); normals();  }
  
  void fanThisHole(int cc) {   // fill shole with triangle fan (around average of parallelogram predictors). Must then call computeO to restore O table
    if(!b(cc)) return ; // stop if cc is not facing a border
    G[nv].setTo(0,0,0);   // tip vertex of fan
    int o=0;              // tip corner of new fan triangle
    int n=0;              // triangle count in fan
    int a=n(cc);          // corner running along the border
    while (n(a)!=cc) {    // walk around the border loop
      if(b(p(a))) {       // when a is at the left-end of a border edge
        // TODO
        //G[nv].addPt( M(M(g(a),g(n(a))),S(g(a),V(g(p(a)),g(n(a))))) ); // add parallelogram prediction and mid-edge point
        o=3*nt; V[o]=nv; V[n(o)]=v(n(a)); V[p(o)]=v(a); visible[nt]=true; nt++; // add triangle to V table, make it visible
        O[o]=p(a); O[p(a)]=o;        // link opposites for tip corner
        O[n(o)]=-1; O[p(o)]=-1;
        n++;}; // increase triangle-count in fan
        a=s(a);} // next corner along border
    G[nv].scaleBy(1./n); // divide fan tip to make it the average of all predictions
    a=o(cc);       // reset a to walk around the fan again and set up O
    int l=n(a);   // keep track of previous
    int i=0; 
    while(i<n) {
      a=s(a);
      if(v(a)==nv) {
        i++;
        O[p(a)]=l;
        O[l]=p(a);
        l=n(a);
      }
    }  // set O around the fan
    nv++;  nc=3*nt;  // update vertex count and corner count
  }

  // =========================================== GEODESIC MEASURES, DISTANCES
  // =============================
  boolean  showPath=false, showDistance=false;  
  boolean[] P = new boolean [3*maxnt];       // marker of corners in a path to parent triangle
  int[] Distance = new int[maxnt];           // triangle markers for distance fields
  int[] SMt = new int[maxnt];                // sum of triangle markers for isolation
  //int prevc = 0;                             // previously selected corner
  int rings=2;                           // number of rings for colorcoding

  void computeDistance(int c, int maxr) {
    int tc=0;
    int r=1;
    for(int i=0; i<nt; i++) {Mt[i]=0;};  Mt[t(c)]=1; tc++;
    for(int i=0; i<nv; i++) {Mv[i]=0;};
    while ((tc<nt)&&(r<=maxr)) {
      for(int i=0; i<nc; i++) {if ((Mv[v(i)]==0)&&(Mt[t(i)]==r)) {Mv[v(i)]=r;};};
      for(int i=0; i<nc; i++) {if ((Mt[t(i)]==0)&&(Mv[v(i)]==r)) {Mt[t(i)]=r+1; tc++;};};
      r++;
    };
    rings=r;
  }

  void computeIsolation() {
    int c;
    println("Starting isolation computation for "+nt+" triangles");
    for(int i=0; i<nt; i++) {SMt[i]=0;}; 
    for(c=0; c<nc; c+=3) {println("  triangle "+t(c)+"/"+nt); computeDistance(c, 1000); for(int j=0; j<nt; j++) {SMt[j]+=Mt[j];}; };
    int L=SMt[0], H=SMt[0];  for(int i=0; i<nt; i++) { H=max(H,SMt[i]); L=min(L,SMt[i]);}; if (H==L) {H++;};
    c=0; for(int i=0; i<nt; i++) {Mt[i]=(SMt[i]-L)*255/(H-L); if(Mt[i]>Mt[t(c)]) {c=3*i;};}; rings=255;
    for(int i=0; i<nv; i++) {Mv[i]=0;};  for(int i=0; i<nc; i++) {Mv[v(i)]=max(Mv[v(i)],Mt[t(i)]);};
    println("finished isolation");
  }

  void computePath(int c, int prevc) {                 // graph based shortest path between t(c0 and t(prevc), prevc is the previously picekd corner
    for(int i=0; i<nt; i++) {Mt[i]=0;}; Mt[t(prevc)]=1; // Mt[0]=1;
    for(int i=0; i<nc; i++) {P[i]=false;};
    boolean searching=true;
    while (searching) {
      for(int i=0; i<nc; i++) {
        if (searching&&(Mt[t(i)]==0)&&(o(i)!=-1)) {
          if(Mt[t(o(i))]==r) {
            Mt[t(i)]=r+1; 
            P[i]=true; 
            if(t(i)==t(c)){searching=false;};
          }
        }
      }
      r++;
    };
    for(int i=0; i<nt; i++) {Mt[i]=0;};  // graph distance between triangle and t(c)
    rings=1;      // track ring number
    int b=c;
    int k=0;
    while (t(b)!=t(prevc)) {rings++;  
    if (P[b]) {b=o(b); print(".o");} else {if (P[p(b)]) {b=r(b);print(".r");} else {b=l(b);print(".l");};}; Mt[t(b)]=rings; };
  }
  //void  showDistance(JPApplet p ) { for(int t=0; t<nt; t++) {if(Mt[t]==0) p.fill(cyan); else fill(ramp(Mt[t],rings)); p.shade(t);}; } 

  // ========================================================== DELETE
  // ===========================================
  void hideROI() { for(int i=0; i<nt; i++) if(Mt[i]>0) visible[i]=false; }

  // ========================================================== GARBAGE COLLECTION
  // ===========================================
  //void clean() {excludeInvisibleTriangles();  compactVO(); compactV(); M.normals();}  // removes deleted triangles and unused vertices
  void excludeInvisibleTriangles () {for (int b=0; b<nc; b++) {if (!visible[t(o(b))]) {O[b]=-1;};};}
  void compactVO() {  
    int[] U = new int [nc];
    int lc=-1; for (int c=0; c<nc; c++) {if (visible[t(c)]) {U[c]=++lc; }; };
    for (int c=0; c<nc; c++) {if (!b(c)) {O[c]=U[o(c)];} else {O[c]=-1;}; };
    int lt=0;
    for (int t=0; t<nt; t++) {
      if (visible[t]) {
        V[3*lt]=V[3*t]; V[3*lt+1]=V[3*t+1]; V[3*lt+2]=V[3*t+2]; 
        O[3*lt]=O[3*t]; O[3*lt+1]=O[3*t+1]; O[3*lt+2]=O[3*t+2]; 
        visible[lt]=true; 
        lt++;
      };
    };
    nt=lt; nc=3*nt;    
    println("      ...  NOW: nv="+nv +", nt="+nt +", nc="+nc );
  }

  void compactV() {  
    println("COMPACT VERTICES: nv="+nv +", nt="+nt +", nc="+nc );
    int[] U = new int [nv];
    boolean[] deleted = new boolean [nv];
    for (int v=0; v<nv; v++) {deleted[v]=true;};
    for (int c=0; c<nc; c++) {deleted[v(c)]=false;};
    int lv=-1; for (int v=0; v<nv; v++) {if (!deleted[v]) {U[v]=++lv; }; };
    for (int c=0; c<nc; c++) {V[c]=U[v(c)]; };
    lv=0;
    for (int v=0; v<nv; v++) {
      if (!deleted[v]) {G[lv].setTo(G[v]);  deleted[lv]=false; 
      lv++;
      };
    };
    nv=lv;
    println("      ...  NOW: nv="+nv +", nt="+nt +", nc="+nc );
  }

  // ============================================================= ARCHIVAL
  // ============================================================
  boolean flipOrientation=false;            // if set, save will flip all triangles

  void saveMesh(Oplet p) {
    String [] inppts = new String [nv+1+nt+1];
    int s=0;
    inppts[s++]=str(nv);
    for (int i=0; i<nv; i++) {inppts[s++]=str(G[i].x)+","+str(G[i].y)+","+str(G[i].z);};
    inppts[s++]=str(nt);
    if (flipOrientation) {for (int i=0; i<nt; i++) {inppts[s++]=str(V[3*i])+","+str(V[3*i+2])+","+str(V[3*i+1]);};}
    else {for (int i=0; i<nt; i++) {inppts[s++]=str(V[3*i])+","+str(V[3*i+1])+","+str(V[3*i+2]);};};
    p.saveStrings("mesh.vts",inppts);  println("saved on file");
  };

  /*
  void loadMesh() {
    println("loading fn["+fni+"]: "+fn[fni]); 
    String [] ss = loadStrings(fn[fni]);
    String subpts;
    int s=0;   int comma1, comma2;   float x, y, z;   int a, b, c;
    nv = iint(ss[s++]);
    print("nv="+nv);
    for(int k=0; k<nv; k++) {int i=k+s; 
    comma1=ss[i].indexOf(',');   
    x=fl(ss[i].substring(0, comma1));
    String rest = ss[i].substring(comma1+1, ss[i].length());
    comma2=rest.indexOf(',');    y=fl(rest.substring(0, comma2)); z=fl(rest.substring(comma2+1, rest.length()));
    G[k].setTo(x,y,z);
    };
    s=nv+1;
    nt = iint(ss[s]); nc=3*nt;
    println(", nt="+nt);
    s++;
    for(int k=0; k<nt; k++) {int i=k+s;
    comma1=ss[i].indexOf(',');   a=iint(ss[i].substring(0, comma1));  
    String rest = ss[i].substring(comma1+1, ss[i].length()); comma2=rest.indexOf(',');  
    b=iint(rest.substring(0, comma2)); c=iint(rest.substring(comma2+1, rest.length()));
    V[3*k]=a;  V[3*k+1]=b;  V[3*k+2]=c;
    }
  }
  */ 

  // ========================================================== FLIP
  // ===========================================
  void flipWhenLonger() {for (int c=0; c<nc; c++) if (dist(g(n(c)),g(p(c)))>dist(g(c),g(o(c)))) flip(c); } 
  //void flip() {flip(c);}
  void flip(int c) {      // flip edge opposite to corner c, FIX border cases
    if (b(c)) return;
    V[n(o(c))]=v(c); V[n(c)]=v(o(c));
    int co=o(c); O[co]=r(c); if(!b(p(c))) O[r(c)]=co; if(!b(p(co))) O[c]=r(co); if(!b(p(co))) O[r(co)]=c; O[p(c)]=p(co); O[p(co)]=p(c);  }

  // ========================================================== SIMPLIFICATION
  // ===========================================
  //void collapse() {collapse(c);}
  void collapse(int c) {if (b(c)) return;      // collapse edge opposite to corner c, does not check anything !!! assumes manifold
  int b=n(c), oc=o(c), vpc=v(p(c));
  visible[t(c)]=false; visible[t(oc)]=false;
  for (int a=b; a!=p(oc); a=n(l(a))) V[a]=vpc;
  O[l(c)]=r(c); O[r(c)]=l(c); O[l(oc)]=r(oc); O[r(oc)]=l(oc);  }

  
  // helpers, from laziness, should just convert processing-style casts into java ones
  static float fl(int f) {
    return (float)f;
  }

  static float fl(String f) {
    return Float.parseFloat(f);
  }
  
  static int iint(String i) {
    return Integer.parseInt(i);
  }
  
  static int iint(float i) {
    return (int)i;
  }

  static float log2(float x) {float r=0; if (x>0.00001) { r=log(x) / log(2);} ; return(r);}
  vec3 labelD=new vec3(-4,+4, 12);           // offset vector for drawing labels
  int maxr=1;
}

