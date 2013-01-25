package shapecore;

public class Max<T> {
  private float max = -Float.MAX_VALUE;
  private T best = null;
  float getMin() { return max; }
  T getBest() { return best; }
  void update(T obj, float value) {
    if(value > max) {
      max = value;
      best = obj;
    }
  }
}