/**
 * 
 */
package shapecore.cluster;

import shapecore.Oplet;

public class KMeans {
  float[][] values;
  int dim,numClusters;
  float[][] clusters;
  float[][] clusterSum;
  int[] memberCount;

  public KMeans(float[][] values, int numClusters) {
    this.values = values;
    this.numClusters = numClusters;
    this.dim = values[0].length;
    this.clusters = new float[numClusters][dim];
    this.clusterSum = new float[numClusters][dim];
    this.memberCount = new int[numClusters];
    
    seed();
  }
  
  /**
   * Get the cluster label for a particular instance.
   * 
   * @param val
   * @return
   */
  public int label(float[] val) {
    float minDistSq = Float.MAX_VALUE;
    int bestCluster = -1;
    for(int i = 0; i < clusters.length; i++) {
      float d = sqdist(clusters[i], val);
      if(d < minDistSq) {
        bestCluster = i;
        minDistSq = d;
      }
    }
    return bestCluster;
  }
  
  public float[] clusterCenter(float[] val) {
    return clusters[label(val)];
  }

  void seed() {
    for(int j = 0; j < clusters.length; j++) {
      seed(j);
    }
  }
  
  void seed(int j) {
    // pick a random attribute value from a random sample
    // is it better to pick the whole sample?
    // or better to splice them together like this?
    for(int k = 0; k < dim; k++) {
      clusters[j][k] = values[(int)Math.random()*values.length][k];
    }
  }
  
  public float step() {
    // clear out the accumulators
    for(int j = 0; j < clusters.length; j++) {
      for(int k = 0; k < dim; k++) {
        clusterSum[j][k] = 0;
      }
      memberCount[j] = 0;
    }
    
    // compute closest cluster per point, accumulate contributions
    for(int i = 0; i < values.length; i++) {
      float bestDistSq = Float.MAX_VALUE;
      int bestCluster = -1;
      for(int j = 0; j < clusters.length; j++) {
        float d = sqdist(values[i], clusters[j]);
        if(d < bestDistSq) {
          bestCluster = j;
          bestDistSq = d;
        }
      }
      if(bestCluster >= 0) {
        memberCount[bestCluster]++;
        for(int k = 0; k < dim; k++) {
          clusterSum[bestCluster][k] += values[i][k];
        }
      } else {
        System.out.println("nan?");
      }
    }
    
    // normalize clusterSum to center, accumulate amount of change this iteration
    float clusterMovement = 0;
    for(int j = 0; j < clusters.length; j++) {
      if(memberCount[j] == 0) {
        // reseed, because this cluster contains nothing
        seed(j);
        clusterMovement += 100; // always do at least one more iteration after this
        
      } else {
        float sqdiff = 0;
        for(int k = 0; k < dim; k++) {
          float newValue = clusterSum[j][k]/memberCount[j];
          float diff = clusters[j][k] - newValue;
          sqdiff += diff*diff;
          clusters[j][k] = newValue;
        }
        clusterMovement += Oplet.sqrt(sqdiff);
      }
    }
    
    return clusterMovement;
  }
  
  private static float sqdist(float[] a, float[] b) {
    int len = Math.min(a.length, b.length); // for searching via prefix
    float sum = 0;
    for(int i = 0; i < len; i++) {
      float diff = a[i]-b[i];
      sum += diff*diff;
    }
    return sum;
  }

  public void iterate(float minMovement, int maxIterations) {
    int iterations = 0;
    float movement;
    do {
      movement = step();
      iterations++;
    } while(movement > minMovement && iterations < maxIterations);
    
  }

  public float[][] getCenters() {
    return clusters;
  }
}