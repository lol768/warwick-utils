package uk.ac.warwick.util.collections;

import java.io.Serializable;

import com.google.common.base.Function;

/**
 * Container for a pair of objects.
 * 
 * Will be Serializable if and only if both L and R are Serializable.
 * @see PairIterator
 * 
 */
public final class Pair<L,R> implements Serializable {
    private static final long serialVersionUID = 3671033033084202618L;
    
    private final L left;
    private final R right;
	public Pair (L left, R right){
		this.left = left;
		this.right = right;		
	}
	public L getLeft() {
		return left;
	}
	public R getRight() {
		return right;
	}
	
	public boolean isEqual(Pair<L, R> other) {
	    return ((left == null && other.getLeft() == null) || (other.getLeft() != null && other.getLeft().equals(left)))
	        && ((right == null && other.getRight() == null) || (other.getRight() != null && other.getRight().equals(right)));
	}
	
	@SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        return obj != null
            && obj instanceof Pair
            && isEqual((Pair<L, R>)obj);
    }
	
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    @Override
    public String toString() {
        return "<" + (left == null ? "null" : left.toString()) + "," + (right == null ? "null" : right.toString()) + ">";
    }
    
    public static <L,R> Pair<L,R> of(L left, R right){
		return new Pair<L,R>(left, right);
	}
    
    public static <L,R> Function<Pair<? extends L, ? extends R>, L> leftFunction() {
        return new Function<Pair<? extends L,? extends R>, L>() {
            public L apply(Pair<? extends L, ? extends R> pair) {
                return pair.getLeft();
            }
        };
    }
    
    public static <L,R> Function<Pair<? extends L, ? extends R>, R> rightFunction() {
        return new Function<Pair<? extends L,? extends R>, R>() {
            public R apply(Pair<? extends L, ? extends R> pair) {
                return pair.getRight();
            }
        };
    }
}
