package uk.ac.warwick.util.collections;

/**
 * Container for a pair of objects
 * @see PairIterator
 * 
 */
public final class Pair<L,R> {
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
	    return other.getLeft() != null && other.getLeft().equals(left)
	        && other.getRight() != null && other.getRight().equals(right);
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
    
    public static<L,R> Pair<L,R>of(L left, R right){
		return new Pair<L,R>(left, right);
	}
}
