package shapecore;

import java.util.Arrays;

import shapecore.Optimizer;



import junit.framework.TestCase;

public class OptimizerTest extends TestCase {

  
  public void testUniformIndex() throws Exception {
    int[] dimensions = new int[] {3,3,3};
     
    assertTrue(Arrays.equals(Optimizer.index(0, dimensions), new int[] {0,0,0}));
    assertTrue(Arrays.equals(Optimizer.index(1, dimensions), new int[] {0,0,1}));
    assertTrue(Arrays.equals(Optimizer.index(2, dimensions), new int[] {0,0,2}));
    
    assertTrue(Arrays.equals(Optimizer.index(3, dimensions), new int[] {0,1,0}));
    assertTrue(Arrays.equals(Optimizer.index(4, dimensions), new int[] {0,1,1}));
    assertTrue(Arrays.equals(Optimizer.index(5, dimensions), new int[] {0,1,2}));
    
    assertTrue(Arrays.equals(Optimizer.index(6, dimensions), new int[] {0,2,0}));
    assertTrue(Arrays.equals(Optimizer.index(7, dimensions), new int[] {0,2,1}));
    assertTrue(Arrays.equals(Optimizer.index(8, dimensions), new int[] {0,2,2}));
    
    
    assertTrue(Arrays.equals(Optimizer.index(9,  dimensions), new int[] {1,0,0}));
    assertTrue(Arrays.equals(Optimizer.index(10, dimensions), new int[] {1,0,1}));
    assertTrue(Arrays.equals(Optimizer.index(11, dimensions), new int[] {1,0,2}));
    
    assertTrue(Arrays.equals(Optimizer.index(12, dimensions), new int[] {1,1,0}));
    assertTrue(Arrays.equals(Optimizer.index(13, dimensions), new int[] {1,1,1}));
    assertTrue(Arrays.equals(Optimizer.index(14, dimensions), new int[] {1,1,2}));
    
    assertTrue(Arrays.equals(Optimizer.index(15, dimensions), new int[] {1,2,0}));
    assertTrue(Arrays.equals(Optimizer.index(16, dimensions), new int[] {1,2,1}));
    assertTrue(Arrays.equals(Optimizer.index(17, dimensions), new int[] {1,2,2}));
    
    
    assertTrue(Arrays.equals(Optimizer.index(18, dimensions), new int[] {2,0,0}));
    assertTrue(Arrays.equals(Optimizer.index(19, dimensions), new int[] {2,0,1}));
    assertTrue(Arrays.equals(Optimizer.index(20, dimensions), new int[] {2,0,2}));
    
    assertTrue(Arrays.equals(Optimizer.index(21, dimensions), new int[] {2,1,0}));
    assertTrue(Arrays.equals(Optimizer.index(22, dimensions), new int[] {2,1,1}));
    assertTrue(Arrays.equals(Optimizer.index(23, dimensions), new int[] {2,1,2}));
    
    assertTrue(Arrays.equals(Optimizer.index(24, dimensions), new int[] {2,2,0}));
    assertTrue(Arrays.equals(Optimizer.index(25, dimensions), new int[] {2,2,1}));
    assertTrue(Arrays.equals(Optimizer.index(26, dimensions), new int[] {2,2,2}));
  }
  
  public void testOddIndex() throws Exception {
    int[] dimensions = new int[] {3,4};
    
    assertTrue(Arrays.equals(Optimizer.index(0, dimensions), new int[] {0,0}));
    assertTrue(Arrays.equals(Optimizer.index(1, dimensions), new int[] {0,1}));
    assertTrue(Arrays.equals(Optimizer.index(2, dimensions), new int[] {0,2}));
    assertTrue(Arrays.equals(Optimizer.index(3, dimensions), new int[] {0,3}));
    
    assertTrue(Arrays.equals(Optimizer.index(4, dimensions), new int[] {1,0}));
    assertTrue(Arrays.equals(Optimizer.index(5, dimensions), new int[] {1,1}));
    assertTrue(Arrays.equals(Optimizer.index(6, dimensions), new int[] {1,2}));
    assertTrue(Arrays.equals(Optimizer.index(7, dimensions), new int[] {1,3}));
    
    assertTrue(Arrays.equals(Optimizer.index(8,  dimensions), new int[] {2,0}));
    assertTrue(Arrays.equals(Optimizer.index(9,  dimensions), new int[] {2,1}));
    assertTrue(Arrays.equals(Optimizer.index(10, dimensions), new int[] {2,2}));
    assertTrue(Arrays.equals(Optimizer.index(11, dimensions), new int[] {2,3}));
  }
  
  public void testMakeGridPoints() {
    float[][] points = Optimizer.makeGridPoints(new float[] {7,33}, new float[] {11,99}, 3);
    // toString because Array.equals only works one level deep
    assertEquals(Arrays.deepToString(points), Arrays.deepToString(new float[][] {
        {7,33},
        {7,66},
        {7,99},
        {9,33},
        {9,66},
        {9,99},
        {11,33},
        {11,66},
        {11,99}
    }));
    
    float[][] points2 = Optimizer.makeGridPoints(new float[] {-2, 7,33}, new float[] {-6,11,99}, 3);
    assertEquals(Arrays.deepToString(points2), Arrays.deepToString(new float[][] {
        {-2,7,33},  {-2,7,66},  {-2,7,99},
        {-2,9,33},  {-2,9,66},  {-2,9,99}, 
        {-2,11,33}, {-2,11,66}, {-2,11,99},

        {-4,7,33},  {-4,7,66},  {-4,7,99},
        {-4,9,33},  {-4,9,66},  {-4,9,99}, 
        {-4,11,33}, {-4,11,66}, {-4,11,99},

        {-6,7,33},  {-6,7,66},  {-6,7,99},
        {-6,9,33},  {-6,9,66},  {-6,9,99}, 
        {-6,11,33}, {-6,11,66}, {-6,11,99},
    }));
  }
}
