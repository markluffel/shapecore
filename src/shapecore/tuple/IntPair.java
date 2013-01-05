/**
 * 
 */
package shapecore.tuple;

public class IntPair {
	public int fst;
	public int snd;
	
	public IntPair(int first, int second) {
		this.fst = first;
		this.snd = second;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof IntPair) {
			IntPair that = (IntPair) obj;
			return this.fst == that.fst && this.snd == that.snd;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(fst).hashCode() ^ Integer.valueOf(snd).hashCode(); // xor
	}
	
	@Override
	public String toString() {
		return "("+fst+", "+snd+")";
	}
	
	public IntPair clone() {
		return new IntPair(fst, snd);
	}
}