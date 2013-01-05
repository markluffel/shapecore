/**
 * 
 */
package shapecore.tuple;

public class DoublePair {
	public double a,b;
	
	public DoublePair(double a, double b) {
		this.a = a;
		this.b = b;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DoublePair) {
			DoublePair that = (DoublePair) obj;
			return this.a == that.a && this.b == that.b;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Double.valueOf(a).hashCode() ^ Double.valueOf(b).hashCode(); // xor
	}
	
	@Override
	public String toString() {
		return "("+a+", "+b+")";
	}
}