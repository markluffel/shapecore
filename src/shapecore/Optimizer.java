package shapecore;

import java.util.ArrayList;



public abstract class Optimizer {

  /**
   * Get the max/min bounds for all params
   * @return an n x 2 dimensional array
   */
  public abstract float[][] getBounds();
  
  /**
   * Get the error for the current model params
   * @return
   */
  public abstract float getError();
  
  /**
   * Set the model params to the given values 
   * @param params
   */
  public abstract void setParams(float[] params);
  
  int gridSteps, topN;
  float[] loBounds;
  float[] hiBounds;
  float[] originalLoBounds;
  float[] originalHiBounds;
  ArrayList<ExploreLocation> exploreQueue;
  
  // for post-mortem viz
  private ArrayList<float[]> recordedErrors;
  private float min, max;
  
  public float[] run() {
    return run(5, 2, 3);
  }
  
  /**
   * We evenly sample every dimension
   * 
   * @param gridSteps
   */
  public float[] run(int gridSteps, int depth, int topN) {
    recordedErrors = new ArrayList<float[]>();
    exploreQueue = new ArrayList<ExploreLocation>();
    
    this.gridSteps = gridSteps;
    if(originalLoBounds == null) {
      float[][] bounds = getBounds();
      
      loBounds = new float[bounds.length];
      hiBounds = new float[bounds.length];
      originalLoBounds = new float[bounds.length];
      originalHiBounds = new float[bounds.length];
      for(int i = 0; i < bounds.length; i++) {
        originalLoBounds[i] = loBounds[i] = bounds[i][0];
        originalHiBounds[i] = hiBounds[i] = bounds[i][1];
      }
    } else {
      System.arraycopy(originalLoBounds, 0, loBounds, 0, originalLoBounds.length);
      System.arraycopy(originalHiBounds, 0, hiBounds, 0, originalHiBounds.length);
    }
    ExploreLocation el = new ExploreLocation();
    el.loBounds = loBounds;
    el.hiBounds = hiBounds;
    el.error = Float.MAX_VALUE;
    exploreQueue.add(el);
    
    min = Float.MAX_VALUE;
    max = Float.MIN_VALUE;
    
    float[] bestLocation = null;
    float bestError = Float.MAX_VALUE;
    int currentDepth = 0;
    while(!exploreQueue.isEmpty()) {
      el = exploreQueue.remove(0);
        
      loBounds = el.loBounds;
      hiBounds = el.hiBounds;
      
      if(el.error < bestError) {
        bestError = el.error;
        bestLocation = el.location;
      }
      if(el.depth >= depth) {
        continue; // skip processing this one if it is deep enough already
      } else {
        currentDepth = el.depth+1;
      }

      // find the set of parameters that we're going to test 
      float[][] gridPoints = makeGridPoints(loBounds, hiBounds, gridSteps);
      float[] errors = new float[gridPoints.length];
      
      for(int i = 0; i < gridPoints.length; i++) {
        // set those parameters on the underlying model
        setParams(gridPoints[i]);
        // and test the resulting error in the mode
        errors[i] = getError();
        recordError(gridPoints[i], errors[i]);
        if(errors[i] < min) min = errors[i];
        if(errors[i] > max) max = errors[i];
      }
      
      // take the best N locations are record them for further exploration
      for(int k = 0; k < topN; k++) {
        int exploreCenterIndex = minIndex(errors); // could optimize by getting all the topN in one pass
        if(exploreCenterIndex == -1) break;
        float[] location = gridPoints[exploreCenterIndex];
        enqueueLocation(location, errors[exploreCenterIndex], currentDepth);
        errors[exploreCenterIndex] = Float.MAX_VALUE; // blank it and get another
      }
    }
    
    if(bestLocation != null) {
      setParams(bestLocation);
    }
    return bestLocation; 
  }
  
  private void enqueueLocation(float[] location, float error, int depth) {
    ExploreLocation el = new ExploreLocation();
    el.location = location;
    el.error = error;
    el.loBounds = new float[loBounds.length];
    el.hiBounds = new float[hiBounds.length];
    el.depth = depth;
    
    float[] bestLocation = location;
    for(int i = 0; i < bestLocation.length; i++) {
      float dimStep = dimensionStep(i, loBounds, hiBounds);
      el.loBounds[i] = constrain(bestLocation[i]-dimStep, loBounds[i], hiBounds[i]);
      el.hiBounds[i] = constrain(bestLocation[i]+dimStep, loBounds[i], hiBounds[i]);
    }
    exploreQueue.add(el);
  }

  private void recordError(float[] index, float error) {
    float[] entry = new float[index.length+1];
    for(int i = 0; i< index.length; i++) {
      entry[i] = index[i];
    }
    entry[index.length] = error;
    getRecordedErrors().add(entry);
  }

  float constrain(float value, float left, float right) {
    if(left < right) {
      return Math.min(Math.max(left, value), right);
    } else {
      return Math.min(Math.max(right, value), left);
    }
  }

  float dimensionStep(int dim, float[] loBounds, float[] hiBounds) {
    return (hiBounds[dim]-loBounds[dim])/gridSteps;
  }
  
  static float[][] makeGridPoints(float[] loBounds, float[] hiBounds, int gridSteps) {
    int numDimensions = loBounds.length;
    int numTestPoints = (int) Math.pow(gridSteps, numDimensions);
    final int[] dimensions = new int[numDimensions];
    final float[] dimensionSteps = new float[numDimensions];
    for(int i = 0; i < dimensions.length; i++) {
      dimensions[i] = gridSteps;
      dimensionSteps[i] = 1/((float)dimensions[i]-1);
    }
    
    float[][] gridPoints = new float[numTestPoints][numDimensions];
    for(int i = 0; i < gridPoints.length; i++) {
      int[] index = index(i, dimensions);
      for(int j = 0; j < numDimensions; j++) {
        // TODO: can inline lerp and premultiply
        gridPoints[i][j] = lerp(loBounds[j], hiBounds[j], index[j]*dimensionSteps[j]);
      }
    }
    return gridPoints;
  }
  
  static float lerp(float A, float B, float t) {
    return A*(1-t) + B*t;
  }

  /**
   * Given an index in a 1-d matrix, find an index in n-dimensional matrix
   * 
   * @param i the input index
   * @param dimensions the size of each dimension
   * @return  an array that is the same lengths as the dimensions parameter
   */
  public static int[] index(int i, final int[] dimensions) {
    int[] index = new int[dimensions.length];
    
    for(int ii = dimensions.length-1; ii >= 0; ii--) {
      int mod = i%dimensions[ii];
      int div = i/dimensions[ii];
      index[ii] = mod;
      i = div;
    }
    return index;
  }
  
  // find the index of the smallest value
  static int minIndex(float[] values) {
    int minIndex = -1;
    float minValue = Float.MAX_VALUE;
    for(int i = 0; i < values.length; i++) {
      if(values[i] < minValue) {
        minValue = values[i];
        minIndex = i;
      }
    }
    return minIndex;
  }

  public ArrayList<float[]> getRecordedErrors() {
    return recordedErrors;
  }

  public float getMin() {
    return min;
  }

  public float getMax() {
    return max;
  }

  class ExploreLocation {
    float[] loBounds, hiBounds, location;
    float error = Float.MAX_VALUE;
    int depth; // used to limit the search
  }
}
