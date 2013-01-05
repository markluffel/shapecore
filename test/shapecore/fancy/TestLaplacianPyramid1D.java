package shapecore.fancy;

import java.util.Arrays;

import shapecore.fancy.LaplacianPyramid1D;


import junit.framework.TestCase;

public class TestLaplacianPyramid1D extends TestCase {

  public void testBuild() {
    LaplacianPyramid1D lp = new LaplacianPyramid1D();
    
    float[] data = new float[] {2,3,4,5,5,4,5,4};
    lp.build(data);
    
    assertEquals(lp.averages.size(), 3);
    assertEquals(lp.differences.size(), 3);
    
    assertTrue("first level average", Arrays.equals(lp.averages.get(0), new float[] {2.5f, 4.5f, 4.5f, 4.5f}));
    assertTrue("second level average", Arrays.equals(lp.averages.get(1), new float[] {3.5f, 4.5f}));
    assertTrue("third level average", Arrays.equals(lp.averages.get(2), new float[] {4}));
    
    assertTrue("first level difference", Arrays.equals(lp.differences.get(0), 
        new float[] {-0.5f, +0.5f, -0.5f, +0.5f, +0.5f, -0.5f, +0.5f, -0.5f}));
    assertTrue("second level difference", Arrays.equals(lp.differences.get(1), 
        new float[] {-1.0f, +1.0f, 0, 0}));
    assertTrue("second level difference", Arrays.equals(lp.differences.get(2), 
        new float[] {-0.5f, +0.5f}));
  }
  
  public void testBuildOdd() {
    LaplacianPyramid1D lp = new LaplacianPyramid1D();
    
    float[] data = new float[] {2,3,4,5,5,4,5,4,7,7,7};
    lp.build(data);
    
    assertEquals(lp.averages.size(), 3);
    assertEquals(lp.differences.size(), 3);
    
    assertTrue("first level average", Arrays.equals(lp.averages.get(0), new float[] {2.5f, 4.5f, 4.5f, 4.5f, 7}));
    assertTrue("second level average", Arrays.equals(lp.averages.get(1), new float[] {3.5f, 4.5f}));
    assertTrue("third level average", Arrays.equals(lp.averages.get(2), new float[] {4}));
    
    assertTrue("first level difference", Arrays.equals(lp.differences.get(0), 
        new float[] {-0.5f, +0.5f, -0.5f, +0.5f, +0.5f, -0.5f, +0.5f, -0.5f, 0, 0, 7}));
    assertTrue("second level difference", Arrays.equals(lp.differences.get(1), 
        new float[] {-1.0f, +1.0f, 0, 0, 7}));
    assertTrue("second level difference", Arrays.equals(lp.differences.get(2), 
        new float[] {-0.5f, +0.5f}));
  }
  
  public void testRoundTrip() {
    LaplacianPyramid1D lp = new LaplacianPyramid1D();
    
    float[] data = new float[] {2,3,4,5,5,4,5,4};
    lp.build(data);
    float[] newData = lp.collapse();
    assertTrue(Arrays.equals(newData, data));
  }
  
  public void testNoBlend() {
    LaplacianPyramid1D lp = new LaplacianPyramid1D();
    
    float[] data = new float[] {1,2,3,4};
    lp.build(data);
    float[] newData = lp.collapse();
    assertTrue(Arrays.equals(newData, data));
  }
  
}
