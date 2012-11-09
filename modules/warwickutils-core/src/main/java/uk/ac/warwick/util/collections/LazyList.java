package uk.ac.warwick.util.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * List implementation that protects against {@link IndexOutOfBoundException}s
 * by using a {@link Factory} to instantiate a new object whenever a null value
 * is returned or the index is larger than the size of the list.
 * <p>
 * Unsuitable for storing null values (since they will be instantiated with the Factory)
 * <p>
 * Based on {@link org.apache.commons.collections.list.LazyList}
 * 
 * @author Mat
 */
public final class LazyList<T> implements List<T> {
    
    public interface Factory<T> {
        T create();
    }
    
    private final List<T> list;
    
    private final Factory<T> factory;
    
    LazyList(List<T> delegate, Factory<T> theFactory) {
        this.list = delegate;
        this.factory = theFactory;
    }
    
    private List<T> getList() { return list; }
    
    /**
     * Decorate the get method to perform the lazy behaviour.
     * <p>
     * If the requested index is greater than the current size, the list will 
     * grow to the new size and a new object will be returned from the factory.
     * Indexes in-between the old size and the requested size are left with a 
     * placeholder that is replaced with a factory object when requested.
     * 
     * @param index  the index to retrieve
     */
    public T get(int index) {
        int size = getList().size();
        if (index < size) {
            // within bounds, get the object
            T object = getList().get(index);
            if (object == null) {
                // item is a place holder, create new one, set and return
                object = factory.create();
                getList().set(index, object);
                return object;
            } else {
                // good and ready to go
                return object;
            }
        } else {
            // we have to grow the list
            for (int i = size; i < index; i++) {
                getList().add(null);
            }
            // create our last object, set and return
            T object = factory.create();
            getList().add(object);
            return object;
        }
    }

    public List<T> subList(int fromIndex, int toIndex) {
        List<T> sub = getList().subList(fromIndex, toIndex);
        return new LazyList<T>(sub, factory);
    }
    
    public void add(int index, T element) {
        list.add(index, element);
    }

    public boolean add(T e) {
        return list.add(e);
    }

    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        return list.addAll(index, c);
    }

    public void clear() {
        list.clear();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean equals(Object o) {
        return list.equals(o);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Iterator<T> iterator() {
        return list.iterator();
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    public T remove(int index) {
        return list.remove(index);
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    public T set(int index, T element) {
        return list.set(index, element);
    }

    public int size() {
        return list.size();
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public <L> L[] toArray(L[] a) {
        return list.toArray(a);
    }

    public static <T> LazyList<T> decorate(List<T> list, Factory<T> factory) {
        return new LazyList<T>(list, factory);
    }

}
