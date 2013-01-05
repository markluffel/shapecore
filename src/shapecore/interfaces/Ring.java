package shapecore.interfaces;

public interface Ring<T> extends Addable<T>, Scalable<T> {
  T addScaledBy(float alpha, T that);
  T zero();
}
