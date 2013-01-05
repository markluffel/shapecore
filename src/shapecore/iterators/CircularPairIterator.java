package shapecore.iterators;

import java.util.Iterator;

import shapecore.tuple.Pair;




/**
 * An iterator that returns sequential pairs of elements from a wrapped iterator,
 * and whose last pair is from the last element of the wrapped iterator to the first element.
 * 
 * Useful for constructing edge offsets from a sequence of points.
 *
 */
public class CircularPairIterator<T> implements Iterable<Pair<T,T>>, Iterator<Pair<T,T>> {
	private Iterator<T> wrapped;
	private T first, prev;
	private boolean done;
	
	public CircularPairIterator(Iterable<T> wrapped) {
		this(wrapped.iterator());
	}
	
	public CircularPairIterator(Iterator<T> wrapped) {
		this.wrapped = wrapped;
		if(wrapped.hasNext()) {
			prev = first = wrapped.next();
			done = false;
		} else {
			done = true;
		}
	}
	
	public boolean hasNext() {
		return !done;
	}

	public Pair<T,T> next() {
		T next;
		if(wrapped.hasNext()) {
			next = wrapped.next();
		} else {
			next = first;
			done = true;
		}
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