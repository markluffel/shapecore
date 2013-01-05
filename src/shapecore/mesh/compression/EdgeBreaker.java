/**
 * 
 */
package shapecore.mesh.compression;

import static shapecore.Oplet.*;
import shapecore.Oplet;
import shapecore.pt3;

/**
 * 
 * Triangle mesh compression strategy,
 * encodes mesh as a series of traversal operations,
 * uses 
 *
 */
class EdgeBreaker {
  
  EBCornerTable table;
  int c;
  protected int nv,nt;
  protected boolean done;
  
  protected boolean[] VisitedV;   // vertex visited
  protected boolean[] VisitedT;  // triangle visited

  char[] CLERS;
  char[] triangleSymbol;
  int symbols=0;
  int[] stack;
  int stackHeight = 0;
  int Ccount=0, Lcount=0, Ecount=0, Rcount=0, Scount=0;
  int firstCorner=0; 
  int step=1;                              // to do something step by step
  
  EdgeBreaker(EBCornerTable table) {
    this.table = table;
    nv = table.nv;
    nt = table.nt;
    VisitedV = new boolean[nv];
    VisitedT = new boolean[nt];
    CLERS = new char[nt];
    triangleSymbol = new char[nt];
    
    stack = new int[10000]; // ought to estimate this based on the nv/nt
  }
  
  // delegate traversal to table
  final int t(int c) {return table.t(c);}
  final int n(int c) {return table.n(c);}
  final int p(int c) {return table.p(c);}
  final int v(int c) {return table.v(c);}
  final pt3 g(int c) {return table.g(c);}
  final boolean b(int c) {return table.b(c);}
  final int o(int c) {return table.o(c);}
  final int l(int c) {return table.l(c);}
  final int r(int c) {return table.r(c);}
  final int s(int c) {return table.s(c);}
  

  void init() {
    VisitedT[t(c)] = true;
    triangleSymbol[t(c)]='B';
    VisitedV[v(c)]=true; VisitedV[v(n(c))]=true; VisitedV[v(p(c))]=true; c=r(c);
    symbols=0; done=false;
  }

  void move() {
    if (!done) {
      VisitedT[t(c)]=true; 
      if (!VisitedV[v(c)]) {triangleSymbol[t(c)]='C'; Ccount++; CLERS[symbols++]=triangleSymbol[t(c)]; VisitedV[v(c)]=true; c=r(c); }
      else {
        if (VisitedT[t(r(c))]) {
          if (VisitedT[t(l(c))]) {triangleSymbol[t(c)]='E'; Ecount++; CLERS[symbols++]=triangleSymbol[t(c)]; c=stack[--stackHeight]; if (stackHeight==0) {done=true;};}
          else {triangleSymbol[t(c)]='R'; Rcount++; CLERS[symbols++]=triangleSymbol[t(c)]; c=l(c);};
        }
        else {
          if (VisitedT[t(l(c))]) {triangleSymbol[t(c)]='L'; Lcount++; CLERS[symbols++]=triangleSymbol[t(c)]; c=r(c);}
          else {triangleSymbol[t(c)]='S'; triangleSymbol[t(l(c))]='w'; Scount++; CLERS[symbols++]=triangleSymbol[t(c)]; stack[stackHeight++]=l(c); c=r(c);};
        }
      }
      Oplet.print(symbols+":"+CLERS[symbols-1]+" ");
    } else {
      Oplet.print(".");
    }
  }

  void jump() {
    int t = t(c);
    switch(triangleSymbol[t]) {
    case 'B':
    case 'C':
    case 'L': c=r(c); break;
    case 'E': c=pop(); break;
    case 'R': c=l(c); break;
    case 'S': push(l(c)); c=r(c); break;
    }
  }

  void show(Oplet p, int c, pt3 nV, pt3 pV) {
    p.fill(yellow);
    int t=t(c);
    pt3 tV = g(c).get();
    switch(triangleSymbol[t]) {
    case 'B': p.fill(200,200,200); tV.translateTowardsBy(0.07f,nV); break;
    case 'C': p.fill(90,250,200); tV.translateTowardsBy(0.07f,nV); break;
    case 'L': p.fill(20,250,250); break;
    case 'E': p.fill(155,250,150); tV.translateTowardsBy(0.05,pV); tV.translateTowardsBy(0.05,nV); break;
    case 'R': p.fill(127,250,200); break;
    case 'S': p.fill(255,250,150); tV.translateTowardsBy(0.05,pV); tV.translateTowardsBy(0.05,nV); break;
    }
    
    p.beginShape(TRIANGLES);  p.vertex(nV); p.vertex(pV);  p.vertex(tV); p.endShape();
    
    switch(triangleSymbol[t]) {
    case 'B':
    case 'C':
    case 'L': show(p, r(c),nV,tV); break;
    case 'E': break;
    case 'R': show(p, l(c),tV,pV); break;
    case 'S': show(p, r(c),nV,tV); show(p, l(c),tV,pV); break;
    }
  }

  void print(int c) {
    int t=t(c);
    print(triangleSymbol[t]); CLERS[symbols++]=triangleSymbol[t(c)];
    switch(triangleSymbol[t]) {
    case 'B':
    case 'C':
    case 'L': print(r(c)); break;
    case 'E': break;
    case 'R': print(l(c)); break;
    case 'S': print(r(c)); print(l(c)); break;
    }
  }
  
  void printS(int pc) {
    int c=pc; 
    symbols=0;
    resetStack();
    boolean EBisDone=false;
    while (!EBisDone) {
      int t=t(c);
      print(triangleSymbol[t]);
      CLERS[symbols++] = triangleSymbol[t(c)];
      switch(triangleSymbol[t]) {
      case 'B':
      case 'C':
      case 'L': c=r(c); break;
      case 'E': c=pop(); if (stackHeight==0) {EBisDone=true;} break;
      case 'R': c=l(c); break;
      case 'S': push(r(c)); c=l(c); break;
      }
    }
  }

  int leadingCS() {
    int r = 1;
    while(CLERS[r]=='C') r++;
    return r;
  }

  void stats(int lCs) {
    int cc=0, cl=0, ce=0, cr=0, cs=0, ct=0; 
    int lc=0, ll=0, le=0, lr=0, ls=0, lt=0; 
    int ec=0, el=0, ee=0, er=0, es=0, et=0; 
    int rc=0, rl=0, re=0, rr=0, rs=0, rt=0; 
    int sc=0, sl=0, se=0, sr=0, ss=0, st=0; 
    char last='C';
    println("    The "+lCs+" leading Cs are not counted and replaced by an overhead of "+(int)(EBCornerTable.log2(lCs))+" bits");
    for (int i=lCs; i<symbols; i++) {
      char s=CLERS[i];    print(s);
      if (last=='C') {if(s=='C') {cc++;}; if(s=='L') {cl++;}; if(s=='E') {ce++;}; if(s=='R') {cr++;}; if(s=='S') {cs++;}; };
      if (last=='L') {if(s=='C') {lc++;}; if(s=='L') {ll++;}; if(s=='E') {le++;}; if(s=='R') {lr++;}; if(s=='S') {ls++;}; };
      if (last=='E') {if(s=='C') {ec++;}; if(s=='L') {el++;}; if(s=='E') {ee++;}; if(s=='R') {er++;}; if(s=='S') {es++;}; };
      if (last=='R') {if(s=='C') {rc++;}; if(s=='L') {rl++;}; if(s=='E') {re++;}; if(s=='R') {rr++;}; if(s=='S') {rs++;}; };
      if (last=='S') {if(s=='C') {sc++;}; if(s=='L') {sl++;}; if(s=='E') {se++;}; if(s=='R') {sr++;}; if(s=='S') {ss++;}; };
      last=s;
    };
    println();
    Oplet.print("symbols reduced from "+symbols);  int rsymbols=symbols-lCs;  println(" to "+rsymbols);
    ct=cc+lc+ec+rc+sc;
    lt=cl+ll+el+rl+sl;
    et=ce+le+ee+re+se;
    rt=cr+lr+er+rr+sr;
    st=cs+ls+es+rs+ss;

    float Cf=((float)ct)/rsymbols, Lf=((float)lt)/rsymbols, Ef=((float)et)/rsymbols, Rf=((float)rt)/rsymbols, Sf=((float)st)/rsymbols;
    float entropy = -( EBCornerTable.log2(Cf)*Cf + EBCornerTable.log2(Lf)*Lf + EBCornerTable.log2(Ef)*Ef + EBCornerTable.log2(Rf)*Rf + EBCornerTable.log2(Sf)*Sf ); 
    println("100*Frequencies: C="+nf(Cf*100,2,2)+", L="+nf(Lf*100,2,2)+", E="+nf(Ef*100,2,2)+", R="+nf(Rf*100,2,2)+", S="+nf(Sf*100,2,2));
    println("***   Entropy (over remaining symbols ) = "+nf(entropy,1,2));
    println();
    println("COUNTS for "+rsymbols+" CLERS symbols:");
    println("        COUNTS cc="+nf(cc,4)+",  lc="+nf(lc,4)+",  ec="+nf(ec,4)+",  rc="+nf(rc,4)+",  sc="+nf(sc,4)+" .c="+nf(ct,4)); 
    println("        COUNTS cl="+nf(cl,4)+",  ll="+nf(ll,4)+",  el="+nf(el,4)+",  rl="+nf(rl,4)+",  sl="+nf(sl,4)+" .l="+nf(lt,4)); 
    println("        COUNTS ce="+nf(ce,4)+",  le="+nf(le,4)+",  ee="+nf(ee,4)+",  re="+nf(re,4)+",  se="+nf(se,4)+" .e="+nf(et,4)); 
    println("        COUNTS cr="+nf(cr,4)+",  lr="+nf(lr,4)+",  er="+nf(er,4)+",  rr="+nf(rr,4)+",  sr="+nf(sr,4) +" .r="+nf(rt,4)); 
    println("        COUNTS cs="+nf(cs,4)+",  ls="+nf(ls,4)+",  es="+nf(es,4)+",  rs="+nf(rs,4)+",  ss="+nf(ss,4) +" .s="+nf(st,4)); 
    float cost = entropy*rsymbols;   float costWlcs = cost+(int)(EBCornerTable.log2(lCs));
    float e = cost/(symbols);   float eWlcs = costWlcs/(symbols); 
    println("***  Amortized over all symbols :");
    println("*** No-context:                 Entropy = "+nf(e,1,2)+" bpt. Total cost = "+nf(cost,6,2)+" bits");
    println("*** counting RLE of leading Cs: Entropy = "+nf(eWlcs,1,2)+" bpt. Total cost = "+nf(costWlcs,6,2)+" bits");

    println("Pairs frequencies:");
    println("        COUNTS cc="+nf(((float)cc)/rsymbols,1,4)+",  lc="+nf(((float)lc)/rsymbols,1,4)+",  ec="+nf(((float)ec)/rsymbols,1,4)+",  rc="+nf(((float)rc)/rsymbols,1,4)+",  sc="+nf(EBCornerTable.fl(sc)/rsymbols,1,4)+" .c="+nf(EBCornerTable.fl(ct)/rsymbols,1,4)); 
    println("        COUNTS cl="+nf(((float)cl)/rsymbols,1,4)+",  ll="+nf(((float)ll)/rsymbols,1,4)+",  el="+nf(((float)el)/rsymbols,1,4)+",  rl="+nf(((float)rl)/rsymbols,1,4)+",  sl="+nf(EBCornerTable.fl(sl)/rsymbols,1,4)+" .l="+nf(EBCornerTable.fl(lt)/rsymbols,1,4)); 
    println("        COUNTS ce="+nf(((float)ce)/rsymbols,1,4)+",  le="+nf(((float)le)/rsymbols,1,4)+",  ee="+nf(((float)ee)/rsymbols,1,4)+",  re="+nf(((float)re)/rsymbols,1,4)+",  se="+nf(EBCornerTable.fl(se)/rsymbols,1,4)+" .e="+nf(EBCornerTable.fl(et)/rsymbols,1,4)); 
    println("        COUNTS cr="+nf(((float)cr)/rsymbols,1,4)+",  lr="+nf(((float)lr)/rsymbols,1,4)+",  er="+nf(((float)er)/rsymbols,1,4)+",  rr="+nf(((float)rr)/rsymbols,1,4)+",  sr="+nf(EBCornerTable.fl(sr)/rsymbols,1,4) +" .r="+nf(EBCornerTable.fl(rt)/rsymbols,1,4)); 
    println("        COUNTS cs="+nf(((float)cs)/rsymbols,1,4)+",  ls="+nf(((float)ls)/rsymbols,1,4)+",  es="+nf(((float)es)/rsymbols,1,4)+",  rs="+nf(((float)rs)/rsymbols,1,4)+",  ss="+nf(EBCornerTable.fl(ss)/rsymbols,1,4) +" .s="+nf(EBCornerTable.fl(st)/rsymbols,1,4)); 


    ct=cc+cl+ce+cr+cs;
    lt=lc+ll+le+lr+ls;
    et=ec+el+ee+er+es;
    rt=rc+rl+re+rr+rs;
    st=sc+sl+se+sr+ss;

    println();
    float ccf=0, clf=0, cef=0, crf=0, csf=0;
    float lcf=0, llf=0, lef=0, lrf=0, lsf=0;
    float ecf=0, elf=0, eef=0, erf=0, esf=0;
    float rcf=0, rlf=0, ref=0, rrf=0, rsf=0;
    float scf=0, slf=0, sef=0, srf=0, ssf=0;

    if (ct!=0) {  ccf=EBCornerTable.fl(cc)/ct; clf=EBCornerTable.fl(cl)/ct; cef=EBCornerTable.fl(ce)/ct; crf=EBCornerTable.fl(cr)/ct; csf=EBCornerTable.fl(cs)/ct; };
    if (lt!=0) {  lcf=EBCornerTable.fl(lc)/lt; llf=EBCornerTable.fl(ll)/lt; lef=EBCornerTable.fl(le)/lt; lrf=EBCornerTable.fl(lr)/lt; lsf=EBCornerTable.fl(ls)/lt; };
    if (et!=0) {  ecf=EBCornerTable.fl(ec)/et; elf=EBCornerTable.fl(el)/et; eef=EBCornerTable.fl(ee)/et; erf=EBCornerTable.fl(er)/et; esf=EBCornerTable.fl(es)/et; };
    if (rt!=0) {  rcf=EBCornerTable.fl(rc)/rt; rlf=EBCornerTable.fl(rl)/rt; ref=EBCornerTable.fl(re)/rt; rrf=EBCornerTable.fl(rr)/rt; rsf=EBCornerTable.fl(rs)/rt; };
    if (st!=0) {  scf=EBCornerTable.fl(sc)/st; slf=EBCornerTable.fl(sl)/st; sef=EBCornerTable.fl(se)/st; srf=EBCornerTable.fl(sr)/st; ssf=EBCornerTable.fl(ss)/st; };

    println("  Context frequencies");
    println("        % cc="+nf(ccf,0,2)+",  lc="+nf(lcf,0,2)+",  ec="+nf(ecf,0,2)+",  rc="+nf(rcf,0,2)+",  sc="+nf(scf,0,2)); 
    println("        % cl="+nf(clf,0,2)+",  ll="+nf(llf,0,2)+",  el="+nf(elf,0,2)+",  rl="+nf(rlf,0,2)+",  sl="+nf(slf,0,2)); 
    println("        % ce="+nf(cef,0,2)+",  le="+nf(lef,0,2)+",  ee="+nf(eef,0,2)+",  re="+nf(ref,0,2)+",  se="+nf(sef,0,2)); 
    println("        % cr="+nf(crf,0,2)+",  lr="+nf(lrf,0,2)+",  er="+nf(erf,0,2)+",  rr="+nf(rrf,0,2)+",  sr="+nf(srf,0,2)); 
    println("        % cs="+nf(csf,0,2)+",  ls="+nf(lsf,0,2)+",  es="+nf(esf,0,2)+",  rs="+nf(rsf,0,2)+",  ss="+nf(ssf,0,2)); 
    println();

    float cE = -( EBCornerTable.log2(ccf)*ccf + EBCornerTable.log2(clf)*clf + EBCornerTable.log2(cef)*cef + EBCornerTable.log2(crf)*crf + EBCornerTable.log2(csf)*csf ) ; 
    float lE = -( EBCornerTable.log2(lcf)*lcf + EBCornerTable.log2(llf)*llf + EBCornerTable.log2(lef)*lef + EBCornerTable.log2(lrf)*lrf + EBCornerTable.log2(lsf)*lsf ) ; 
    float eE = -( EBCornerTable.log2(ecf)*ecf + EBCornerTable.log2(elf)*elf + EBCornerTable.log2(eef)*eef + EBCornerTable.log2(erf)*erf + EBCornerTable.log2(esf)*esf ) ; 
    float rE = -( EBCornerTable.log2(rcf)*rcf + EBCornerTable.log2(rlf)*rlf + EBCornerTable.log2(ref)*ref + EBCornerTable.log2(rrf)*rrf + EBCornerTable.log2(rsf)*rsf ) ; 
    float sE = -( EBCornerTable.log2(scf)*scf + EBCornerTable.log2(slf)*slf + EBCornerTable.log2(sef)*sef + EBCornerTable.log2(srf)*srf + EBCornerTable.log2(ssf)*ssf ) ; 

    println("    Stream entropies: after C="+nf(cE,1,2)+", after L="+nf(lE,1,2)+", after E="+nf(eE,1,2)+", after R="+nf(rE,1,2)+", after S="+nf(sE,1,2));
    println("    Frequencies:            C="+nf(Cf,1,2)+",       L="+nf(Lf,1,2)+",       E="+nf(Ef,1,2)+",       R="+nf(Rf,1,2)+",       S="+nf(Sf,1,2));
    float Centropy=cE*Cf+lE*Lf+eE*Ef+rE*Rf+sE*Sf;
    cost = Centropy*rsymbols;    costWlcs = cost+EBCornerTable.iint(EBCornerTable.log2(lCs));
    e = cost/symbols;    eWlcs = costWlcs/symbols; 
    println("***   Entropy (over remaining symbols ) = "+nf(Centropy,1,2));
    println("***  Amortized over all symbols :");
    println("*** Average context Entropy = "+nf(Centropy,1,2)+" bpt. Total cost = "+nf(Centropy*symbols,6,2)+" bits");
    println("*** Ccontext:                   Entropy = "+nf(e,1,2)+" bpt. Total cost = "+nf(cost,6,2)+" bits");
    println("*** counting RLE of leading Cs: Entropy = "+nf(eWlcs,1,2)+" bpt. Total cost = "+nf(costWlcs,6,2)+" bits");
    println("+++++++++++++++++++++++++++++++++++++++++++++");
  }

  void compress(int c) {
    Ccount=0; Lcount=0; Ecount=0; Rcount=0; Scount=0; 
    resetStack(); 
    for (int v=0; v<nv; v++) {VisitedV[v]=false;};
    for (int t=0; t<nt; t++) {VisitedT[t]=false;};
    VisitedT[t(c)]=true; triangleSymbol[t(c)]='B'; VisitedV[v(c)]=true; VisitedV[v(n(c))]=true; VisitedV[v(p(c))]=true; c=r(c);
    symbols=0; 
    boolean EBisDone=false;
    while (!EBisDone) {
      VisitedT[t(c)]=true; 
      if (!VisitedV[v(c)]) {triangleSymbol[t(c)]='C'; Ccount++; CLERS[symbols++]=triangleSymbol[t(c)]; VisitedV[v(c)]=true; c=r(c); }
      else {
        if (VisitedT[t(r(c))]) {
          if (VisitedT[t(l(c))]) {triangleSymbol[t(c)]='E'; Ecount++; CLERS[symbols++]=triangleSymbol[t(c)]; c=stack[--stackHeight]; if (stackHeight==0) {EBisDone=true;};}
          else {triangleSymbol[t(c)]='R'; Rcount++; CLERS[symbols++]=triangleSymbol[t(c)]; c=l(c);};
        }
        else {
          if (VisitedT[t(l(c))]) {triangleSymbol[t(c)]='L'; Lcount++; CLERS[symbols++]=triangleSymbol[t(c)]; c=r(c);}
          else {triangleSymbol[t(c)]='S'; triangleSymbol[t(l(c))]='w'; Scount++; CLERS[symbols++]=triangleSymbol[t(c)]; stack[stackHeight++]=l(c); c=r(c);};
        };
      };
    }; 
    int total=Ccount+Lcount+Ecount+Rcount+Scount;
    println(nt+" triangles, "+total+" symbols: C="+Ccount+", L="+Lcount+", E="+Ecount+", R="+Rcount+", S="+Scount);
  }

  void show(Oplet p) {
    for(int t=0; t < nt; t++) {
      switch(triangleSymbol[t]) {
      case 'w': p.fill(white); break;
      case 'B': p.fill(black); break;
      case 'C': p.fill(yellow); break;
      case 'L': p.fill(blue); break;
      case 'E': p.fill(magenta); break;
      case 'R': p.fill(orange); break;
      case 'S': p.fill(red); break;
      default: p.fill(cyan);
      }
      table.shade(p, t); 
    }
  }
  
  // tiny stack implementation

  private int pop() {
    if (stackHeight == 0) {
      throw new IllegalStateException("Stack is empty");
    }
    return stack[--stackHeight];
  }
  private void push(int cor) {
    if(stackHeight >= stack.length) { // resize stack as needed
      int[] newStack = new int[stack.length*2];
      System.arraycopy(stack, 0, newStack, 0, stack.length);
    }
    stack[stackHeight] = cor;
    stackHeight++;    
  }
  private void resetStack() {
    stackHeight = 0;
  }
}