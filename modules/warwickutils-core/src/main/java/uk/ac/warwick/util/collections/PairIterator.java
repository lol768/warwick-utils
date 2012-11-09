package uk.ac.warwick.util.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
/**
 * Iterator which steps through two iterators in sync
 * 
 * Use the static of(Iterator,Iterator) method to get an iterable instance which can
 * then be used in a 
 * @code for (Foo foo: foos)
 * style loop.
 */
public class PairIterator<L,R> implements Iterator<Pair<L,R>> {

	private final Iterator<L> leftIterator;
	private final Iterator<R> rightIterator;
	
	public PairIterator(Iterator<L> left, Iterator<R> right){
		this.leftIterator = left;
		this.rightIterator = right;
	}
	
	public PairIterator(Iterable<L> left, Iterable<R> right){
		this.leftIterator = left.iterator();
		this.rightIterator = right.iterator();
		
	}
	
	public static <E,F> Iterable<Pair<E,F>> of(final Iterable<E> left, final Iterable<F> right){
		return new Iterable<Pair<E,F>>(){

			public Iterator<Pair<E, F>> iterator() {
				return new PairIterator<E,F>(left, right);
			}
			
		};
	}
	
	public boolean hasNext() {
		return leftIterator.hasNext() || rightIterator.hasNext();
	}

	public Pair<L,R> next() {
		if (!hasNext()){
			throw new NoSuchElementException();			
		}
		L left = leftIterator.hasNext()?leftIterator.next():null;
		R right = rightIterator.hasNext()?rightIterator.next():null;
		return Pair.of(left, right);
	}

	public void remove() {
	    leftIterator.remove();
	    rightIterator.remove();
	}

}
