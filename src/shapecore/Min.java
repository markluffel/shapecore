package shapecore;

public class Min<T> {
  private float min = Float.MAX_VALUE;
  private T best = null;
  float getMin() { return min; }
  T getBest() { return best; }
  void update(T obj, float value) {
    if(value < min) {
      min = value;
      best = obj;
    }
  }
}