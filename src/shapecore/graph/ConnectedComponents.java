package shapecore.graph;

import java.util.HashMap;
import java.util.Map;

public class ConnectedComponents<D> {
  private Map<Integer,Component<D>> assignments = new HashMap<Integer,Component<D>>();
  
  public void union(int i, int j) {
    Component<D>
      ci = find(i),
      cj = find(j);
    if(ci == null && cj == null) {
      ci = cj = new Component<D>();
    } else if(ci == null && cj != null) {
      ci = cj;
    } else if(ci != null && cj == null) {
      cj = ci;
    }
    if(ci != cj) { // merge
      if(ci.rank < cj.rank) { // rank keeps merge tree balanced
        ci.parent = cj;
        ci = cj;
      } else if(ci.rank > cj.rank) {
        cj.parent = ci;
        cj = ci;
      } else {
        ci.rank++;
        cj.parent = ci;
        cj = ci;
      }
    }
    // this isn't always necessary (sometimes it's already set)
    assignments.put(i, ci);
    assignments.put(j, cj);
  }
  
  public D getData(int i) {
    Component<D> c = find(i);
    if(c == null) return null;
    else return c.data;
  }
  
  public boolean setData(int i, D data) {
    Component<D> c = find(i);
    if(c == null) {
      return false;
    } else {
      c.data = data;
      return true;
    }
  }
  
  private Component<D> find(int i) {
    Component<D> c = assignments.get(i);
    if(c != null) {
      return c.root();
    } else {
      return null;
    }
  }
  
  private static class Component<D> {
    Component<D> parent = null;
    int rank = 0;
    D data;
    
    Component<D> root() {
      if(parent == null) return this;
      else return parent.root();
    }
  }
  
}