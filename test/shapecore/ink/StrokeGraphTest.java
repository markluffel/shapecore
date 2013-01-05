package shapecore.ink;

import java.util.Arrays;

import shapecore.ink.StrokeGraph;


import junit.framework.TestCase;

public class StrokeGraphTest extends TestCase {

  static String file1 = "/Users/markluffel/code/verkspace/research/src/data/example.sgrf";
  static String file2 = "/Users/markluffel/code/verkspace/research/src/data/example2.sgrf";
  static String file3 = "/Users/markluffel/code/verkspace/research/src/data/example3.sgrf";
  
  public void testMatchIdentical() throws Exception {
    StrokeGraph A = StrokeGraph.fromFile(file1);
    StrokeGraph B = StrokeGraph.fromFile(file1);
    
    int[][] matches = A.perfectMatch(B, 0, 0);
    // test that the match occurred
    assertNotNull(matches);
    
    // test that they're proper inverses
    int[] matchesA2B = matches[0];
    int[] matchesB2A = matches[1];
    for(int i = 0; i < matchesA2B.length; i++) {
      assertEquals(i, matchesB2A[matchesA2B[i]]);
      assertEquals(i, matchesA2B[matchesB2A[i]]);
    }
    
    // should be an "identity match"
    for(int i = 0; i < matchesA2B.length; i++) {
      assertEquals(i, matchesA2B[i]);
      assertEquals(i, matchesB2A[i]);
    }
  }
  
  public void testNoMatch() throws Exception {
    StrokeGraph A = StrokeGraph.fromFile(file1);
    StrokeGraph B = new StrokeGraph();
    
    int[][] matches = A.perfectMatch(B, 0, 0);
    assertNull(matches);
  }
  
  public void testMatchRenumbered() throws Exception {
    StrokeGraph A = StrokeGraph.fromFile(file1);
    StrokeGraph B = StrokeGraph.fromFile(file2);
    
    int[][] matches = A.perfectMatch(B, 4, 3);
    // test that the match occurred
    assertNotNull(matches);
    
    // test that they're proper inverses
    int[] matchesA2B = matches[0];
    int[] matchesB2A = matches[1];
    for(int i = 0; i < matchesA2B.length; i++) {
      assertEquals(i, matchesB2A[matchesA2B[i]]);
      assertEquals(i, matchesA2B[matchesB2A[i]]);
    }
    
    Arrays.equals(matchesA2B, new int[] {23,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22});
  }
  
  public void testNearMatch() throws Exception {
    StrokeGraph A = StrokeGraph.fromFile(file2);
    StrokeGraph B = StrokeGraph.fromFile(file3);
    
    int[][] matches = A.perfectMatch(B, 0, 0);
    
    assertNull(matches);
  }
  
  public void testMatchIterator() throws Exception {
    StrokeGraph A = StrokeGraph.fromFile(file1);
    StrokeGraph B = StrokeGraph.fromFile(file2);
    
    int count = 0;
    for(int[][] match : A.allPerfectMatches(B)) {
      // test that the match occurred
      assertNotNull(match);
      
      // test that they're proper inverses
      int[] matchesA2B = match[0];
      int[] matchesB2A = match[1];
      for(int i = 0; i < matchesA2B.length; i++) {
        assertEquals(i, matchesB2A[matchesA2B[i]]);
        assertEquals(i, matchesA2B[matchesB2A[i]]);
      }
      count++;
    }
    assertEquals(1, count);
    //Arrays.equals(matchesA2B, new int[] {23,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22});
  }
  /*
  public void testReindex() throws Exception {
    StrokeGraph A = StrokeGraph.fromFile(file);
    // rotate first element to the end
    HalfEdge first = A.halfedges.remove(0);
    A.halfedges.add(first);
    int newZero = A.halfedges.size();
    
    for(HalfEdge e : A.halfedges) {
      if(e.clockwise == 0) e.clockwise = newZero;
      if(e.clockwise > 0) e.clockwise--;
      
      if(e.opposite == 0) e.opposite = newZero;
      if(e.opposite > 0) e.opposite--;
      
      if(e.forward == 0) e.forward = newZero;
      if(e.forward > 0) e.forward--;
    }

    A.toFile("/Users/markluffel/code/verkspace/research/src/data/example2.sgrf");
  }
  */
}
