/**
 * 
 */
package shapecore.tuple;

import java.util.Map.Entry;

public class Pair<S,T> {
	public S fst;
	public T snd;
	
	public Pair(S fst, T snd) {
		this.fst = fst;
		this.snd = snd;
	}
	public String toString() {
		return "("+fst+", "+snd+")";
	}
	public static <T> Pair<T, T> make(T fst, T snd) {
		return new Pair<T,T>(fst,snd);
	}
	public static <S,T> Pair<S, T> make(Entry<S, T> entry) {
		return new Pair<S, T>(entry.getKey(), entry.getValue());
	}
	
	public int hashCode() {
    return fst.hashCode() ^ snd.hashCode();
  }
  
  public boolean equals(Object that) {
    if(that instanceof Pair) {
      Pair other = (Pair)that;
      return other.fst.equals(this.fst) && other.snd.equals(this.snd);
    } else {
      return false;
    }
  }
}