package shapecore.interfaces;

public interface Addable<T> {
  /** Add something else in */
  T add(T that);
  T get();
}
