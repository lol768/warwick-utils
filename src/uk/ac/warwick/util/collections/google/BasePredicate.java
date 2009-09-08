package uk.ac.warwick.util.collections.google;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A predicate with added methods to support common functions.
 * <p>
 * The {@link #from(Predicate)} method returns the equivalent {@code
 * BasePredicate} instance for a pre-existing comparator. You can also skip the
 * predicate step and extend {@code BasePredicate} directly:
 * 
 * <pre>
 * BasePredicate&lt;String&gt; lowercasePredicate = new BasePredicate&lt;String&gt;() {
 * 	public boolean apply(String target) {
 * 		return target.isLowerCase();
 * 	}
 * };
 * </pre>
 * 
 * <p>
 * There are equivalent statics here to represent the predicates returned from
 * {@link Predicates}.
 * 
 * @author Mat
 */
public abstract class BasePredicate<T> implements Predicate<T> {

	@SuppressWarnings("unchecked")
	public static <T> BasePredicate<T> from(Predicate<T> predicate) {
		return (predicate instanceof BasePredicate ? (BasePredicate<T>) predicate
				: new WrappedPredicate<T>(predicate));
	}

	public abstract boolean apply(T object);

	/**
	 * Returns the elements of {@code unfiltered} that satisfy a predicate. The
	 * resulting iterable's iterator does not support {@code remove()}.
	 */
	public Iterable<T> filter(Iterable<T> unfiltered) {
		return Iterables.filter(unfiltered, this);
	}

	/**
	 * Returns the first element in {@code iterable} that satisfies the given
	 * predicate.
	 * 
	 * @throws NoSuchElementException
	 *             if no element in {@code iterable} matches the given predicate
	 */
	public T find(Iterable<T> iterable) {
		return Iterables.find(iterable, this);
	}

	/**
	 * Returns {@code true} if one or more elements in {@code iterable} satisfy
	 * the predicate.
	 */
	public boolean any(Iterable<T> iterable) {
		return Iterables.any(iterable, this);
	}

	/**
	 * Returns {@code true} if every element in {@code iterable} satisfies the
	 * predicate. If {@code iterable} is empty, {@code true} is returned.
	 */
	public boolean all(Iterable<T> iterable) {
		return Iterables.all(iterable, this);
	}

	/**
	 * Returns the elements of {@code unfiltered} that satisfy a predicate. The
	 * returned set is a live view of {@code unfiltered}; changes to one affect
	 * the other.
	 * 
	 * <p>
	 * The resulting set's iterator does not support {@code remove()}, but all
	 * other set methods are supported. The set's {@code add()} and {@code
	 * addAll()} methods throw an {@link IllegalArgumentException} if an element
	 * that doesn't satisfy the predicate is provided. When methods such as
	 * {@code removeAll()} and {@code clear()} are called on the filtered set,
	 * only elements that satisfy the filter will be removed from the underlying
	 * collection.
	 * 
	 * <p>
	 * The returned set isn't threadsafe or serializable, even if {@code
	 * unfiltered} is.
	 * 
	 * <p>
	 * Many of the filtered set's methods, such as {@code size()}, iterate
	 * across every element in the underlying set and determine which elements
	 * satisfy the filter. When a live view is <i>not</i> needed, it may be
	 * faster to copy the filtered set and use the copy.
	 */
	public Set<T> filter(Set<T> unfiltered) {
		return Sets.filter(unfiltered, this);
	}

	/**
	 * Returns a filtered copy of the input list, with order retained.
	 */
	public List<T> filteredCopy(List<T> unfiltered) {
		return Lists.newArrayList(filter((Iterable<T>) unfiltered));
	}

	/**
	 * Returns the elements of {@code unfiltered} that satisfy a predicate. The
	 * returned collection is a live view of {@code unfiltered}; changes to one
	 * affect the other.
	 * 
	 * <p>
	 * The resulting collection's iterator does not support {@code remove()},
	 * but all other collection methods are supported. The collection's {@code
	 * add()} and {@code addAll()} methods throw an
	 * {@link IllegalArgumentException} if an element that doesn't satisfy the
	 * predicate is provided. When methods such as {@code removeAll()} and
	 * {@code clear()} are called on the filtered collection, only elements that
	 * satisfy the filter will be removed from the underlying collection.
	 * 
	 * <p>
	 * The returned collection isn't threadsafe or serializable, even if {@code
	 * unfiltered} is.
	 * 
	 * <p>
	 * Many of the filtered collection's methods, such as {@code size()},
	 * iterate across every element in the underlying collection and determine
	 * which elements satisfy the filter. When a live view is <i>not</i> needed,
	 * it may be faster to copy the filtered collection and use the copy.
	 */
	public Collection<T> filter(Collection<T> unfiltered) {
		return Collections2.filter(unfiltered, this);
	}
	
	public <V> Map<T, V> filterKeys(Map<T, V> unfiltered) {
		return Maps.filterKeys(unfiltered, this);
	}
	
	public <K> Map<K, T> filterValues(Map<K, T> unfiltered) {
		return Maps.filterValues(unfiltered, this);
	}

	/**
	 * Returns a predicate that always evaluates to {@code true}.
	 */
	public static <T> BasePredicate<T> alwaysTrue() {
		return new WrappedPredicate<T>(Predicates.<T> alwaysTrue());
	}

	/**
	 * Returns a predicate that always evaluates to {@code false}.
	 */
	public static <T> Predicate<T> alwaysFalse() {
		return new WrappedPredicate<T>(Predicates.<T> alwaysFalse());
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if the object
	 * reference being tested is null.
	 */
	public static <T> Predicate<T> isNull() {
		return new WrappedPredicate<T>(Predicates.<T> isNull());
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if the object
	 * reference being tested is not null.
	 */
	public static <T> Predicate<T> notNull() {
		return new WrappedPredicate<T>(Predicates.<T> notNull());
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if the given predicate
	 * evaluates to {@code false}.
	 */
	public static <T> Predicate<T> not(Predicate<T> predicate) {
		return new WrappedPredicate<T>(Predicates.<T> not(predicate));
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if each of its
	 * components evaluates to {@code true}. The components are evaluated in
	 * order, and evaluation will be "short-circuited" as soon as a false
	 * predicate is found. It defensively copies the iterable passed in, so
	 * future changes to it won't alter the behavior of this predicate. If
	 * {@code components} is empty, the returned predicate will always evaluate
	 * to {@code true}.
	 */
	public static <T> Predicate<T> and(
			Iterable<? extends Predicate<? super T>> components) {
		return new WrappedPredicate<T>(Predicates.<T> and(components));
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if each of its
	 * components evaluates to {@code true}. The components are evaluated in
	 * order, and evaluation will be "short-circuited" as soon as a false
	 * predicate is found. It defensively copies the array passed in, so future
	 * changes to it won't alter the behavior of this predicate. If {@code
	 * components} is empty, the returned predicate will always evaluate to
	 * {@code true}.
	 */
	public static <T> Predicate<T> and(Predicate<? super T>... components) {
		return new WrappedPredicate<T>(Predicates.<T> and(components));
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if both of its
	 * components evaluate to {@code true}. The components are evaluated in
	 * order, and evaluation will be "short-circuited" as soon as a false
	 * predicate is found.
	 */
	public static <T> Predicate<T> and(Predicate<? super T> first,
			Predicate<? super T> second) {
		return new WrappedPredicate<T>(Predicates.<T> and(first, second));
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if any one of its
	 * components evaluates to {@code true}. The components are evaluated in
	 * order, and evaluation will be "short-circuited" as soon as as soon as a
	 * true predicate is found. It defensively copies the iterable passed in, so
	 * future changes to it won't alter the behavior of this predicate. If
	 * {@code components} is empty, the returned predicate will always evaluate
	 * to {@code false}.
	 */
	public static <T> Predicate<T> or(
			Iterable<? extends Predicate<? super T>> components) {
		return new WrappedPredicate<T>(Predicates.<T> or(components));
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if any one of its
	 * components evaluates to {@code true}. The components are evaluated in
	 * order, and evaluation will be "short-circuited" as soon as as soon as a
	 * true predicate is found. It defensively copies the array passed in, so
	 * future changes to it won't alter the behavior of this predicate. If
	 * {@code components} is empty, the returned predicate will always evaluate
	 * to {@code false}.
	 */
	public static <T> Predicate<T> or(Predicate<? super T>... components) {
		return new WrappedPredicate<T>(Predicates.<T> or(components));
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if either of its
	 * components evaluates to {@code true}. The components are evaluated in
	 * order, and evaluation will be "short-circuited" as soon as as soon as a
	 * true predicate is found.
	 */
	public static <T> Predicate<T> or(Predicate<? super T> first,
			Predicate<? super T> second) {
		return new WrappedPredicate<T>(Predicates.<T> or(first, second));
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if the object being
	 * tested {@code equals()} the given target or both are null.
	 */
	public static <T> Predicate<T> equalTo(T target) {
		return new WrappedPredicate<T>(Predicates.<T> equalTo(target));
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if the object being
	 * tested is an instance of the given class. If the object being tested is
	 * {@code null} this predicate evaluates to {@code false}.
	 * 
	 * <p>
	 * If you want to filter an {@code Iterable} to narrow its type, consider
	 * using {@link com.google.common.collect.Iterables#filter(Iterable, Class)}
	 * in preference.
	 */
	public static Predicate<Object> instanceOf(Class<?> clazz) {
		return new WrappedPredicate<Object>(Predicates.instanceOf(clazz));
	}

	/**
	 * Returns a predicate that evaluates to {@code true} if the object
	 * reference being tested is a member of the given collection. It does not
	 * defensively copy the collection passed in, so future changes to it will
	 * alter the behavior of the predicate.
	 * 
	 * This method can technically accept any Collection<?>, but using a typed
	 * collection helps prevent bugs. This approach doesn't block any potential
	 * users since it is always possible to use {@code Predicates.<Object>in()}.
	 * 
	 * @param target
	 *            the collection that may contain the function input
	 */
	public static <T> Predicate<T> in(Collection<? extends T> target) {
		return new WrappedPredicate<T>(Predicates.<T> in(target));
	}

	/**
	 * Returns the composition of a function and a predicate. For every {@code
	 * x}, the generated predicate returns {@code predicate(function(x))}.
	 * 
	 * @return the composition of the provided function and predicate
	 */
	public static <A, B> Predicate<A> compose(Predicate<B> predicate,
			Function<A, ? extends B> function) {
		return new WrappedPredicate<A>(Predicates.<A, B> compose(predicate,
				function));
	}

	private static final class WrappedPredicate<T> extends BasePredicate<T> {

		private final Predicate<T> predicate;

		public WrappedPredicate(Predicate<T> p) {
			this.predicate = p;
		}

		@Override
		public boolean apply(T object) {
			return predicate.apply(object);
		}

	}

}
