package shapecore.iterators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import shapecore.tuple.Pair;




public class StridedCircularPairIterator<T> implements Iterable<Pair<T,T>>, Iterator<Pair<T,T>> {
	private Iterator<T> wrapped;
	private Queue<T> first, prev;
	private boolean done;
	
	public StridedCircularPairIterator(Iterable<T> wrapped, int strideSize) {
		this(wrapped.iterator(), strideSize);
	}
	
	public StridedCircularPairIterator(Iterator<T> wrapped, int strideSize) {
		if(strideSize < 2) throw new IllegalArgumentException("stride size must be greater than 1");
		this.wrapped = wrapped;
		this.first = new LinkedList<T>();
		this.prev = new LinkedList<T>();
		for(int i = 0; i < strideSize; i++) {
    		if(wrapped.hasNext()) {
    			T item = wrapped.next();
    			prev.add(item);
    			first.add(item);
    			done = false;
    		} else {
    			done = true;
    		}
		}
	}
	
	public boolean hasNext() {
		return !done;
	}

	public Pair<T,T> next() {
		// get the next element, either from the wrapped iterator
		// or from the buffer we saved at the beginning
		T next;
		if(wrapped.hasNext()) {
			next = wrapped.next();
		} else if(!first.isEmpty()) {
			next = first.poll();
			if(first.isEmpty()) {
				done = true;
			}
		} else {
			throw new IllegalStateException();
		}
		
		Pair<T,T> result = new Pair<T,T>(prev.poll(), next);
		prev.offer(next);
		return result;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public Iterator<Pair<T, T>> iterator() {
		return this;
	}		
}