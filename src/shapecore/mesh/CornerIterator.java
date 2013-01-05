package shapecore.mesh;


public class CornerIterator {
  CornerTable table;
  public int c;
  
  // traveral interface allows method chaining
  CornerIterator n() {c = table.n(c); return this; } // next
  CornerIterator p() {c = table.p(c); return this; } // previous
  CornerIterator o() {c = table.o(c); return this; } // opposite
  CornerIterator l() {c = table.l(c); return this; } // left
  CornerIterator r() {c = table.r(c); return this; } // right
  CornerIterator s() {c = table.s(c); return this; } // swing
  
  // interface for extracting info other than the connectivity
  int t() {return table.t(c); }       // triangle id
  int v() {return table.v(c); }       // vertex id
  boolean b() {return table.b(c);}    // border: returns true if corner has no opposite  
}
