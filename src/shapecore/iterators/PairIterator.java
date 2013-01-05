package shapecore.iterators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import shapecore.tuple.Pair;



/**
 * An iterator that returns sequential pairs of elements from a wrapped iterator,
 * and whose last pair is from the last element of the wrapped iterator to the first element.
 * 
 * Useful for constructing edge offsets from a sequence of points.
 *
 */
public class PairIterator<T> implements Iterable<Pair<T,T>>, Iterator<Pair<T,T>> {
	protected Iterator<T> wrapped;
	private T prev;
	
	public PairIterator(Iterable<T> wrapped) {
		this(wrapped.iterator());
	}
	
	public PairIterator(Iterator<T> wrapped) {
		this.wrapped = wrapped;
		if(wrapped.hasNext()) {
			prev = wrapped.next();
		}
	}
	
	public static <T extends Comparable<? super T>> PairIterator<T> sorted(List<T> unsorted) {
		ArrayList<T> sorted = new ArrayList<T>();
		sorted.addAll(unsorted);
		Collections.sort(sorted);
		return new PairIterator<T>(sorted);
	}
	
	public boolean hasNext() {
		return wrapped.hasNext();
	}

	public Pair<T,T> next() {
		T next = wrapped.next();
		Pair<T,T> result = new Pair<T,T>(prev, next);
		prev = next;
		return result;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public Iterator<Pair<T, T>> iterator() {
		return this;
	}		
}