package shapecore.fancy;

import java.util.ArrayList;

import shapecore.Oplet;


public class LaplacianPyramid1D {

  public ArrayList<float[]> averages = new ArrayList<float[]>();
  public ArrayList<float[]> differences = new ArrayList<float[]>();
  
  void build(float[] data) {
    build(data, 1);
  }
  
  public void build(float[] data, int cutoff) {
    averages.clear();
    differences.clear();
    
    float[] cur = data;
    while(cur.length > cutoff) {
      float[] avg = new float[cur.length/2];
      float[] dif = new float[cur.length];
      for(int i = 0; i < avg.length; i++) {
        int j = i<<1;
        float av = avg[i] = (cur[j]+cur[j+1])/2;
        dif[j] = cur[j]-av;
        dif[j+1] = cur[j+1]-av;
      }
      if(dif.length%2 == 1) { // for non-power of two
        dif[dif.length-1] = cur[cur.length-1];
      }
      averages.add(avg);
      differences.add(dif);
      cur = avg;
    }
  }  
  
  public float[] collapse() {
    // TODO: make the shift var into an actual bitshift
    float[] result = new float[differences.get(0).length];
    
    // take the smallest average
    float[] avg = averages.get(averages.size()-1);
    int shift = result.length/avg.length;
    for(int i = 0; i < avg.length; i++) {
      int offset = i*shift;
      for(int j = 0; j < shift; j++) {
        result[offset+j] = avg[i];
      }
    }
    
    // apply each difference layer
    for(int k = 0; k < differences.size(); k++) {
      float[] dif = differences.get(k);
      shift = result.length/dif.length;
      for(int i = 0; i < dif.length; i++) {
        int offset = i*shift;
        for(int j = 0; j < shift; j++) {
          result[offset+j] += dif[i];
        }
      }
    }
    return result;
  }
  
  public LaplacianPyramid1D blend(LaplacianPyramid1D that, float t) {
    if(!compatible(that)) throw new IllegalArgumentException();
    
    // linearly interpolate all the differences
    LaplacianPyramid1D blended = new LaplacianPyramid1D();
    for(int i = 0; i < this.differences.size(); i++) {
      blended.differences.add(
          Oplet.blend(this.differences.get(i),
                that.differences.get(i),
                t));
      blended.averages.add(new float[0]);
      // to get the indexing right in collapse
    }
    // also interpolate the final (smallest) average
    int last = this.averages.size()-1;
    blended.averages.set(last,
        Oplet.blend(this.averages.get(last),
              that.averages.get(last),
              t));
    
    // the user can collapse this easily
    return blended;
  }
  
  boolean compatible(LaplacianPyramid1D that) {
    return this.averages.size() == that.averages.size()
        && this.differences.size() == that.differences.size()
        && this.differences.get(0).length == this.differences.get(0).length;
  }

  public void clear() {
    averages.clear();
    differences.clear();
  }
}
