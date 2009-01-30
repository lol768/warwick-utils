package uk.ac.warwick.util.collections.google;

import java.util.Comparator;
import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * A class to allow Ruby-style internal iterator methods on java Iterables.
 * 
 * @author cusaab
 *
 */
public final class InternalIterable<T> implements Iterable<T> {

    private Iterable<T> delegate;

    public InternalIterable(final Iterable<T> delegateIterable) {
        this.delegate = delegateIterable;
    }

    /**
     * Return a new Iterable, containing only those elements of _delegate_ for which
     * predicate returns true
     * 
     */
    public InternalIterable<T> filter(final Predicate<? super T> predicate) {
        return new InternalIterable<T>(Iterables.filter(delegate,predicate));
    }

    /**
     * Return a new Iterable where each element is the result of applying _transform_
     * to the corresponding element in _delegate_.
     * 
     * c.f. "Map" in Ruby's Enumerable class (I stuck with "Transform" to avoid confusion with
     * java.util.map
     * 
     */
    public <X>InternalIterable<X> transform(final Function<T,X> transform) {
        return new InternalIterable<X>(Iterables.transform(delegate, transform));
    }

    /**
     * Return the underlying iterator
     */
    
    public Iterator<T> iterator() {
        return delegate.iterator();
    }
    
    /** 
     * Convenience method to construct a new InternalIterable. Infers the type from the parameter
     * to save typing
     */
    public static <X>InternalIterable<X> of(final Iterable<X> source){
        return new InternalIterable<X>(source);
    }

    /**
     * Return true if predicate returns true for every element in _delegate_.
     * 
     * Stops evaluation after the first false result
     * 
     */
    public boolean all(final Predicate<T> test) {
        return Iterables.all(delegate, test);
    }

    /**
     * Return true if predicate returns true for at least one element in _delegate_
     * 
     *  Stops evaluation after the first true result.
     */
    public boolean any(final Predicate<T> test) {
        return Iterables.any(delegate, test);
    }

    /**
     * Return a copy of the iterable, sorted according to the supplied comparator.
     * n.b. there is no version of this method that doesn't take a comparator, (and thus uses natural order)
     * because we don't constrain the Iterables to contain only Comparable objects.
     */
    public InternalIterable<T> sort(final Comparator<T> comparator ){
        return new InternalIterable<T>(Lists.sortedCopy(delegate,comparator));
    }

    /**
     * Return the first element for which _match_ is true.
     * thows NoSuchElementException if no element matches
     */
    public T find(Predicate<? super T> match) {
        return Iterables.find(delegate, match);
    }
    
    /**
     * Combines the elements of _delegate_ by applying the Injector function to an accumulator value (memo) and each element in turn. 
     * At each step, memo is set to the value returned by Injector.apply. 
     * The first parameter lets you supply an initial value for memo. 
     */
    public <F> F inject(final F initialMemo,final  Injector<F,T> injector) {
        F memo = initialMemo;
        for (T element: delegate) {
            memo = injector.apply(memo, element);
        }
        return memo;
    }

    /**
     * A binary Functor. i.e. a function callback that takes two arguments.
     */
    public interface Injector<F,T>{
        F apply(F memo, T element);
    }
    
}
