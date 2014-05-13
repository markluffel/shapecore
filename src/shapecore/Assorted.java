package shapecore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shapecore.tuple.Pair;

public class Assorted {

  public static float smallestGreaterThanZero(float a, float b) {
    if(a > 0 && a < b) return a;
    else if(b > 0 && b < a) return b;
    else return Math.max(a, b);
  }
  
  public static <T> T firstCommonElement(List<T> a, List<T> b) {
    Set<T> bHash = new HashSet<T>(b);
    for(T item : a) {
      if(bHash.contains(item)) {
        return item;
      }
    }
    return null;
  }
  
  // FIXME: write a docstring for this
  public static <T> Pair<Integer,Integer> firstCommonElementIndicies(List<T> a, List<T> b) {
    Set<T> bHash = new HashSet<T>(b);
    int aIndex = 0;
    int bIndex = -1; 
    for(T item : a) {
      if(bHash.contains(item)) {
        bIndex = b.indexOf(item);
        break;
      }
      aIndex++;
    }
    
    if(bIndex == -1) return null;
    
    return Pair.make(aIndex,bIndex);
  }

}
