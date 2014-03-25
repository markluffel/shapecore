package shapecore;

import java.util.Arrays;
import java.util.List;

public class SamplablePolyline3 {

  public List<pt3> points;
  float[] distances; // prefix sum of the arc length at each point

protected SamplablePolyline3() {}
  
  public SamplablePolyline3(List<pt3> pts) {
    this.points = pts;
    recomputeDistances();
  }
  
  public float getLength() {
    return distances[distances.length-1];
  }

  int sampleIndex(float t) {
    return sampleIndexByDistance(t*getLength());
  }
  
  int sampleIndexByDistance(float distance) {
    float length = getLength();
    
    if(distance <= 0) return 0;
    if(distance >= length) return points.size()-1;
    
    int left = Arrays.binarySearch(distances, distance);
    
    if(left > 0) {
      return left; // exactly on a point
    } else {
      return -left-2; // between two points, return the index of the prior point
    }
  }
  
  /**
   * 
   * @param t a value between
   * @return
   */
  public pt3 sample(float t) {
    return sampleByDistance(t*getLength());
  }
  
  void recomputeDistances() {
    distances = new float[points.size()];
    distances[0] = 0;
    for(int i = 1; i < distances.length; i++) {
      distances[i] = distances[i-1] + points.get(i).dist(points.get(i-1));
    }
  }
  
  public pt3 sampleByDistance(float distance) {
    if(distance < 0) distance = getLength()+distance; // allow negative indexes
    if(distance >= getLength()) return points.get(points.size()-1).get(); // if it's too big, clamp it
    
    int left = sampleIndexByDistance(distance);
    int right = left+1;
    if(right > points.size()) return points.get(left); // at endpoint 
    float lDist = distances[left], rDist = distances[right];
    pt3 lPoint = points.get(left), rPoint = points.get(right);
    
    if(rDist == lDist) return lPoint.get(); //  for coincident points
    float localT = (distance-lDist)/(rDist-lDist);
    
    return Oplet.lerp(lPoint, rPoint, localT);
  }
}
