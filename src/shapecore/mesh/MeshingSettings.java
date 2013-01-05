/**
 * 
 */
package shapecore.mesh;

public class MeshingSettings {
  
  public boolean processBoundary = true;
  public float interiorSampleSpacing = 10;
  public float minBoundaryEdgeLength = 15;
  public float maxBoundaryEdgeLength = 20;
  public float boundarySmoothingAmount = 0.5f;
  public int numBoundarySmoothingSteps = 2;
  public int numLloydSteps = 5;
  
  public MeshingSettings clone() {
    MeshingSettings result = new MeshingSettings();
    result.processBoundary = processBoundary;
    result.interiorSampleSpacing = interiorSampleSpacing;
    result.minBoundaryEdgeLength = minBoundaryEdgeLength;
    result.maxBoundaryEdgeLength = maxBoundaryEdgeLength;
    result.boundarySmoothingAmount = boundarySmoothingAmount;
    result.numBoundarySmoothingSteps = numBoundarySmoothingSteps;
    result.numLloydSteps = numLloydSteps;
    return result;
  }
  
  public MeshingSettings fewerLloydSteps() {
    MeshingSettings newSettings = clone();
    newSettings.numLloydSteps = numLloydSteps-1;
    return newSettings;
  }
}