/**
 * 
 */
package shapecore.matching;

import java.util.ArrayList;
import java.util.Collections;

public abstract class DTW {
  
  public float[][] warpData;
  public float totalCost;
  
  // cost of matching  
  abstract public float costFunction(int i, int j);
  
  abstract public float[][] exec();

  public float skipCost() {
    return 1;
  }
  
  public int[][] getWarp() {
    return extractWarp(exec());
  }
  
  // dynamic time warp
  // computes the matching between two sequences whose ranges may be streched differently
  public float[][] exec(int aLength, int bLength) {
    // allocate new data if 1) none has been allocated yet or 2) the dimensions don't match
    if(warpData == null || warpData.length != aLength || warpData[0].length != bLength) {
      warpData = new float[aLength][bLength];
    }
    // initialize the sides of the matrix so
    for(int i = 0; i < aLength; i++) {
      warpData[i][0] = 100000000000000f; // max float
    }
    for(int i = 0; i < bLength; i++) {
      warpData[0][i] = 100000000000000f; // max float
    }
    // initialize the "origin" of the matrix, this is because we force the beginnings to be aligned?
    warpData[0][0] = 0;
    
    // maybe this should be anisotropic?
    float skipCost = skipCost()/Math.max(aLength, bLength);
    
    // start looping at 1 rather than 0 because we've already initialized the zeros (the sides of the matrix)
    for(int i = 1; i < aLength; i++) {
      for(int j = 1; j < bLength; j++) {
        float cost = costFunction(i, j);
        warpData[i][j] = cost + DynamicTimeWarp.min(
          warpData[i-1][j] + skipCost,   // skip the previous element of B
          warpData[i][j-1] + skipCost,   // skip the previous element of A
          warpData[i-1][j-1]  // match the two previous elements
        );
      }
    }
    totalCost = warpData[aLength-1][bLength-1];
    /*
    int n = warpData.length;
    String[] strings = new String[1+warpData.length*warpData.length];
    int k = 0;
    strings[k] = str(n);
    for (int i=0; i < n; i++) {
      for (int j=0; j<n;j++) {
        k++;
        strings[k] = str(warpData[i][j]);
      }
    }
    saveStrings("F1.hts", strings);
    */
    return warpData;
  }

  // walk the time warp backwards, finding the minimum cost path
  // record the indicies of this path, this is a (piecewise) function that maximizes the matching
  // this extracts the warp going from A to B
  // the result will be the length of A, and its value will be indexes of B
  // the result is monotonic
  // i don't know if we actually need to walk it backwards
  // i think that there may be multiple minimal paths
  // we favor that most central path (least warping locally)
  int[][] extractWarp(float[][] warpData) {
    int i = warpData.length-1, j = warpData[0].length-1;
    ArrayList<int[]> result = new ArrayList<int[]>();
    while(i > 0 && j > 0) { // once we get one step from a side, we know the rest of that path
      result.add(new int[] {i,j}); // record this location
      // look at the neighborhood
      float
      skipB = warpData[i-1][j],
      skipA = warpData[i][j-1],
      match = warpData[i-1][j-1];
      // follow the minimum path
      if(skipB < skipA && skipB < match) {
        i--;
      } else if(skipA < skipB && skipA < match) {
        j--;
      } else { // favor matching over alternatives
        i--; j--;
      }
    }
    while(i > 0) {
      i--;
      result.add(new int[]{i,j});
    }
    while(j > 0) {
      j--;
      result.add(new int[]{i,j});
    }
    
    Collections.reverse(result);
    int[][] warp = new int[result.size()][];
    result.toArray(warp);      
    return warp;
  }
  
  int[] extractWarpOld(float[][] warpData) {
    int i = warpData.length-1, j = warpData[0].length-1;
    int[] warp = new int[warpData.length];
    int k = warp.length - 1;
    while(i > 0 && j > 0 && k > 0) { // once we get one step from a side, we know the rest of that path
      warp[k] = j; // record this location
      k--;
      // look at the neighborhood
      float
      skipB = warpData[i-1][j],
      skipA = warpData[i][j-1],
      match = warpData[i-1][j-1];
      // follow the minimum path
      if(skipB < skipA && skipB < match) {
        i--;
      } else if(skipA < skipB && skipA < match) {
        j--;
      } else { // favor matching over alternatives
        i--; j--;
      }
    }
    /*
    // TODO: write a testcase for this
    if(i == 1) {
      while(k > 1) warp[k] = k;
    } else if (j == 1) {
      while(k > 1) warp[k] = 1;
    }
    */
    warp[0] = 0; 
    return warp;
  }
}